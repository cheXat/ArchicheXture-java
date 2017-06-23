package at.chex.archichexture.repository.impl;

import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.EntityPathBase;
import java.util.ArrayList;
import java.util.Collection;
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

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(AbstractBaseRepository.class);
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
    return createQuery().from(getEntityPath()).list(getEntityPath());
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
   * Implement this method and return your Entitymanager.
   * You can probably use code like:
   *
   * @return the EntityManager
   * @PersistenceContext private javax.persistence.EntityManager entityManager;
   * @Override protected javax.persistence.EntityManager getEntityManager() { return
   * this.entityManager; }
   */
  protected abstract EntityManager getEntityManager();

  /**
   * We need the class of the {@link ENTITY} here.
   * <p>
   * ...
   * return MyAwesomeClass.class;
   * ...
   */
  protected abstract Class<ENTITY> getEntityClass();

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
   * Override this to add {@link Predicate}s to the Arguments of Queries
   * according to the given RequestParameters {@link Map}
   * <p>
   * Start the implementation with super.getPredicateForQueryArgumentsMap()
   * and add your values here.
   */
  protected Collection<Predicate> getPredicateForQueryArgumentsMap(
      Map<String, List<String>> arguments) {
    return new ArrayList<Predicate>();
  }

  /**
   * These arguments will be handed over on each query, narrowing down your search by a fixed value
   * (e.g. logged in User)
   */
  @Override
  public void addPermanentQueryAttribute(String key, Collection<String> values) {
    if (!permanentQueryAttributes.containsKey(key)) {
      permanentQueryAttributes.put(key, new ArrayList<String>());
    }
    permanentQueryAttributes.get(key).addAll(values);
  }

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
    JPAQuery query = createQuery().from(getEntityPath());
    Map<String, List<String>> queryAttributes = new HashMap<>(
        getPermanentQueryAttributes());
    // Merge those 2 Maps carrying the arguments for the query
    for (Entry<String, List<String>> entry : arguments.entrySet()) {
      if (!queryAttributes.containsKey(entry.getKey())) {
        queryAttributes.put(entry.getKey(), new ArrayList<String>());
      }
      queryAttributes.get(entry.getKey()).addAll(entry.getValue());
    }

    if (null != queryAttributes && queryAttributes.size() > 0) {
      for (Predicate predicate : getPredicateForQueryArgumentsMap(queryAttributes)) {
        query.where(predicate);
      }
      List<OrderSpecifier<?>> sortParameter = getSortParameter(queryAttributes);
      if (sortParameter.size() > 0) {
        log.debug("ordering by {} parameters", sortParameter.size());
        for (OrderSpecifier<?> predicate : sortParameter) {
          query.orderBy(predicate);
        }
      }
    }
    if (canBeLimited() && limit > 0) {
      query.limit(limit).offset(offset);
    }
    List<ENTITY> returnList = new ArrayList<ENTITY>(addAdditionalQueryAttributes(
        query).list(getEntityPath()));
    if (null != queryAttributes && null != queryAttributes.get(ARGUMENT_ENTITY_ID)) {
      List<ENTITY> idList = new ArrayList<ENTITY>();
      for (String arg : arguments.get(ARGUMENT_ENTITY_ID)) {
        idList.add(findEntityById(Long.valueOf(arg)));
      }
      returnList.retainAll(idList);
    }
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
    ENTITY entity = getEntityManager().find(getEntityClass(), id);
    if (null == entity) {
      return createQuery().from(getEntityPath()).where(getIdPredicate(id))
          .singleResult(getEntityPath());
    }
    return entity;
  }

  @Override
  public List<ENTITY> findEntityById(List<Long> idList) {
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
    if (!getEntityManager().contains(entity)) {
      log.debug("Tried to delete entity {}, but it was not contained in the entitymanager");
      return false;
    }
    getEntityManager().remove(entity);
    return true;
  }

  @Override
  public ENTITY save(ENTITY entity) {
    log.trace("Save called for entity {}", entity);
    EntityManager entityManager = getEntityManager();
    // if (!entityManager.contains(entity)) {
    // entityManager.persist(entity);
    // FIXME reload entity
    // } else {
    ENTITY mergedEntity = entityManager.merge(entity);
    // }
    return mergedEntity;
  }


}
