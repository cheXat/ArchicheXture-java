package at.chex.archichexture.rest;

import at.chex.archichexture.dto.BaseDto;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.rest.config.RestConfig;
import at.chex.archichexture.rest.config.RestConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is the main class to inherit from, when building web controllers. We suggest to use the implementations ( {@link TokenBaseRestController}, {@link } ) provided, that already inherit from here.
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

    protected boolean isInitialized() {
        return initialized;
    }

    public void init() {
        this.restConfig = RestConfigFactory.get();
        this.initialized = true;
    }

    protected abstract BaseRepository<ENTITY> getEntityRepository();

    protected Collection<BaseDto<ENTITY>> postProcessEntitiesCollectionBeforeReturn(Collection<BaseDto<ENTITY>> entityCollection) {
        return entityCollection;
    }

    protected abstract ENTITY updateOrCreateEntityFromParameters(
            DTO formObject, ENTITY entity) throws IllegalArgumentException;

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
     * Override this to add additional checks for the id
     *
     * @param id
     * @return
     */
    protected boolean isIdAccepted(Long id) {
        return (null != id && id > 0L);
    }

    /**
     * Override this to interfere with entity creation
     *
     * @return
     */
    protected ENTITY createNewEntity() {
        return getEntityRepository().create();
    }

    protected Response internalExecutePOSTRequest(Long id, String token, DTO formParam) {
        log.debug("Update entity with id {} and formParam {}", id, formParam);

        if (!isIdAccepted(id)) {
            log.debug("Id {} was not accepted to process", id);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }

        ENTITY loadedEntityById = loadEntityById(id, token);
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

    protected Response internalExecuteDELETERequest(Long id, String token) {
        log.debug("Delete entity with id {}", id);
        ENTITY entity = loadEntityById(id, token);
        if (this.getEntityRepository().delete(entity)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    /**
     * Override this to interfere in the loading process (e.g. load different entities according to id characteristics)
     *
     * @param id
     * @return
     */
    protected ENTITY loadEntityById(Long id, String token) {
        return getEntityRepository().findEntityById(id);
    }

    /**
     * Transform the given {@link ENTITY} to the corresponding Dto before
     * returning it after the webservice call.
     *
     * @param entity
     * @return
     */
    protected abstract BaseDto<ENTITY> transformToDto(ENTITY entity);

    protected List<BaseDto<ENTITY>> transformToDto(List<ENTITY> entityList) {
        List<BaseDto<ENTITY>> returnList = new ArrayList<BaseDto<ENTITY>>();
        for (ENTITY e : entityList) {
            BaseDto<ENTITY> entityBaseDto = transformToDto(e);
            if (null != entityBaseDto) {
                returnList.add(entityBaseDto);
            }
        }
        return returnList;
    }

    protected Long handleIdBeforeProcessing(Long id) {
        return id;
    }

    protected List<ENTITY> getAdditionalEntitiesForListRequest(
            MultivaluedMap<String, String> pathParametersMap) {
        return new ArrayList<ENTITY>();
    }

    protected Response internalGETRequest(Long id, String token) {
        log.trace("Incoming request for id {}", id);
        ENTITY entity = loadEntityById(id, token);
        return Response.ok(transformToDto(entity)).build();
    }

    /**
     * Override this to enforce required parameters
     *
     * @param parametersMap
     * @return
     */
    protected boolean isRequiredParametersSet(MultivaluedMap<String, String> parametersMap) {
        return true;
    }

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

        return Response.ok(postProcessEntitiesCollectionBeforeReturn(transformToDto(entityList))).build();
    }
}
