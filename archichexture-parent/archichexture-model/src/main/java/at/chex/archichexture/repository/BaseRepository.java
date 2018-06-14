package at.chex.archichexture.repository;

import at.chex.archichexture.model.BaseEntity;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implement this interface and annotate it with an "injectable" Annotation. That is something like
 * {@link Stateless}, {@link Singleton}, {@link Named}, ... Everything, that is injected by your
 * engine at the injection points of {@link Inject} to your controllers. <p> Hint: inherit from
 * {@link at.chex.archichexture.repository.impl.AbstractBaseRepository} to get your kickstart
 * without too much coding.
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
   * Ignore the default filtering for active entities (not supported by all)
   */
  String ARGUMENT_IGNORE_ACTIVE = "ignore_active";
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
   */
  ENTITY findEntityById(Long id);

  /**
   * Delete the given instance of {@link ENTITY} from the EntityManager (and the database of course)
   */
  boolean delete(ENTITY entity);

  /**
   * Create a new instance of {@link ENTITY}
   */
  ENTITY create();

  /**
   * Saves the given {@link ENTITY} and returns the created (and reloaded) or
   * updated {@link ENTITY}
   */
  ENTITY save(ENTITY entity);

  /**
   * These arguments will be handed over on each query, narrowing down your search by a fixed value
   * (e.g. logged in User)
   */
  @SuppressWarnings("unused")
  void addPermanentQueryAttribute(String key, Collection<String> values);

  /**
   * Select for a List of {@link ENTITY}s with the given parameters, limits and offsets
   */
  List<ENTITY> list(Map<String, List<String>> arguments, int limit,
      int offset);

  /**
   * Select for a List of {@link ENTITY}s with the given primary keys
   */
  @SuppressWarnings("unused")
  List<ENTITY> findEntityById(List<Long> id);
}