package at.chex.archichexture.helpers;

import at.chex.archichexture.helpers.convert.BigDecimalConverter;
import at.chex.archichexture.helpers.convert.BooleanConverter;
import at.chex.archichexture.helpers.convert.Converter;
import at.chex.archichexture.helpers.convert.DateConverter;
import at.chex.archichexture.helpers.convert.IntegerConverter;
import at.chex.archichexture.helpers.convert.LongConverter;
import at.chex.archichexture.helpers.convert.StringConverter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class Values {

  public static final String SPLIT_CHARACTER = ",";
  private static Values self = new Values();
  private Map<Class<?>, Converter<?>> converter = new HashMap<>();

  private Values() {
    converter.put(BigDecimal.class, new BigDecimalConverter());
    converter.put(Boolean.class, new BooleanConverter());
    converter.put(Date.class, new DateConverter());
    converter.put(Integer.class, new IntegerConverter());
    converter.put(Long.class, new LongConverter());
    converter.put(String.class, new StringConverter());
  }

  public static <TYPE> void registerConverter(Class<TYPE> clazz, Converter<TYPE> converter) {
    self.converter.put(clazz, converter);
  }

  public static Set<String> getValuesForKeys(Map<String, List<String>> map,
      Collection<String> keys) {
    Set<String> returnValues = new HashSet<>();
    for (String key : keys) {
      if (map.containsKey(key)) {
        List<String> strings = map.get(key);
        for (String value : strings) {
          returnValues.addAll(Arrays.asList(value.split(SPLIT_CHARACTER)));
        }
      }
    }
    return returnValues;
  }

  @SuppressWarnings("unchecked")
  public static <TYPE> TYPE convert(String object, Class<TYPE> clazz) {
    Converter<TYPE> converter = (Converter<TYPE>) self.converter.get(clazz);
    if (null != converter) {
      return converter.convert(object);
    }
    throw new RuntimeException("Unable to convert to class:" + clazz.getClass());
  }
}
