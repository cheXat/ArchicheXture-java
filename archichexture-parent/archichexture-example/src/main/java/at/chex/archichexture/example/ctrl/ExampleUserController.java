package at.chex.archichexture.example.ctrl;

import at.chex.archichexture.example.model.ExampleUser;
import at.chex.archichexture.example.repository.ExampleUserRepository;
import at.chex.archichexture.extension.rest.AbstractUserController;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * This is a fully functional REST Controller for the {@link ExampleUser} class.
 * PUT:/users with a JSON as payload will create a new {@link ExampleUser}
 * POST:/users/{id} with a JSON as payload will update the existing {@link ExampleUser}
 * GET:/users will return a paged list of all {@link ExampleUser}s in the database
 * GET:/users/{id} will return the {@link ExampleUser} with the given id
 * DELETE:/users/{id} will delete the {@link ExampleUser} with the given id
 *
 * Additionally, {@link at.chex.archichexture.extension.repository.UserRepository} provides methods for userhandling and passwords:
 * GET:/users/login with x-www-form-urlencoded username and password will return that user alongside a valid access-token, if exists
 * GET:/users/self?token={access-token} will return the {@link ExampleUser} with the given access-token
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@Path("/users")
public class ExampleUserController extends AbstractUserController<ExampleUser> {

  @Inject
  private ExampleUserRepository exampleUserRepository;

  /**
   * Every incoming request is being checked for a valid {@link at.chex.archichexture.extension.model.User} with the given token
   */
  @PostConstruct
  public void init() {
    super.init(
        // if there is a User with the given token, that's enough for us to let him through
        (token, resetTokenExpiration) -> null != exampleUserRepository.findUserByToken(token),
        // set this to false to get a read/write controller. true is readonly (default)
        false);
  }

  @Override
  protected ExampleUserRepository getEntityRepository() {
    return exampleUserRepository;
  }
}
