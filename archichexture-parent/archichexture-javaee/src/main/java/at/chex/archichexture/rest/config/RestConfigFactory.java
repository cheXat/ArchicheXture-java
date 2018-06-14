package at.chex.archichexture.rest.config;

import org.aeonbits.owner.ConfigFactory;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/03/2017
 */
@SuppressWarnings("WeakerAccess")
public class RestConfigFactory {

  private RestConfigFactory() {
  }

  /**
   * Load the config
   */
  @SuppressWarnings("unused")
  public static RestConfig get() {
    return ConfigFactory.create(RestConfig.class);
  }
}
