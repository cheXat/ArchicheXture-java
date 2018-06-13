package at.chex.archichexture.extension.token;

import at.chex.archichexture.token.TokenCheck;
import com.google.common.base.Strings;
import javax.ws.rs.core.Response.Status;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 31.01.18
 */
public class NotNullOrEmptyTokenCheck implements TokenCheck {

  @Override
  public int getTokenResponseCode(String token, boolean resetTokenExpiration) {
    if (Strings.isNullOrEmpty(token)) {
      return Status.FORBIDDEN.getStatusCode();
    }
    return 0;
  }
}
