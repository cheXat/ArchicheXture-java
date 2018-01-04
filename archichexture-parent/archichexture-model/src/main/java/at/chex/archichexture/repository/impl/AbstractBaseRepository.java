package at.chex.archichexture.repository.impl;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.collections.Values;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.model.DocumentedEntity;
import at.chex.archichexture.model.QBaseEntity;
import at.chex.archichexture.reflect.Reflection;
import at.chex.archichexture.repository.BaseRepository;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.EntityPathBase;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

  /**
   * Create a new {@link JPASubQuery}
   */
  protected JPASubQuery createSubQuery() {
    return new JPASubQuery();
  }

  /**
   * Get all entities of the given type {@link ENTITY}. Use this for small tables like
   * configurations or for dropdowns or thelike. This is an unfiltered query, so be careful when
   * using it.
   *
   * @return all existing entities
   */
  public List<ENTITY> findAll() {
    return createQuery().from(getEntityPath()).where(getActivePredicate(true))
        .list(getEntityPath());
  }

  /**
   * Create a new {@link JPAQuery} using the given {@link EntityManager}
   */
  protected JPAQuery createQuery() {
    return new JPAQuery(getEntityManager());
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
   * @return the EntityManager
   * @PersistenceContext private javax.persistence.EntityManager entityManager;
   * @Override protected javax.persistence.EntityManager getEntityManager() { return
   * this.entityManager; }
   */
  protected abstract EntityManager getEntityManager();

  private Class<ENTITY> getEntityClass() {
    return (Class<ENTITY>) typeToken.getRawType();
  }

  /**
   * Override this to make your {@link ENTITY}-Query aware of an active or inactive state (that is
   * the argument). Make sure to also override {@link #isActiveEntity(BaseEntity)} when overriding
   * this.
   */
  protected Predicate getActivePredicate(boolean activeState) {
    return new BooleanBuilder();
  }

  /**
   * Override this to check whether an {@link ENTITY} is active or not. This is used, when a {@link
   * ENTITY} is found in the {@link EntityManager} and no query is executed where we could append
   * {@link #getActivePredicate(boolean)}. Make sure to also override {@link
   * #getActivePredicate(boolean)} when overriding this.
   */
  protected boolean isActiveEntity(ENTITY entity) {
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
    if (null == this.getEntityClass()) {
      log.error("EntityClass was not initialized correctly at {}!",
          this.getClass().getCanonicalName());
    }
    if (null == this.getEntityPath()) {
      log.error("EntityPath was not initialized correctly at {}!",
          this.getClass().getCanonicalName());
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
  protected Collection<Predicate> getPredicateForQueryArgumentsMap(
      Map<String, List<String>> arguments) {
    ArrayList<Predicate> list = new ArrayList<>();
    /**
     * Debug logging
     */
    if (log.isDebugEnabled()) {
      String joined = "";
      boolean first = true;
      for (String s : arguments.keySet()) {
        if (!first) {
          joined += ",";
        }
        joined += s;
        first = false;
      }
      log.debug("Requested Predicates for Arguments: {}", joined);
    }
    /**
     * "Active" Attribute in- or exclusion
     */
    if (arguments.containsKey(ARGUMENT_IGNORE_ACTIVE)) {
      List<String> activeArgumentList = arguments
          .getOrDefault(ARGUMENT_IGNORE_ACTIVE, Collections.singletonList("false"));
      if (null == activeArgumentList || activeArgumentList.size() < 1) {
        /**
         * Defaulting, if none set
         */
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

  private <T> boolean arrayContainsValue(T[] array, T value) {
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
        log.debug("Processing aspect (sort) for Field:{}", field);

        String[] filterNames = new String[0];
        if (field.isAnnotationPresent(AlternativeNames.class)) {
          AlternativeNames alternativeNames = field.getAnnotation(AlternativeNames.class);
          filterNames = alternativeNames.value();
        }

        if (aspect.filterable() && (field.getName().equals(value) || arrayContainsValue(
            filterNames, value))) {
          log.debug("This is the aspect we are filtering for! {}", aspect);

          EntityPathBase<ENTITY> entityPath = getEntityPath();
          try {
            Field fieldDefinition = entityPath.getClass().getField(field.getName());
            Object fieldFromEntity = fieldDefinition.get(entityPath);
            String methodToInvoke =
                SORT_DIRECTION_VALUE_REVERSE.equals(direction) ? METHOD_SORT_DESC : METHOD_SORT_ASC;
            log.debug("Trying to invoke Method {} with parameter of type {} on class {}",
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
        log.debug("Processing aspect for Field:{}", field);
        if (aspect.filterable()) {
          List<String> filterNames = new ArrayList<>();

          if (field.isAnnotationPresent(AlternativeNames.class)) {
            AlternativeNames alternativeNames = field.getAnnotation(AlternativeNames.class);
            filterNames.addAll(Arrays.asList(alternativeNames.value()));
          }

          String name = field.getName();
          filterNames.add(name);
          Set<String> valuesForKeys = Values.getValuesForKeys(arguments, filterNames);
          if (!valuesForKeys.isEmpty()) {
            EntityPathBase<ENTITY> entityPath = getEntityPath();
            try {
              Field fieldDefinition = entityPath.getClass().getField(name);
              Object fieldFromEntity = fieldDefinition.get(entityPath);
              String methodToInvoke =
                  !aspect.strict() && field.getType().equals(String.class) ? METHOD_LIKE
                      : METHOD_EQUALS;
              if (valuesForKeys.size() > 1) {
                methodToInvoke = METHOD_IN;
              }
              log.debug("Trying to invoke Method {} with parameter of type {} on class {}",
                  methodToInvoke, field.getType(), fieldFromEntity.getClass());

              Method method = Reflection
                  .getMethodByReflection(fieldFromEntity, methodToInvoke, field.getType());

              for (String value : valuesForKeys) {
                //(field.getType()) value;
                Object invocationResult = Reflection
                    .invokeMethodWithCorrectArgumentsType(fieldFromEntity, method, field.getType(),
                        value);
                if (invocationResult instanceof Predicate) {
                  predicateList.add((Predicate) invocationResult);
                } else {
                  log.error("Invocation Result was NOT a Predicate: {}", invocationResult);
                }
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
    permanentQueryAttributes.put(key, new ArrayList<String>());
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
  protected boolean canBeLimited() {
    return true;
  }

  /**
   * This is your main select method. Put all your arguments, limit and offset parameters here
   */
  @Override
  public List<ENTITY> list(Map<String, List<String>> arguments, int limit,
      int offset) {
    JPAQuery query = createQuery().from(getEntityPath()).where(getActivePredicate(true));
    Map<String, List<String>> queryAttributes = new HashMap<>(
        getPermanentQueryAttributes());
    /**
     * Merge those 2 Maps carrying the arguments for the query
     */
    if (null != arguments) {
      for (Entry<String, List<String>> entry : arguments.entrySet()) {
        if (!queryAttributes.containsKey(entry.getKey())) {
          queryAttributes.put(entry.getKey(), new ArrayList<>());
        }
        queryAttributes.get(entry.getKey()).addAll(entry.getValue());
      }
    }

    if (queryAttributes.size() > 0) {
      /**
       * Add all query parameters
       */
      query.where(getPredicateForQueryArgumentsMap(queryAttributes).toArray(new Predicate[0]));
    }
    /**
     * Prepare and add all sort parameters
     */
    OrderSpecifier<?> sortParameter = getSortParameter(queryAttributes);
    if (null != sortParameter) {
      log.debug("ordering by parameter {}", sortParameter);
      query.orderBy(sortParameter);
    }
    /**
     * Limit and offset go together
     */
    if (canBeLimited() && limit > 0) {
      query.limit(limit).offset(offset);
    }
    /**
     * Execute the query
     */
    List<ENTITY> returnList = new ArrayList<>(addAdditionalQueryAttributes(
        query).list(getEntityPath()));
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
  protected JPAQuery addAdditionalQueryAttributes(JPAQuery query) {
    return query;
  }

  @Override
  public ENTITY findEntityById(Long id) {
    if (null == id) {
      log.debug("findEntityById with NULL id");
      return null;
    }

    EntityPathBase<ENTITY> entityPath = getEntityPath();

    if (getPermanentQueryAttributes().size() < 1) {
      // we can use the cached entitymanager entity only, when there are no additional arguments
      ENTITY entity = getEntityManager().find(getEntityClass(), id);
      ENTITY returnEntity = isActiveEntity(entity) ? entity : null;
      log.debug("Returning entity {}", returnEntity);
      return returnEntity;
    }

    JPAQuery query = createQuery().from(entityPath).where(((QBaseEntity) entityPath).id.eq(id))
        .where(getActivePredicate(true));

    if (getPermanentQueryAttributes().size() > 0) {
      query.where(getPredicateForQueryArgumentsMap(getPermanentQueryAttributes())
          .toArray(new Predicate[0]));
    }

    ENTITY returnEntity = query
        .singleResult(entityPath);
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

    if (entity instanceof DocumentedEntity && ((DocumentedEntity) entity)
        .isDeletedByDeactivation()) {
      Boolean visibleBefore = ((DocumentedEntity) entity).getActive();
      visibleBefore = null == visibleBefore ? true : visibleBefore;
      ((DocumentedEntity) entity).setActive(false);
      if (visibleBefore) {
        ((DocumentedEntity) entity).setDeletedAt(new Date());
      }
      save(entity);
      return visibleBefore;
    }
    if (!getEntityManager().contains(entity)) {
      log.debug("Tried to delete entity {}, but it was not contained in the entitymanager");
      return false;
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

    if (entity instanceof DocumentedEntity) {
      if (null == ((DocumentedEntity) entity).getCreatedAt()) {
        ((DocumentedEntity) entity).setCreatedAt(new Date());
      }
      if (null == ((DocumentedEntity) entity).getActive()) {
        ((DocumentedEntity) entity).setActive(true);
      }
      ((DocumentedEntity) entity).setUpdatedAt(new Date());
    }

    log.trace("Save called for entity {}", entity);
    EntityManager entityManager = getEntityManager();
    return entityManager.merge(entity);
  }
}
