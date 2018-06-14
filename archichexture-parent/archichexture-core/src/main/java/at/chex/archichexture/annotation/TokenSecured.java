package at.chex.archichexture.annotation;

import at.chex.archichexture.Defines;
import at.chex.archichexture.token.TokenCheck;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 31.01.18
 */
public @interface TokenSecured {

  @SuppressWarnings("unused")
  String value() default Defines.KEYWORD_TOKEN;

  @SuppressWarnings("unused")
  Class<? extends TokenCheck> checkClass();
}
