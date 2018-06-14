package at.chex.archichexture.helpers.convert;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class LongConverter implements Converter<Long> {

  @Override
  public Long convert(String input) {
    return Long.valueOf(input);
  }
}
