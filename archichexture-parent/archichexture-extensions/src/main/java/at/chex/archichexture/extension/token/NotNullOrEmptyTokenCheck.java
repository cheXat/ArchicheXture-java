package at.chex.archichexture.extension.token;

import at.chex.archichexture.token.TokenCheck;
import com.google.common.base.Strings;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 31.01.18
 */
public class NotNullOrEmptyTokenCheck implements TokenCheck {

  @Override
  public boolean isTokenValid(String token, boolean resetTokenExpiration) {
    return !Strings.isNullOrEmpty(token);
  }
}
