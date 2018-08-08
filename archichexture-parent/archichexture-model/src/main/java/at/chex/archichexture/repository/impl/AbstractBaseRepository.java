package at.chex.archichexture.repository.impl;

import at.chex.archichexture.Deactivatable;
import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.RemoveOnDelete;
import at.chex.archichexture.annotation.Serialized;
import at.chex.archichexture.annotation.Serialized.ExposureType;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.model.DocumentedEntity;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.slh.Reflection;
import at.chex.archichexture.slh.Values;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base implementation of {@link BaseRepository} and supports most features out of the
 * box. You need to only implement a few details of your architecture to have this running.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
public abstract class AbstractBaseRepository<ENTITY extends BaseEntity> implements
    BaseRepository<ENTITY> {

  private static final String METHOD_EQUALS = "eq";
  private static final String METHOD_LIKE = "likeIgnoreCase";
  private static final String METHOD_IN = "in";
  private static final String METHOD_SORT_ASC = "asc";
  private static final String METHOD_SORT_DESC = "desc";

  private static final String SORT_FIELD = "_order_by";
  private static final String SORT_DIRECTION = "_order_dir";
  private static final String SORT_DIRECTION_VALUE_REVERSE = "desc";


  /**
   * Logger
   */
  private static final Logger log = LoggerFactory.getLogger(AbstractBaseRepository.class);
  private final TypeToken<ENTITY> typeToken = new TypeToken<ENTITY>(getClass()) {

  };
  /**
   * Additional stuff
   */
  private Map<String, List<String>> permanentQueryAttributes = new HashMap<>();
  private JPAQueryFactory queryFactory;

  /**
   * Get all entities of the given type {@link ENTITY}. Use this for small tables like
   * configurations or for dropdowns or thelike. This is an unfiltered query, so be careful when
   * using it.
   *
   * @return all existing entities
   */
  @SuppressWarnings("unused")
  public List<ENTITY> findAll() {
    return query().selectFrom(getEntityPath()).where(getActivePredicate(true)).fetch();
  }

  /**
   * Create a new {@link JPAQuery} using the given {@link EntityManager}
   *
   * @deprecated use {@link #query()} instead
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @Deprecated
  protected JPAQuery<?> createQuery() {
    return queryFactory.query();
  }

  /**
   * Create a new {@link JPAQuery} using the given {@link EntityManager}
   */
  @SuppressWarnings("WeakerAccess")
  protected JPAQueryFactory query() {
    return queryFactory;
  }

  /**
   * This is the QueryDSL-generated Q-class. Just generate it from your JPA Model and return it
   * here.
   *
   * @return the QueryClass
   */
  protected abstract EntityPathBase<ENTITY> getEntityPath();

  /**
   * Implement this method and return your Entitymanager. You can probably use code like:
   *
   * \@PersistenceContext private javax.persistence.EntityManager entityManager;
   * protected javax.persistence.EntityManager getEntityManager() { return this.entityManager; }
   */
  protected abstract EntityManager getEntityManager();

  @Override
  @SuppressWarnings({"unchecked", "WeakerAccess"})
  public Class<ENTITY> getEntityClass() {
    return (Class<ENTITY>) typeToken.getRawType();
  }

  @SuppressWarnings("UnnecessaryBoxing")
  private Predicate getActivePredicate(boolean activeState) {
    EntityPathBase<ENTITY> entityPath = getEntityPath();
    try {
      Field declaredField = entityPath.getClass()
          .getDeclaredField(Deactivatable.FIELD_NAME_ACTIVE);
      Object fieldFromEntity = declaredField.get(entityPath);
      log.debug("Returning active Query Predicate on Field {}", fieldFromEntity);
      Method method = Reflection
          .getMethodByReflection(fieldFromEntity, METHOD_EQUALS, Boolean.class);
      if (null != method) {
        return (Predicate) method.invoke(fieldFromEntity, Boolean.valueOf(activeState));
      }
    } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      log.trace(e.getLocalizedMessage(), e);
    }
    log.debug("Returning always-true predicate");
    return new BooleanBuilder();
  }

  /**
   * Override this to check whether an {@link ENTITY} is active or not. This is used, when a {@link
   * ENTITY} is found in the {@link EntityManager} and no query is executed where we could append
   * {@link #getActivePredicate(boolean)}. Make sure to also override {@link
   * #getActivePredicate(boolean)} when overriding this.
   */
  private boolean isActiveEntity(ENTITY entity) {
    if (entity instanceof Deactivatable) {
      return ((Deactivatable) entity).getActive();
    }
    return true;
  }

  /**
   * Execute checks, if the Repo is initialized correctly
   */
  @PostConstruct
  public void init() {
    if (null == this.getEntityManager()) {
      log.error("EntityManager was not initialized correctly at {}!",
          this.getClass().getCanonicalName());
    }
    if (null == this.getEntityPath()) {
      log.error("EntityPath was not initialized correctly at {}!",
          this.getClass().getCanonicalName());
    }
    if (null == queryFactory) {
      queryFactory = new JPAQueryFactory(this::getEntityManager);
    }
  }

  /**
   * Create new instance of {@link ENTITY}
   *
   * @return the newly created entity
   */
  public ENTITY create() {
    log.debug("Create new Entity");

    try {
      return getEntityClass().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      log.error(
          "Entity {} doesn't implement an empty default constructor!",
          getEntityClass(), e);
      throw new RuntimeException(
          "Entity doesn't implement an empty default constructor!");
    }
  }

  /**
   * Override this to add {@link Predicate}s to the Arguments of Queries according to the given
   * RequestParameters {@link Map} <p> Start the implementation with super.getPredicateForQueryArgumentsMap()
   * and add your values here.
   *
   * Fields annotated with {@link Aspect} will be filtered automatically.
   *
   * Add just those, that can't be determined automatically (e.g. you have a field 'date' and you want to implement date_from and date_to)
   */
  @SuppressWarnings("WeakerAccess")
  protected Collection<Predicate> getPredicateForQueryArgumentsMap(
      Map<String, List<String>> arguments) {
    ArrayList<Predicate> list = new ArrayList<>();

    // Debug logging
    if (log.isDebugEnabled()) {
      StringBuilder joined = new StringBuilder();
      boolean first = true;
      for (String s : arguments.keySet()) {
        if (!first) {
          joined.append(",");
        }
        joined.append(s);
        first = false;
      }
      log.debug("Requested Predicates for Arguments: {}", joined.toString());
    }

    //"Active" Attribute in- or exclusion
    if (arguments.containsKey(ARGUMENT_IGNORE_ACTIVE)) {
      List<String> activeArgumentList = arguments
          .getOrDefault(ARGUMENT_IGNORE_ACTIVE, Collections.singletonList("false"));
      if (null == activeArgumentList || activeArgumentList.size() < 1) {

        // Defaulting, if none set
        list.add(getActivePredicate(true));
      } else {
        String s = activeArgumentList.get(0);

        if (!Strings.isNullOrEmpty(s)) {
          list.add(getActivePredicate(Boolean.valueOf(s)));
        } else {
          list.add(getActivePredicate(true));
        }
      }
    } else {
      list.add(getActivePredicate(true));
    }

    Class<?> classToProcess = getEntityClass();
    do {
      list.addAll(processEntityClassWithReflection(classToProcess, arguments));
    } while (null != (classToProcess = classToProcess.getSuperclass()));
    return list;
  }

  private <T> boolean arrayContainsValue(List<T> array, T value) {
    if (null == value) {
      return false;
    }
    for (T arrayValue : array) {
      if (null != arrayValue && arrayValue.equals(value)) {
        return true;
      }
    }
    return false;
  }

  private OrderSpecifier<?> getSortParameter(Map<String, List<String>> arguments) {
    if (arguments.containsKey(SORT_FIELD)) {
      List<String> sortFieldList = arguments.get(SORT_FIELD);
      if (null == sortFieldList || sortFieldList.isEmpty()) {
        log.debug("Sort parameters empty.");
        return null;
      }
      List<String> sortDirectionList = arguments.get(SORT_DIRECTION);
      String sortDirection =
          null == sortDirectionList || sortDirectionList.isEmpty() ? "" : sortDirectionList.get(0);
      log.debug("Sort direction is {}.", sortDirection);

      Class<?> classToProcess = getEntityClass();
      do {
        OrderSpecifier orderSpecifier = sortEntitiesByFieldByReflection(classToProcess,
            sortFieldList.get(0), sortDirection);
        if (null != orderSpecifier) {
          log.debug("Returning orderSpecifier {}", orderSpecifier);
          return orderSpecifier;
        }
      } while (null != (classToProcess = classToProcess.getSuperclass()));
    } else {
      log.debug("No sort parameter given.");
    }
    return null;
  }


  private OrderSpecifier sortEntitiesByFieldByReflection(Class<?> clazz,
      String value, String direction) {
    if (null == clazz || Strings.isNullOrEmpty(value)) {
      log.warn("Unable to process class {} or value {}", clazz, value);
      return null;
    }

    for (Field field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(Aspect.class)) {
        Aspect aspect = field.getAnnotation(Aspect.class);
        log.trace("Processing aspect (sort) for Field:{}", field);

        List<String> filterNames = new ArrayList<>();
        if (field.isAnnotationPresent(AlternativeNames.class)) {
          AlternativeNames alternativeNames = field.getAnnotation(AlternativeNames.class);
          filterNames.addAll(Arrays.asList(alternativeNames.value()));
        }
        if (field.isAnnotationPresent(Exposed.class)) {
          Exposed exposed = field.getAnnotation(Exposed.class);
          if (!Strings.isNullOrEmpty(exposed.value())) {
            filterNames.add(exposed.value());
          }
        }

        if (aspect.filterable() && (field.getName().equals(value) || arrayContainsValue(
            filterNames, value))) {
          log.trace("This is the aspect we are filtering for! {}", aspect);

          EntityPathBase<ENTITY> entityPath = getEntityPath();
          try {
            Field fieldDefinition = entityPath.getClass().getField(field.getName());
            Object fieldFromEntity = fieldDefinition.get(entityPath);
            String methodToInvoke =
                SORT_DIRECTION_VALUE_REVERSE.equals(direction) ? METHOD_SORT_DESC : METHOD_SORT_ASC;
            log.trace("Trying to invoke Method {} with parameter of type {} on class {}",
                methodToInvoke, field.getType(), fieldFromEntity.getClass());
            Method method;
            try {
              method = fieldFromEntity.getClass()
                  .getMethod(methodToInvoke);
            } catch (NoSuchMethodException e) {
              log.debug(
                  "Fallback <{}> of Class <{}> not found by reflection! Retrying with Parameter type Object",
                  value, clazz.getName());
              method = fieldFromEntity.getClass()
                  .getMethod(methodToInvoke, Object.class);
            }

            Object invocationResult = method.invoke(fieldFromEntity);
            if (invocationResult instanceof OrderSpecifier) {
              return (OrderSpecifier) invocationResult;
            } else {
              log.error("Invocation Result was NOT a Predicate: {}", invocationResult);
            }
          } catch (IllegalAccessException e) {
            log.error("Field <{}> of Class <{}> cannot be accessed by reflection!", value,
                entityPath.getClass().getName(), e);
          } catch (NoSuchFieldException e) {
            log.error(
                "Field <{}> of Class <{}> not found by reflection! Did you add a new field in the JPA Model and forgot to rerun the QEntity generation?",
                value,
                entityPath.getClass().getName(), e);
          } catch (NoSuchMethodException e) {
            log.error("Method <{}> of Class <{}> not found by reflection!", value, clazz.getName(),
                e);
          } catch (InvocationTargetException e) {
            log.error("Method <{}> of Class <{}> cannot be invoked by reflection!", value,
                clazz.getName(), e);
          }
        }
      }
    }
    return null;
  }

  private List<Predicate> processEntityClassWithReflection(Class<?> clazz,
      Map<String, List<String>> arguments) {
    List<Predicate> predicateList = new ArrayList<>();
    for (Field field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(Aspect.class)) {
        Aspect aspect = field.getAnnotation(Aspect.class);
        log.trace("Processing aspect for Field:{}", field);
        if (aspect.filterable()) {
          Set<String> filterNames = new HashSet<>();

          if (field.isAnnotationPresent(AlternativeNames.class)) {
            AlternativeNames alternativeNames = field.getAnnotation(AlternativeNames.class);
            filterNames.addAll(Arrays.asList(alternativeNames.value()));
          }

          String name = field.getName();
          filterNames.add(name);

          String idAppendix = null;

          if (clazz.isAnnotationPresent(Serialized.class)) {
            Serialized serialized = clazz.getAnnotation(Serialized.class);
            if (ExposureType.ID.equals(serialized.exposeNested()) ||
                ExposureType.BOTH.equals(serialized.exposeNested())) {
              idAppendix = serialized.idAppendix();
              String finalIdAppendix = idAppendix;
              filterNames.addAll(filterNames.stream().filter(Objects::nonNull)
                  .map(s -> s.concat(finalIdAppendix)).collect(Collectors.toList()));
            }
          }

          Set<String> valuesForKeys = Values.getValuesForKeys(arguments, filterNames);
          if (!valuesForKeys.isEmpty()) {
            EntityPathBase<ENTITY> entityPath = getEntityPath();
            try {
              boolean isIdValue = false;
              Field fieldDefinition;
              try {
                //
                for (String value : valuesForKeys) {
                  Long.parseLong(value);
                }
                fieldDefinition = entityPath.getClass().getField(name.concat(".id"));
                isIdValue = true;
              } catch (NumberFormatException nfe) {
                // this just means, it's not an ID, but maybe a complex object instead
                fieldDefinition = entityPath.getClass().getField(name);
                isIdValue = false;
              }

              Object fieldFromEntity = fieldDefinition.get(entityPath);
              String methodToInvoke =
                  !isIdValue && !aspect.strict() && field.getType().equals(String.class)
                      ? METHOD_LIKE
                      : METHOD_EQUALS;
              if (valuesForKeys.size() > 1) {
                methodToInvoke = METHOD_IN;
              }
              log.trace("Trying to invoke Method {} with parameter of type {} on class {}",
                  methodToInvoke, field.getType(), fieldFromEntity.getClass());

              Method method = Reflection
                  .getMethodByReflection(fieldFromEntity, methodToInvoke, field.getType());
              if (null != method) {
                for (String value : valuesForKeys) {
                  //(field.getType()) value;
                  Object invocationResult = Reflection
                      .invokeMethodWithCorrectArgumentsType(fieldFromEntity, method,
                          field.getType(),
                          value);
                  if (invocationResult instanceof Predicate) {
                    predicateList.add((Predicate) invocationResult);
                  } else {
                    log.error("Invocation Result was NOT a Predicate: {}", invocationResult);
                  }
                }
              } else {
                log.debug("Unable to find method {} in class {}", methodToInvoke,
                    clazz.getSimpleName());
              }
            } catch (IllegalAccessException e) {
              log.error("Field <{}> of Class <{}> cannot be accessed by reflection!", name,
                  entityPath.getClass().getName(), e);
            } catch (NoSuchFieldException e) {
              log.error("Field <{}> of Class <{}> not found by reflection!", name,
                  entityPath.getClass().getName(), e);
            } catch (NoSuchMethodException e) {
              log.error("Method <{}> of Class <{}> not found by reflection!", name, clazz.getName(),
                  e);
            } catch (InvocationTargetException e) {
              log.error("Method <{}> of Class <{}> cannot be invoked by reflection!", name,
                  clazz.getName(), e);
            }
          }
        }
      }
    }
    return predicateList;
  }

  /**
   * These arguments will be handed over on each query, narrowing down your search by a fixed value
   * (e.g. logged in User)
   */
  @Override
  public void addPermanentQueryAttribute(String key, Collection<String> values) {
    log.debug("Adding permanent Attribute {}:{}", key, values);
    permanentQueryAttributes.put(key, new ArrayList<>());
    permanentQueryAttributes.get(key).addAll(values);
  }

  /**
   * Permanently set query attributes
   */
  private Map<String, List<String>> getPermanentQueryAttributes() {
    return permanentQueryAttributes;
  }

  /**
   * Override this if you always want all values (no limits)
   */
  @SuppressWarnings({"WeakerAccess", "SameReturnValue"})
  protected boolean canBeLimited() {
    return true;
  }

  /**
   * This is your main select method. Put all your arguments, limit and offset parameters here
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<ENTITY> list(Map<String, List<String>> arguments, int limit,
      int offset) {
    JPAQuery query = query().selectFrom(getEntityPath()).where(getActivePredicate(true));
    Map<String, List<String>> queryAttributes = new HashMap<>(
        getPermanentQueryAttributes());
    // Merge those 2 Maps carrying the arguments for the query
    if (null != arguments) {
      for (Entry<String, List<String>> entry : arguments.entrySet()) {
        if (!queryAttributes.containsKey(entry.getKey())) {
          queryAttributes.put(entry.getKey(), new ArrayList<>());
        }
        queryAttributes.get(entry.getKey()).addAll(entry.getValue());
      }
    }

    if (queryAttributes.size() > 0) {
      // Add all query parameters
      query.where(getPredicateForQueryArgumentsMap(queryAttributes).toArray(new Predicate[0]));
    }
    // Prepare and add all sort parameters
    OrderSpecifier<?> sortParameter = getSortParameter(queryAttributes);
    if (null != sortParameter) {
      log.debug("ordering by parameter {}", sortParameter);
      query.orderBy(sortParameter);
    }

    // Limit and offset go together
    if (canBeLimited() && limit > 0) {
      query.limit(limit).offset(offset);
    }

    // Execute the query
    List<ENTITY> returnList = new ArrayList<ENTITY>(addAdditionalQueryAttributes(
        query).fetch());
    if (null != queryAttributes.get(ARGUMENT_ENTITY_ID)) {
      List<ENTITY> idList = new ArrayList<>();
      if (null != arguments) {
        for (String arg : arguments.get(ARGUMENT_ENTITY_ID)) {
          idList.add(findEntityById(Long.valueOf(arg)));
        }
      }
      returnList.retainAll(idList);
    }
    log.debug("Returning List of entities {}", returnList);
    return returnList;
  }

  /**
   * Override this to add additional stuff to your query like sort or whatever
   */
  @SuppressWarnings("WeakerAccess")
  protected <T> JPAQuery<T> addAdditionalQueryAttributes(JPAQuery<T> query) {
    return query;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ENTITY findEntityById(Long id) {
    if (null == id) {
      log.debug("findEntityById with NULL id");
      return null;
    }

    EntityPathBase<ENTITY> entityPath = getEntityPath();

    if (getPermanentQueryAttributes().size() < 1) {
      // we can use the cached entitymanager entity only, when there are no additional arguments
      ENTITY entity = getEntityManager().find(getEntityClass(), id);
      if (null != entity) {
        ENTITY returnEntity = isActiveEntity(entity) ? entity : null;
        log.debug("Returning entity {}", returnEntity);
        return returnEntity;
      }
    }

    JPAQuery<ENTITY> query = query().selectFrom(entityPath)
        .where(getActivePredicate(true));

    try {
      Field idField = entityPath.getClass().getDeclaredField("id");
      if (!idField.isAccessible()) {
        idField.setAccessible(true);
      }
      NumberPath<Long> idPath = (NumberPath<Long>) idField.get(entityPath);
      query.where(idPath.eq(id));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      log.warn("No ID Field found in Entity {}", entityPath);
      return null;
    }

    if (getPermanentQueryAttributes().size() > 0) {
      query.where(getPredicateForQueryArgumentsMap(getPermanentQueryAttributes())
          .toArray(new Predicate[0]));
    }

    ENTITY returnEntity = query.fetchOne();
    log.debug("Returning entity {}", returnEntity);

    return returnEntity;
  }

  @Override
  public List<ENTITY> findEntityById(List<Long> idList) {
    if (null == idList) {
      log.debug("findEntityById with NULL List");
      return Collections.emptyList();
    }

    List<ENTITY> returnList = new ArrayList<>();
    for (Long id : idList) {
      ENTITY entity = findEntityById(id);
      // TODO optimize with list query for those not in the EntityManager
      if (null != entity) {
        returnList.add(entity);
      } else {
        log.debug("Cannot find entity {} with id {}", getEntityClass().getSimpleName(), id);
      }
    }
    return returnList;
  }

  @Override
  public boolean delete(ENTITY entity) {
    if (null == entity) {
      log.debug("Delete called for NULL entity");
      return false;
    }

    if (entity instanceof Deactivatable && !entity.getClass()
        .isAnnotationPresent(RemoveOnDelete.class)) {
      Boolean visibleBefore = ((Deactivatable) entity).getActive();
      visibleBefore = null == visibleBefore ? true : visibleBefore;
      ((Deactivatable) entity).setActive(false);
      if (entity instanceof DocumentedEntity) {
        if (visibleBefore) {
          ((DocumentedEntity) entity).setDeletedAt(new Date());
        }
      }
      save(entity);
      return visibleBefore;
    }
    if (!getEntityManager().contains(entity)) {
      ENTITY foundEntity = findEntityById(entity.getId());
      if (null == foundEntity) {
        log.debug("Tried to delete entity {}, but it was not contained in the entitymanager");
        return false;
      } else {
        getEntityManager().remove(foundEntity);
        return true;
      }
    }
    getEntityManager().remove(entity);
    return true;
  }

  @Override
  public ENTITY save(ENTITY entity) {
    if (null == entity) {
      log.debug("Save called for NULL entity");
      return null;
    }

    log.trace("Save called for entity {}", entity);
    EntityManager entityManager = getEntityManager();
    return entityManager.merge(entity);
  }
}
