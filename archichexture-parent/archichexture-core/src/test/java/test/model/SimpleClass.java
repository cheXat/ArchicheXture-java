package test.model;

import at.chex.archichexture.annotation.Serialized;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 06.01.18
 */
@Serialized
public class SimpleClass {

  public int publicInt;
  public String publicString;
  public Long publicLong;
  private String privateString;
}
