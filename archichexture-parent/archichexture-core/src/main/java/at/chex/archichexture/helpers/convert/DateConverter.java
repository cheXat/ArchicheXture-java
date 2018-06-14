package at.chex.archichexture.helpers.convert;

import java.util.Date;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class DateConverter implements Converter<Date> {

  @Override
  public Date convert(String input) {
    return new Date(Long.valueOf(input));
  }
}
