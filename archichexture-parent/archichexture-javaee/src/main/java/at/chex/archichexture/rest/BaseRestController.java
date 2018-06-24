package at.chex.archichexture.rest;

import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.service.TransferEntitiesService;
import at.chex.archichexture.slh.Reflection;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class to inherit from, when building web controllers. We suggest to use the
 * implementations ( {@link TokenBaseRestController}, {@link } ) provided, that already inherit from
 * here.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/03/2017
 */
public abstract class BaseRestController<ENTITY extends BaseEntity>
    implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(BaseRestController.class);

  @Inject
  private TransferEntitiesService transferEntitiesService;

  /**
   * Execute PUT
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected ENTITY internalExecutePUTRequest(@Nonnull ENTITY formParam) {
    log.debug("Create new entity for {}", formParam);
    try {
      ENTITY entity = updateOrCreateEntityFromParameters(formParam, createNewEntity());
      log.info("Successfully created {}", entity);
      return entity;
    } catch (IllegalArgumentException ex) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }
  }

  @Nonnull
  private ENTITY updateOrCreateEntityFromParameters(
      @Nonnull ENTITY formObject, @Nonnull ENTITY entity) throws IllegalArgumentException {
    transferEntitiesService.transfer(formObject, entity);
    ENTITY entityWithValues = Reflection
        .transferValuesFromLeftToRight(formObject, entity);
    log.debug("Entity after transfer is {}", entityWithValues);
    return getEntityRepository().save(updateAdditionalParameters(formObject, entityWithValues));
  }

  /**
   * Values are directly updated from the left {@link ENTITY} to the right {@link ENTITY}, but if you need something additional (e.g. unwrap given ids to value objects in the {@link ENTITY}), this is the place for it.
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @Nonnull
  protected ENTITY updateAdditionalParameters(@Nonnull ENTITY left,
      @Nonnull ENTITY right) {
    return right;
  }

  /**
   * Override this to interfere with entity creation
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected ENTITY createNewEntity() {
    return getEntityRepository().create();
  }

  /**
   * We assume, there is a concrete implementation of {@link BaseRepository} for {@link ENTITY}
   * somewhere around, that we can use.
   */
  protected abstract BaseRepository<ENTITY> getEntityRepository();

  /**
   * Execute update of an entity request
   */
  @SuppressWarnings("WeakerAccess")
  protected ENTITY internalExecutePOSTRequest(Long id, ENTITY formParam) {
    log.debug("Update entity with id {} and formParam {}", id, formParam);

    if (!isIdAccepted(id)) {
      log.debug("Id {} was not accepted to process", id);
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    ENTITY loadedEntityById = loadEntityById(id);
    if (null == loadedEntityById) {
      log.warn("Unable to load entity with ID {} in {}", id, getEntityRepository().getClass());
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    try {
      ENTITY entity = updateOrCreateEntityFromParameters(formParam, loadedEntityById);
      log.info("Successfully updated {}", entity);
      return entity;
    } catch (IllegalArgumentException ex) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }
  }

  /**
   * Override this to add additional checks for the id
   */
  @SuppressWarnings({"WeakerAccess", "BooleanMethodIsAlwaysInverted"})
  protected boolean isIdAccepted(@Nullable Long id) {
    return (null != id && id > 0L);
  }

  /**
   * Override this to interfere in the loading process (e.g. load different entities according to id
   * characteristics)
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected ENTITY loadEntityById(@Nullable Long id) {
    return getEntityRepository().findEntityById(id);
  }

  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected Boolean internalExecuteDELETERequest(@Nonnull Long id) {
    log.debug("Delete entity with id {}", id);
    ENTITY entity = loadEntityById(id);
    return this.getEntityRepository().delete(entity);
  }

  /**
   * Execute the GET Request for the given id/token
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  protected ENTITY internalGETRequest(@Nonnull Long id) {
    log.trace("Incoming request for id {}", id);

    if (!isIdAccepted(id)) {
      log.debug("Id {} was not accepted to process", id);
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    return loadEntityById(id);
  }

  /**
   * Process the GET List Request here.
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected List<ENTITY> internalGETListRequest(@Nonnull UriInfo info, int limit, int offset) {
    log.trace("Incoming LIST request");

    MultivaluedMap<String, String> pathParametersMap = info
        .getQueryParameters();

    if (!isRequiredParametersSet(pathParametersMap)) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    List<ENTITY> entityList = getAdditionalEntitiesForListRequest(pathParametersMap);
    if (limit < 0 || limit > entityList.size()) {
      entityList.addAll(this.getEntityRepository().list(
          pathParametersMap, limit - entityList.size(), offset));
    }

    return postProcessEntitiesCollectionBeforeReturn(entityList);
  }

  /**
   * Override this to enforce required parameters
   */
  @SuppressWarnings({"WeakerAccess", "unused", "SameReturnValue"})
  protected boolean isRequiredParametersSet(@Nonnull MultivaluedMap<String, String> parametersMap) {
    return true;
  }

  /**
   * Here you can add Entities, that should show up with this call, but wouldn't on the generic
   * method.
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @Nonnull
  protected List<ENTITY> getAdditionalEntitiesForListRequest(
      @Nonnull MultivaluedMap<String, String> pathParametersMap) {
    return new ArrayList<>();
  }

  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected List<ENTITY> postProcessEntitiesCollectionBeforeReturn(
      @Nonnull Collection<ENTITY> entityCollection) {
    return new ArrayList<>(entityCollection);
  }
}
