package at.chex.archichexture.helpers.convert;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class BooleanConverter implements Converter<Boolean> {

  @Override
  public Boolean convert(String input) {
    return Boolean.valueOf(input);
  }
}
