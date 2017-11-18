package at.chex.archichexture.repository.impl;

import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.model.DocumentedEntity;
import at.chex.archichexture.repository.BaseRepository;
import com.google.common.base.Strings;
import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.EntityPathBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  /**
   * Filter/Sort attributes
   */
  /**
   * Logger
   */
  private static final Logger log = LoggerFactory.getLogger(AbstractBaseRepository.class);
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

  /**
   * We need the class of the {@link ENTITY} here. <p> ... return MyAwesomeClass.class; ...
   */
  protected abstract Class<ENTITY> getEntityClass();

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
    } catch (InstantiationException e) {
      log.error(
          "Entity {} doesn't implement an empty default constructor!",
          getEntityClass(), e);
      throw new RuntimeException(
          "Entity doesn't implement an empty default constructor!");
    } catch (IllegalAccessException e) {
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
          .getOrDefault(ARGUMENT_IGNORE_ACTIVE, Arrays.asList("false"));
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
    return list;
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
   * Override this to sort in a non-default way
   */
  protected List<OrderSpecifier<?>> getSortParameter(Map<String, List<String>> arguments) {
    return new ArrayList<OrderSpecifier<?>>();
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
          queryAttributes.put(entry.getKey(), new ArrayList<String>());
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
    List<OrderSpecifier<?>> sortParameter = getSortParameter(queryAttributes);
    if (null != sortParameter && sortParameter.size() > 0) {
      log.debug("ordering by {} parameters", sortParameter.size());
      query.orderBy(sortParameter.toArray(new OrderSpecifier[0]));
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
    List<ENTITY> returnList = new ArrayList<ENTITY>(addAdditionalQueryAttributes(
        query).list(getEntityPath()));
    if (null != queryAttributes.get(ARGUMENT_ENTITY_ID)) {
      List<ENTITY> idList = new ArrayList<ENTITY>();
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

  protected abstract Predicate getIdPredicate(Long id);

  @Override
  public ENTITY findEntityById(Long id) {
    if (null == id) {
      log.debug("findEntityById with NULL id");
      return null;
    }

    ENTITY entity = null;
    // we can use the cached entitymanager entity only, when there are no additional arguments
    if (getPermanentQueryAttributes().size() < 1) {
      entity = getEntityManager().find(getEntityClass(), id);
      ENTITY returnEntity = isActiveEntity(entity) ? entity : null;
      log.debug("Returning entity {}", returnEntity);
      return returnEntity;
    }

    JPAQuery query = createQuery().from(getEntityPath()).where(getIdPredicate(id))
        .where(getActivePredicate(true));
    if (getPermanentQueryAttributes().size() > 0) {
      query.where(getPredicateForQueryArgumentsMap(getPermanentQueryAttributes())
          .toArray(new Predicate[0]));
    }

    ENTITY returnEntity = query
        .singleResult(getEntityPath());
    log.debug("Returning entity {}", returnEntity);

    return returnEntity;
  }

  @Override
  public List<ENTITY> findEntityById(List<Long> idList) {
    if (null == idList) {
      log.debug("findEntityById with NULL List");
      return Collections.emptyList();
    }

    List<ENTITY> returnList = new ArrayList<ENTITY>();
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
