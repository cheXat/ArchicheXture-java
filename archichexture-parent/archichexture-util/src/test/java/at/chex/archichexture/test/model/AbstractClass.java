package at.chex.archichexture.test.model;

import at.chex.archichexture.annotation.Aspect;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 06.01.18
 */
public abstract class AbstractClass {

  public static final int INT_VALUE = 4;
  @Aspect
  private int privateInt = INT_VALUE;
}
