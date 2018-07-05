package at.chex.archichexture.extension.repository;

import at.chex.archichexture.extension.model.User;
import at.chex.archichexture.repository.BaseRepository;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
public interface UserRepository<USER extends User> extends BaseRepository<USER> {

  /**
   * Find a USER by its access-token
   */
  USER findUserByToken(String token);

  boolean userExists(String username);

  /**
   * Find a USER by its username and password. This will also generate a token, if there is none.
   */
  USER findUserBy(String username, String password);

  /**
   * Check if the given user is saved with the given password
   */
  boolean validatePasswordFor(USER user, String password);

  /**
   * Overwrite the current password for the given user
   */
  @SuppressWarnings("unused")
  USER overridePasswordFor(USER user, String newPassword);
}
