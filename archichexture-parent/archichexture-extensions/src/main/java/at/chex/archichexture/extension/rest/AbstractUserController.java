package at.chex.archichexture.extension.rest;

import at.chex.archichexture.Defines;
import at.chex.archichexture.extension.model.User;
import at.chex.archichexture.extension.repository.UserRepository;
import at.chex.archichexture.rest.TokenBaseRestController;
import com.google.common.base.Strings;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * This is a convenience controller, that provides you with common REST methods for {@link User} handling like login, load {@link User} by access-token or password reset methods
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
public abstract class AbstractUserController<USER extends User> extends
    TokenBaseRestController<USER> {

  @Override
  protected abstract UserRepository<USER> getEntityRepository();

  /**
   * Login a user with it's username and password. This will also generate a new token, if none is available.
   */
  @POST
  @Path("/login")
  @Consumes(Defines.MEDIA_TYPE_X_WWW_FORM_ENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response loginUser(@QueryParam("username") String username,
      @QueryParam("password") String password) {
    if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    USER user = getEntityRepository().findUserBy(username, password);
    if (null == user) {
      return Response.status(Status.NOT_FOUND).build();
    }
    return Response.ok(user).build();
  }

  /**
   * Return the data of the user with the given token
   */
  @GET
  @Path("/self")
  @Produces(MediaType.APPLICATION_JSON)
  public Response selfData(@QueryParam(Defines.KEYWORD_TOKEN) String token) {
    if (Strings.isNullOrEmpty(token)) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    return Response.ok(getEntityRepository().findUserByToken(token)).build();
  }
}
