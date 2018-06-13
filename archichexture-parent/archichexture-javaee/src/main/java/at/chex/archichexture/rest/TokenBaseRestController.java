package at.chex.archichexture.rest;

import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.token.TokenCheck;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/03/2017
 */
public abstract class TokenBaseRestController<ENTITY extends BaseEntity> extends
    BaseRestController<ENTITY> {

  private static final Logger log = LoggerFactory.getLogger(BaseRestController.class);
  private TokenCheck tokenCheck = (token, resetTokenExpiration) -> 0;
  private boolean readonlyController = true;

  /**
   * Ensure, that one of the init methods is called before handling any requests
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  public void init(TokenCheck tokenCheck) {
    this.init(tokenCheck, true);
  }

  /**
   * CAUTION: Ensure, that one of the init methods is called before handling any requests
   */
  @SuppressWarnings("WeakerAccess")
  public void init(TokenCheck tokenCheck, boolean readonlyController) {
    if (null != tokenCheck) {
      this.tokenCheck = tokenCheck;
    }
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

    log.trace("GET:/ with Parameters. reset_token:{}, token:{}, limit:{}, offset:{}",
        resetTokenTimes, token, limit, offset);

    int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
    if (tokenStatusResponseCode > 0) {
      return Response.status(tokenStatusResponseCode).build();
    }

    return internalGETListRequest(info, token, limit, offset);
  }

  /**
   * This returns the correct HTTP:ResponseCode. If 0, this is interpreted as "everything's ok". Any
   * other value will be returned as the HTTP:Result.
   */
  @SuppressWarnings("WeakerAccess")
  protected int getTokenResponseCode(String token, boolean resetTokenExpiration) {
    log.debug("Checking validity of token:{}", token);
    return tokenCheck.getTokenResponseCode(token, resetTokenExpiration);
  }

  /**
   * Process the GET List Request here.
   */
  @SuppressWarnings("WeakerAccess")
  protected Response internalGETListRequest(UriInfo info, String token, int limit, int offset) {
    log.trace("Incoming LIST request for token {}", token);

    return super.internalGETListRequest(info, limit, offset);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response executeGetRequest(
      @PathParam("id") Long id,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String token) {

    log.trace("GET:/id with Parameters. id:{}, reset_token:{}, token:{}", id, resetTokenTimes,
        token);

    int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
    if (tokenStatusResponseCode > 0) {
      return Response.status(tokenStatusResponseCode).build();
    }
    return internalGETRequest(id, token);
  }

  /**
   * Execute the GET Request for the given id/token
   */
  @SuppressWarnings("WeakerAccess")
  protected Response internalGETRequest(Long id, String token) {
    log.trace("Incoming request for id {} with token {}", id, token);
    return super.internalGETRequest(id);
  }

  @PUT
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response executePUTRequest(
      ENTITY formParam,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String token) {
    if (this.isReadonlyController()) {
      log.warn("Tried to PUT on a readonly controller!");
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    log.trace("PUT:/ (create) with Parameters. dto:{}, reset_token:{}, token:{}", formParam,
        resetTokenTimes, token);

    int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
    if (tokenStatusResponseCode > 0) {
      return Response.status(tokenStatusResponseCode).build();
    }

    return internalExecutePUTRequest(formParam);
  }

  @POST
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response executePOSTRequest(
      @PathParam("id") Long id,
      @BeanParam ENTITY formParam,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String token) {

    if (this.isReadonlyController()) {
      log.warn("Tried to POST on a readonly controller!");
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    log.trace("POST:/id with Parameters. id:{}, dto:{}, reset_token:{}, token:{}", id, formParam,
        resetTokenTimes, token);

    int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
    if (tokenStatusResponseCode > 0) {
      return Response.status(tokenStatusResponseCode).build();
    }

    return internalExecutePOSTRequest(id, token, formParam);
  }

  /**
   * Process the update-entity event
   */
  @SuppressWarnings("WeakerAccess")
  protected Response internalExecutePOSTRequest(Long id, String token, ENTITY formParam) {
    log.debug("Update entity with id {} for token {}. FormParams: {}", id, token, formParam);
    return super.internalExecutePOSTRequest(id, formParam);
  }

  @SuppressWarnings("WeakerAccess")
  protected boolean isReadonlyController() {
    return this.readonlyController;
  }

  @DELETE
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response executeDELETERequest(
      @PathParam("id") Long id,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String token) {

    if (this.isReadonlyController()) {
      log.warn("Tried to DELETE on a readonly controller!");
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
    if (tokenStatusResponseCode > 0) {
      return Response.status(tokenStatusResponseCode).build();
    }

    return internalExecuteDELETERequest(id, token, resetTokenTimes);
  }

  /**
   * Delete the entity with the given id
   */
  @SuppressWarnings("WeakerAccess")
  protected Response internalExecuteDELETERequest(Long id, String token, boolean resetTokenTimes) {
    if (this.readonlyController) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
    if (tokenStatusResponseCode > 0) {
      return Response.status(tokenStatusResponseCode).build();
    }

    log.debug("Delete entity with id {} for token {}", id, token);
    return super.internalExecuteDELETERequest(id);
  }

  /**
   * Override this to interfere in the loading process (e.g. load different entities according to id
   * characteristics)
   *
   * @deprecated has never been used. Remove!
   */
  @Deprecated
  protected ENTITY loadEntityById(Long id, String token) {
    int tokenStatusResponseCode = getTokenResponseCode(token, false);
    if (tokenStatusResponseCode > 0) {
      return null;
    }
    return super.loadEntityById(id);
  }
}
