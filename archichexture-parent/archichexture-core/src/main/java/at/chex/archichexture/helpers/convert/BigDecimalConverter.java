package at.chex.archichexture.helpers.convert;

import java.math.BigDecimal;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

  @Override
  public BigDecimal convert(String input) {
    return new BigDecimal(input);
  }
}
