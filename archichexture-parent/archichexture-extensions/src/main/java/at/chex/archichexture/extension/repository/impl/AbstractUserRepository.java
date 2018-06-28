package at.chex.archichexture.extension.repository.impl;

import at.chex.archichexture.extension.model.User;
import at.chex.archichexture.extension.repository.UserRepository;
import at.chex.archichexture.repository.impl.AbstractBaseRepository;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
public abstract class AbstractUserRepository<USER extends User> extends
    AbstractBaseRepository<USER> implements UserRepository<USER> {

  private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int DEFAULT_TOKEN_SIZE = 64;
  private static final int DEFAULT_SALT_SIZE = 64;


  private Logger log = LoggerFactory.getLogger(AbstractUserRepository.class);

  @Override
  public USER findUserByToken(String token) {
    if (Strings.isNullOrEmpty(token)) {
      return null;
    }
    SimplePath<String> tokenPath = Expressions
        .path(String.class, getEntityPath(), User.FIELD_NAME_TOKEN);

    User user = query().selectFrom(getEntityPath()).where(tokenPath.eq(token)).fetchOne();
    if (null == user) {
      return null;
    }
    return findEntityById(user.getId());
  }

  @Override
  @SuppressWarnings("unchecked")
  public USER findUserBy(String username, String password) {
    if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
      return null;
    }

    SimplePath<String> usernamePath = Expressions
        .path(String.class, getEntityPath(), User.FIELD_NAME_USERNAME);

    USER user = query().selectFrom(getEntityPath()).where(usernamePath.eq(username)).fetchFirst();
    log.debug("lookup customer with username:{} returned {}", username, user);

    if (null == user || !validatePasswordFor(user, password)) {
      log.debug("User {} not found or password didn't match", user);
      return null;
    }

    if (Strings.isNullOrEmpty(user.getToken())) {
      String token;
      do {
        token = generateToken();
      } while (tokenExists(token));
      user.setToken(token);
      user.setTokenIssuingDate(new Date());
      getEntityManager().merge(user);
    }
    return findEntityById(user.getId());
  }

  private boolean tokenExists(@Nonnull String token) {
    SimplePath<String> tokenPath = Expressions
        .path(String.class, getEntityPath(), User.FIELD_NAME_TOKEN);

    return !query().selectOne().from(getEntityPath()).where(tokenPath.eq(token)).fetch().isEmpty();
  }

  @Override
  public boolean validatePasswordFor(USER user, String password) {
    Preconditions.checkNotNull(user);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(password));
    byte[] passwordSalt = user.getPasswordSalt();
    if (null != passwordSalt && passwordSalt.length == getPasswordSaltLength()) {
      String customerPassword = user.getPassword();
      try {
        String newPassword = getSha512HashedPassword(password, passwordSalt);
        log.trace("Customer Password: (DB) {} vs (NEW) {}", customerPassword, newPassword);
        return (!Strings.isNullOrEmpty(customerPassword) && customerPassword.equals(newPassword));
      } catch (UnsupportedEncodingException e) {
        log.warn(e.getLocalizedMessage(), e);
      }
    }
    return false;
  }

  @Override
  public USER overridePasswordFor(USER user, String newPassword) {
    Preconditions.checkNotNull(user);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(newPassword));

    try {
      user.setPasswordSalt(generateSalt());

      user
          .setPassword(getSha512HashedPassword(newPassword, user.getPasswordSalt()));
      return save(user);
    } catch (UnsupportedEncodingException e) {
      log.warn(e.getLocalizedMessage(), e);
    }
    return user;
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  @Nonnull
  private String getSha512HashedPassword(String passwordToHash, byte[] salt)
      throws UnsupportedEncodingException {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(salt);
      byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < bytes.length; i++) {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Password cannot be hashed or generated.");
  }

  @Nonnull
  private byte[] generateSalt() {
    final Random r = new SecureRandom();
    byte[] salt = new byte[getPasswordSaltLength()];
    r.nextBytes(salt);
    return salt;
  }

  /**
   * Override this if you require a less/more secure salt
   */
  @Nonnegative
  @SuppressWarnings("WeakerAccess")
  protected int getPasswordSaltLength() {
    return DEFAULT_SALT_SIZE;
  }

  /**
   * Override this to add/remove characters to/from the keyspace of the generated tokens
   */
  @Nonnull
  @SuppressWarnings("WeakerAccess")
  protected String getTokenCharacters() {
    return ALPHA_NUMERIC_STRING;
  }

  /**
   * Override this to change the length of the generated tokens
   */
  @Nonnegative
  @SuppressWarnings("WeakerAccess")
  protected int getTokenLength() {
    return DEFAULT_TOKEN_SIZE;
  }

  @Nonnull
  private String generateToken() {
    int count = getTokenLength();
    StringBuilder builder = new StringBuilder();
    while (count-- != 0) {
      int character = (int) (Math.random() * getTokenCharacters()
          .length());
      builder.append(getTokenCharacters().charAt(character));
    }
    return builder.toString();
  }

  @Override
  public USER create() {
    USER user = super.create();
    if (Strings.isNullOrEmpty(user.getToken())) {
      String token;
      do {
        token = generateToken();
      } while (tokenExists(token));
      user.setToken(token);
      user.setTokenIssuingDate(new Date());
    }
    return user;
  }
}
