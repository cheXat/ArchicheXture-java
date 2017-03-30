package at.chex.archichexture.repository.impl;

import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.EntityPathBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is the base implementation of {@link BaseRepository} and supports most features out of the box.
 * You need to only implement a few details of your architecture to have this running.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
public abstract class AbstractBaseRepository<ENTITY extends BaseEntity> implements BaseRepository<ENTITY> {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(AbstractBaseRepository.class);

    /**
     * Implement this method and return your Entitymanager.
     * You can probably use code like:
     *
     * @return the EntityManager
     * @PersistenceContext private javax.persistence.EntityManager entityManager;
     * @Override protected javax.persistence.EntityManager getEntityManager() {
     * return this.entityManager;
     * }
     */
    protected abstract EntityManager getEntityManager();

    /**
     * This is the QueryDSL-generated Q-class. Just generate it from your JPA Model and return it here.
     *
     * @return the QueryClass
     */
    protected abstract EntityPathBase<ENTITY> getEntityPath();

    /**
     * We need the class of the {@link ENTITY} here.
     * <p>
     * ...
     * return MyAwesomeClass.class;
     * ...
     *
     * @return
     */
    protected abstract Class<ENTITY> getEntityClass();

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
     *
     * @param arguments
     * @return
     */
    protected Collection<Predicate> getPredicateForQueryArgumentsMap(
            Map<String, List<String>> arguments) {
        return new ArrayList<Predicate>();
    }

    /**
     * Override this to sort in a non-default way
     *
     * @param arguments
     * @return
     */
    protected List<OrderSpecifier<?>> getSortParameter(Map<String, List<String>> arguments) {
        return new ArrayList<OrderSpecifier<?>>();
    }

    /**
     * Override this if you always want all values (no limits)
     *
     * @return
     */
    protected boolean canBeLimited() {
        return true;
    }

    /**
     * This is your main select method. Put all your arguments, limit and offset parameters
     *
     * @param arguments
     * @param limit
     * @param offset
     * @return
     */
    @Override
    public List<ENTITY> list(Map<String, List<String>> arguments, int limit,
                             int offset) {
        JPAQuery query = createQuery().from(getEntityPath());
        if (null != arguments) {
            for (Predicate predicate : getPredicateForQueryArgumentsMap(arguments)) {
                query.where(predicate);
            }
            List<OrderSpecifier<?>> sortParameter = getSortParameter(arguments);
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
        if (null != arguments && null != arguments.get(ARGUMENT_ENTITY_ID)) {
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
     *
     * @param query
     * @return
     */
    protected JPAQuery addAdditionalQueryAttributes(JPAQuery query) {
        return query;
    }

    @Override
    public ENTITY findEntityById(Long id) {
        return getEntityManager().find(getEntityClass(), id);
    }

    @Override
    public List<ENTITY> findEntityById(List<Long> idList) {
        List<ENTITY> returnList = new ArrayList<ENTITY>();
        for (Long id : idList) {
            ENTITY entity = findEntityById(id);
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

    /**
     * Create a new {@link JPAQuery} using the given {@link EntityManager}
     *
     * @return
     */
    protected JPAQuery createQuery() {
        return new JPAQuery(getEntityManager());
    }

    /**
     * Create a new {@link JPASubQuery}
     *
     * @return
     */
    protected JPASubQuery createSubQuery() {
        return new JPASubQuery();
    }

    /**
     * Get all entities of the given type {@link ENTITY}. Use this for small tables like configurations or for dropdowns or thelike.
     * This is an unfiltered query, so be careful when using it.
     *
     * @return all existing entities
     */
    public List<ENTITY> findAll() {
        return createQuery().from(getEntityPath()).list(getEntityPath());
    }
}
