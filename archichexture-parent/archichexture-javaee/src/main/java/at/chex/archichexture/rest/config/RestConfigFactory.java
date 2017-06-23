package at.chex.archichexture.rest.config;

import org.aeonbits.owner.ConfigFactory;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 30/03/2017
 */
public class RestConfigFactory {

  private RestConfigFactory() {
  }

  public static RestConfig get() {
    return ConfigFactory.create(RestConfig.class);
  }
}
