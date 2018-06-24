package at.chex.archichexture.slh;

import at.chex.archichexture.slh.convert.BigDecimalConverter;
import at.chex.archichexture.slh.convert.BooleanConverter;
import at.chex.archichexture.slh.convert.Converter;
import at.chex.archichexture.slh.convert.DateConverter;
import at.chex.archichexture.slh.convert.IntegerConverter;
import at.chex.archichexture.slh.convert.LongConverter;
import at.chex.archichexture.slh.convert.StringConverter;
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

  @SuppressWarnings("WeakerAccess")
  public static final String SPLIT_CHARACTER = ",";
  private static final Values self = new Values();
  private final Map<Class<?>, Converter<?>> converter = new HashMap<>();

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
