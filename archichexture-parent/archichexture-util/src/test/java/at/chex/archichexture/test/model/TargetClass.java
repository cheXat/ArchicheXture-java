package at.chex.archichexture.test.model;

import at.chex.archichexture.annotation.Aspect;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 06.01.18
 */
public class TargetClass {

  @Aspect
  public int publicInt;
  @Aspect
  public String publicString;
  public Long publicLong;
  private String privateString;
}