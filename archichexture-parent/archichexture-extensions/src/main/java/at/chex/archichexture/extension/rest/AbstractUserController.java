package at.chex.archichexture.extension.rest;

import at.chex.archichexture.Defines;
import at.chex.archichexture.extension.model.User;
import at.chex.archichexture.extension.model.UsernameAndPassword;
import at.chex.archichexture.extension.repository.UserRepository;
import at.chex.archichexture.rest.TokenBaseRestController;
import com.google.common.base.Strings;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;

/**
 * This is a convenience controller, that provides you with common REST methods for {@link User} handling like login, load {@link User} by access-token or password reset methods
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
public abstract class AbstractUserController<USER extends User> extends
    TokenBaseRestController<USER> {

  @Inject
  private Logger log;

  @Override
  protected abstract UserRepository<USER> getEntityRepository();

  /**
   * Login a {@link User} with it's username and password. This will also generate a new token, if none is available.
   */
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public USER loginUser(UsernameAndPassword usernameAndPassword) {
    if (Strings.isNullOrEmpty(usernameAndPassword.getUsername()) || Strings
        .isNullOrEmpty(usernameAndPassword.getPassword())) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    USER user = getEntityRepository()
        .findUserBy(usernameAndPassword.getUsername(), usernameAndPassword.getPassword());
    if (null == user) {
      throw new WebApplicationException(HttpURLConnection.HTTP_FORBIDDEN);
    }
    return user;
  }

  /**
   * Return the data of the {@link User} with the given token
   */
  @GET
  @Path("/self")
  @Produces(MediaType.APPLICATION_JSON)
  public USER selfData(@QueryParam(Defines.KEYWORD_TOKEN) String token) {
    if (Strings.isNullOrEmpty(token)) {
      throw new WebApplicationException(HttpURLConnection.HTTP_FORBIDDEN);
    }
    return getEntityRepository().findUserByToken(token);
  }

  /**
   * Change the password of the {@link User} with the given token
   */
  @Path("/change-password")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public USER resetCustomerPassword(@QueryParam(Defines.KEYWORD_TOKEN) String token,
      UsernameAndPassword usernameAndPassword) {
    USER user = getEntityRepository().findUserByToken(token);
    if (null == user) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    if (Strings.isNullOrEmpty(usernameAndPassword.getPassword())) {
      throw new WebApplicationException(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    user = getEntityRepository().overridePasswordFor(user, usernameAndPassword.getPassword());
    log.info("Password change for User {} ({})!", user.getUsername(), user.getId());
    return user;
  }
}
