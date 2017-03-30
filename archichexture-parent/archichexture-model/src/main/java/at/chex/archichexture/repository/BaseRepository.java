package at.chex.archichexture.repository;

import at.chex.archichexture.model.BaseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Implement this interface and annotate it with an "injectable" Annotation. That is something like {@link Stateless}, {@link Singleton}, {@link Named}, ...
 * Everything, that is injected by your engine at the injection points of {@link Inject} to your controllers.
 * <p>
 * Hint: inherit from {@link at.chex.archichexture.repository.impl.AbstractBaseRepository} to get your kickstart without to much coding.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
public interface BaseRepository<ENTITY extends BaseEntity> extends
        Serializable {
    /**
     * Primary Key identifier
     */
    String ARGUMENT_ENTITY_ID = "id";
    /**
     * Sort column identifier
     */
    String PARAMETER_SORT = "sortBy";
    /**
     * Sort order identifier
     * <p>
     * asc/desc (default/none given: asc)
     */
    String PARAMETER_SORT_ORDER = "sortOrder";
    /**
     * Reverse sort order identifier
     */
    String PARAMETER_SORT_ORDER_VALUE_DESC = "desc";

    /**
     * Select for an {@link ENTITY} with the given primary key
     *
     * @param id
     * @return
     */
    ENTITY findEntityById(Long id);

    /**
     * Delete the given instance of {@link ENTITY} from the EntityManager (and the database of course)
     *
     * @param entity
     * @return
     */
    boolean delete(ENTITY entity);

    /**
     * Create a new instance of {@link ENTITY}
     *
     * @return
     */
    ENTITY create();

    /**
     * Saves the given {@link ENTITY} and returns the created (and reloaded) or
     * updated {@link ENTITY}
     *
     * @param entity
     * @return
     */
    ENTITY save(ENTITY entity);

    /**
     * Select for a List of {@link ENTITY}s with the given parameters, limits and offsets
     *
     * @param arguments
     * @param limit
     * @param offset
     * @return
     */
    List<ENTITY> list(Map<String, List<String>> arguments, int limit,
                      int offset);

    /**
     * Select for a List of {@link ENTITY}s with the given primary keys
     *
     * @param id
     * @return
     */
    List<ENTITY> findEntityById(List<Long> id);
}