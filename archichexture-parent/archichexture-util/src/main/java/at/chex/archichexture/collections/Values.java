package at.chex.archichexture.collections;

import java.util.Arrays;
import java.util.Collection;
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

  private Values() {
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
}
