package at.chex.archichexture.helpers.convert;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class IntegerConverter implements Converter<Integer> {

  @Override
  public Integer convert(String input) {
    return Integer.valueOf(input);
  }
}
