package at.chex.archichexture.rest;

import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.token.TokenCheck;
import com.google.common.base.Strings;
import java.net.HttpURLConnection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

  private static final Logger log = LoggerFactory.getLogger(TokenBaseRestController.class);
  private TokenCheck tokenCheck = (token, resetTokenExpiration) -> true;
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
  public List<ENTITY> executeGetListRequest(
      @Context UriInfo info,
      @DefaultValue("50") @QueryParam("limit") int limit,
      @DefaultValue("0") @QueryParam("offset") int offset,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String queryToken,
      @HeaderParam(value = "token") String headerToken) {

    log.trace("GET:/ with Parameters. reset_token:{}, limit:{}, offset:{}",
        resetTokenTimes, limit, offset);

    return internalGETListRequest(info,
        Strings.isNullOrEmpty(headerToken) ? queryToken : headerToken, resetTokenTimes, limit,
        offset);
  }

  /**
   * This returns the correct HTTP:ResponseCode. If 0, this is interpreted as "everything's ok". Any
   * other value will be returned as the HTTP:Result.
   */
  @SuppressWarnings("WeakerAccess")
  protected void tokenCheck(String token, boolean resetTokenExpiration) {
    log.debug("Checking validity of token:{}", token);
    if (!tokenCheck.isTokenValid(token, resetTokenExpiration)) {
      throw new WebApplicationException(HttpURLConnection.HTTP_FORBIDDEN);
    }
  }

  /**
   * Process the GET List Request here.
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  protected List<ENTITY> internalGETListRequest(UriInfo info, String token, int limit, int offset) {
    return internalGETListRequest(info, token, true, limit, offset);
  }

  /**
   * Process the GET List Request here.
   */
  @SuppressWarnings("WeakerAccess")
  protected List<ENTITY> internalGETListRequest(UriInfo info, String token, boolean resetTokenTimes,
      int limit, int offset) {
    tokenCheck(token, resetTokenTimes);

    log.trace("Incoming LIST request for token {}", token);

    return super.internalGETListRequest(info, limit, offset);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ENTITY executeGetRequest(
      @PathParam("id") Long id,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String queryToken,
      @HeaderParam(value = "token") String headerToken) {

    log.trace("GET:/id with Parameters. id:{}, reset_token:{}", id, resetTokenTimes);

    return internalGETRequest(id, Strings.isNullOrEmpty(headerToken) ? queryToken : headerToken,
        resetTokenTimes);
  }

  /**
   * Execute the GET Request for the given id/token
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  protected ENTITY internalGETRequest(Long id, String token) {
    return internalGETRequest(id, token, true);
  }

  /**
   * Execute the GET Request for the given id/token
   */
  @SuppressWarnings("WeakerAccess")
  protected ENTITY internalGETRequest(Long id, String token, boolean resetTokenTimes) {
    tokenCheck(token, resetTokenTimes);

    log.trace("Incoming request for id {} with token {}", id, token);
    return super.internalGETRequest(id);
  }

  @PUT
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public ENTITY executePUTRequest(
      ENTITY formParam,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String queryToken,
      @HeaderParam(value = "token") String headerToken) {
    log.trace("PUT:/ (create) with Parameters. dto:{}, reset_token:{}", formParam,
        resetTokenTimes);

    return internalExecutePUTRequest(Strings.isNullOrEmpty(headerToken) ? queryToken : headerToken,
        resetTokenTimes, formParam);
  }

  /**
   * Execute PUT
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  protected ENTITY internalExecutePUTRequest(String token, boolean resetTokenTimes,
      @Nonnull ENTITY formParam) {
    if (this.isReadonlyController()) {
      log.warn("Tried to PUT on a readonly controller!");
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    tokenCheck(token, resetTokenTimes);

    log.debug("Create entity for token {}. FormParams: {}", token, formParam);
    return super.internalExecutePUTRequest(formParam);
  }


  @POST
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public ENTITY executePOSTRequest(
      @PathParam("id") Long id,
      ENTITY formParam,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String queryToken,
      @HeaderParam(value = "token") String headerToken) {
    log.trace("POST:/id with Parameters. id:{}, dto:{}, reset_token:{}", id, formParam,
        resetTokenTimes);

    return internalExecutePOSTRequest(id,
        Strings.isNullOrEmpty(headerToken) ? queryToken : headerToken, resetTokenTimes, formParam);
  }

  @SuppressWarnings({"WeakerAccess", "unused"})
  protected ENTITY internalExecutePOSTRequest(Long id, String token, ENTITY formParam) {
    return internalExecutePOSTRequest(id, token, true, formParam);
  }

  /**
   * Process the update-entity event
   */
  @SuppressWarnings("WeakerAccess")
  protected ENTITY internalExecutePOSTRequest(Long id, String token, boolean resetTokenTimes,
      ENTITY formParam) {
    if (this.isReadonlyController()) {
      log.warn("Tried to POST on a readonly controller!");
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    tokenCheck(token, resetTokenTimes);

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
  public Boolean executeDELETERequest(
      @PathParam("id") Long id,
      @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
      @QueryParam(value = "token") String queryToken,
      @HeaderParam(value = "token") String headerToken) {

    if (this.isReadonlyController()) {
      log.warn("Tried to DELETE on a readonly controller!");
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    return internalExecuteDELETERequest(id,
        Strings.isNullOrEmpty(headerToken) ? queryToken : headerToken, resetTokenTimes);
  }

  /**
   * Delete the entity with the given id
   */
  @SuppressWarnings("WeakerAccess")
  protected Boolean internalExecuteDELETERequest(Long id, String token, boolean resetTokenTimes) {
    if (this.readonlyController) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    tokenCheck(token, resetTokenTimes);

    log.debug("Delete entity with id {} for token {}", id, token);
    return super.internalExecuteDELETERequest(id);
  }
}
