package at.chex.archichexture.rest;

import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.helpers.Reflection;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.rest.config.RestConfig;
import at.chex.archichexture.rest.config.RestConfigFactory;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
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
  private RestConfig restConfig = RestConfigFactory.get();

  protected RestConfig getConfig() {
    return this.restConfig;
  }

  /**
   * Execute PUT
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected Response internalExecutePUTRequest(@Nonnull ENTITY formParam) {
    log.debug("Create new entity for {}", formParam);
    try {
      ENTITY entity = updateOrCreateEntityFromParameters(formParam, createNewEntity());
      log.info("Successfully created {}", entity);
      return Response.ok(
          entity)
          .build();
    } catch (IllegalArgumentException ex) {
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }
  }

  @Nonnull
  private ENTITY updateOrCreateEntityFromParameters(
      @Nonnull ENTITY formObject, @Nonnull ENTITY entity) throws IllegalArgumentException {
    ENTITY entityWithValues = Reflection.transferValuesFromLeftToRight(formObject, entity);
    log.debug("Entity after transfer is {}", entityWithValues);
    return getEntityRepository().save(updateAdditionalParameters(formObject, entityWithValues));
  }

  /**
   * Values are directly updated to the {@link ENTITY}, but if you need something additional (e.g. unwrap given ids to value objects in the {@link ENTITY}), this is the place for it.
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @Nonnull
  protected ENTITY updateAdditionalParameters(@Nonnull ENTITY ENTITY,
      @Nonnull ENTITY entity) {
    return entity;
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
  protected Response internalExecutePOSTRequest(Long id, ENTITY formParam) {
    log.debug("Update entity with id {} and formParam {}", id, formParam);

    if (!isIdAccepted(id)) {
      log.debug("Id {} was not accepted to process", id);
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }

    ENTITY loadedEntityById = loadEntityById(id);
    if (null == loadedEntityById) {
      log.warn("Unable to load entity with ID {} in {}", id, getEntityRepository().getClass());
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }

    try {
      ENTITY entity = updateOrCreateEntityFromParameters(formParam, loadedEntityById);
      log.info("Successfully updated {}", entity);
      return Response.ok(
          entity).build();
    } catch (IllegalArgumentException ex) {
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }
  }

  /**
   * Override this to add additional checks for the id
   */
  @SuppressWarnings("WeakerAccess")
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
  protected Response internalExecuteDELETERequest(@Nonnull Long id) {
    log.debug("Delete entity with id {}", id);
    ENTITY entity = loadEntityById(id);
    if (this.getEntityRepository().delete(entity)) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.PRECONDITION_FAILED).build();
  }

  /**
   * Execute the GET Request for the given id/token
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected Response internalGETRequest(@Nonnull Long id) {
    log.trace("Incoming request for id {}", id);

    if (!isIdAccepted(id)) {
      log.debug("Id {} was not accepted to process", id);
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }

    ENTITY entity = loadEntityById(id);
    return Response.ok(null == entity ? null : transformToJsonObject(entity)).build();
  }

  /**
   * Process the GET List Request here.
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected Response internalGETListRequest(@Nonnull UriInfo info, int limit, int offset) {
    log.trace("Incoming LIST request");

    MultivaluedMap<String, String> pathParametersMap = info
        .getQueryParameters();

    if (!isRequiredParametersSet(pathParametersMap)) {
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }

    List<ENTITY> entityList = getAdditionalEntitiesForListRequest(pathParametersMap);
    if (limit < 0 || limit > entityList.size()) {
      entityList.addAll(this.getEntityRepository().list(
          pathParametersMap, limit - entityList.size(), offset));
    }

    return Response.ok(postProcessEntitiesCollectionBeforeReturn(transformToJsonObject(entityList)))
        .build();
  }

  /**
   * Override this to enforce required parameters
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
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
    return new ArrayList<ENTITY>();
  }

  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected Collection<JsonObject> postProcessEntitiesCollectionBeforeReturn(
      @Nonnull Collection<JsonObject> entityCollection) {
    return entityCollection;
  }

  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected List<JsonObject> transformToJsonObject(@Nonnull List<ENTITY> entityList) {
    List<JsonObject> returnList = new ArrayList<>();
    for (ENTITY e : entityList) {
      JsonObject entityBaseJsonObject = null == e ? null : transformToJsonObject(e);
      if (null != entityBaseJsonObject) {
        returnList.add(entityBaseJsonObject);
      }
    }
    return returnList;
  }

  /**
   * Transform the given {@link ENTITY} to the corresponding JsonObject before
   * returning it after the webservice call.
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected JsonObject transformToJsonObject(@Nonnull ENTITY entity) {
    JsonObject jsonObject = new JsonObject();

    List<Field> annotatedFields = Reflection.getAnnotatedFields(Exposed.class, entity.getClass());
    for (Field field : annotatedFields) {
      Exposed annotation = field.getAnnotation(Exposed.class);
      String key = Strings.isNullOrEmpty(annotation.exposedName()) ? field.getName()
          : annotation.exposedName();
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      try {
        Object o = field.get(entity);
        jsonObject.addProperty(key, String.valueOf(o));
      } catch (IllegalAccessException e) {
        log.warn("Unable to access entity field!", e);
      }
    }

    return jsonObject;
  }
}
