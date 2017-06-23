package at.chex.archichexture.rest;

import at.chex.archichexture.dto.BaseDto;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.rest.config.RestConfig;
import at.chex.archichexture.rest.config.RestConfigFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 24/03/2017
 */
public abstract class BaseRestController<ENTITY extends BaseEntity, DTO extends BaseDto<ENTITY>>
    implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(BaseRestController.class);
  private boolean initialized = false;
  private RestConfig restConfig;

  protected RestConfig getConfig() {
    return this.restConfig;
  }

  /**
   * If this is false, try #init() before
   */
  protected boolean isInitialized() {
    return initialized;
  }

  /**
   * Make sure to initialize any controller with this method before using it
   */
  public void init() {
    this.restConfig = RestConfigFactory.get();
    this.initialized = true;
  }

  /**
   * Do whatever checks you like here BEFORE this {@link ENTITY} is manipulated. This will be
   * executed before anything is changed.
   */
  protected boolean isCanManipulateEntity(ENTITY entity) {
    return true;
  }

  /**
   * Execute PUT
   */
  protected Response internalExecutePUTRequest(DTO formParam) {
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

  /**
   * Map all values, you need from the {@link DTO} to the {@link ENTITY} and save it afterwards!
   */
  protected abstract ENTITY updateOrCreateEntityFromParameters(
      DTO formObject, ENTITY entity) throws IllegalArgumentException;

  /**
   * Override this to interfere with entity creation
   */
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
  protected Response internalExecutePOSTRequest(Long id, DTO formParam) {
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
  protected boolean isIdAccepted(Long id) {
    return (null != id && id > 0L);
  }

  /**
   * Override this to interfere in the loading process (e.g. load different entities according to id
   * characteristics)
   */
  protected ENTITY loadEntityById(Long id) {
    return getEntityRepository().findEntityById(id);
  }

  protected Response internalExecuteDELETERequest(Long id) {
    log.debug("Delete entity with id {}", id);
    ENTITY entity = loadEntityById(id);
    if (this.getEntityRepository().delete(entity)) {
      return Response.ok().build();
    }
    return Response.status(Response.Status.PRECONDITION_FAILED).build();
  }

  protected Long handleIdBeforeProcessing(Long id) {
    return id;
  }

  /**
   * Execute the GET Request for the given id/token
   */
  protected Response internalGETRequest(Long id) {
    log.trace("Incoming request for id {}", id);

    if (!isIdAccepted(id)) {
      log.debug("Id {} was not accepted to process", id);
      return Response.status(Response.Status.EXPECTATION_FAILED).build();
    }

    ENTITY entity = loadEntityById(id);
    return Response.ok(transformToDto(entity)).build();
  }

  /**
   * Transform the given {@link ENTITY} to the corresponding Dto before
   * returning it after the webservice call.
   */
  protected abstract DTO transformToDto(ENTITY entity);

  /**
   * Process the GET List Request here.
   */
  protected Response internalGETListRequest(UriInfo info, int limit, int offset) {
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

    return Response.ok(postProcessEntitiesCollectionBeforeReturn(transformToDto(entityList)))
        .build();
  }

  /**
   * Override this to enforce required parameters
   */
  protected boolean isRequiredParametersSet(MultivaluedMap<String, String> parametersMap) {
    return true;
  }

  /**
   * Here you can add Entities, that should show up with this call, but wouldn't on the generic
   * method.
   */
  protected List<ENTITY> getAdditionalEntitiesForListRequest(
      MultivaluedMap<String, String> pathParametersMap) {
    return new ArrayList<ENTITY>();
  }

  protected Collection<DTO> postProcessEntitiesCollectionBeforeReturn(
      Collection<DTO> entityCollection) {
    return entityCollection;
  }

  protected List<DTO> transformToDto(List<ENTITY> entityList) {
    List<DTO> returnList = new ArrayList<DTO>();
    for (ENTITY e : entityList) {
      DTO entityBaseDto = transformToDto(e);
      if (null != entityBaseDto) {
        returnList.add(entityBaseDto);
      }
    }
    return returnList;
  }
}
