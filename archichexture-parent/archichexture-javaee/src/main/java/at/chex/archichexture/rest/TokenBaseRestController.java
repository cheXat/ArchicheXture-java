package at.chex.archichexture.rest;

import at.chex.archichexture.dto.BaseDto;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.rest.token.TokenCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 27/03/2017
 */
public abstract class TokenBaseRestController<ENTITY extends BaseEntity, DTO extends BaseDto<ENTITY>> extends BaseRestController<ENTITY, DTO> {
    private static final Logger log = LoggerFactory.getLogger(BaseRestController.class);
    private TokenCheck tokenCheck;
    private boolean readonlyController = true;

    public void init(TokenCheck tokenCheck) {
        this.init(tokenCheck, true);
    }

    public void init(TokenCheck tokenCheck, boolean readonlyController) {
        super.init();
        this.tokenCheck = tokenCheck;
        this.readonlyController = readonlyController;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeGetListRequest(
            @Context UriInfo info,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        int tokenStatusResponseCode = tokenCheck.getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalGETListRequest(info, limit, offset);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeGetRequest(
            @PathParam("id") Long id,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        int tokenStatusResponseCode = tokenCheck.getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }
        return internalGETRequest(id, token);
    }

    @POST
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executePUTRequest(
            @BeanParam DTO formParam,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = tokenCheck.getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalExecutePUTRequest(formParam);
    }

    @POST
    @Path("/{id}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executePOSTRequest(
            @PathParam("id") Long id,
            @BeanParam DTO formParam,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = tokenCheck.getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalExecutePOSTRequest(id, token, formParam);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeDELETERequest(
            @PathParam("id") Long id,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = tokenCheck.getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalExecuteDELETERequest(id, token);
    }
}
