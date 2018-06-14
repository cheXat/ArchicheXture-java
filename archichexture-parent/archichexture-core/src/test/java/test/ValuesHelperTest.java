package test;

import at.chex.archichexture.helpers.Values;
import at.chex.archichexture.helpers.convert.Converter;
import at.chex.archichexture.helpers.convert.StringConverter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import test.model.TargetClass;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 06.01.18
 */
public class ValuesHelperTest {

  @Test
  public void convertBigDecimalTest() {
    BigDecimal convert = Values.convert("1", BigDecimal.class);

    Assert.assertNotNull(convert);
    Assert.assertEquals(convert.getClass(), BigDecimal.class);
    Assert.assertEquals(convert, BigDecimal.valueOf(1));
  }

  @Test
  public void convertBooleanTest() {
    Boolean convert = Values.convert("true", Boolean.class);

    Assert.assertNotNull(convert);
    Assert.assertEquals(convert.getClass(), Boolean.class);
    Assert.assertEquals(convert, Boolean.valueOf(true));
  }

  @Test
  public void convertDateTest() {
    Date today = new Date();
    Date convert = Values.convert(String.valueOf(today.getTime()), Date.class);

    Assert.assertNotNull(convert);
    Assert.assertEquals(convert.getClass(), Date.class);
    Assert.assertEquals(convert, today);
  }

  @Test
  public void convertIntegerTest() {
    Integer convert = Values.convert("1", Integer.class);

    Assert.assertNotNull(convert);
    Assert.assertEquals(convert.getClass(), Integer.class);
    Assert.assertEquals(convert, Integer.valueOf(1));
  }

  @Test
  public void convertLongTest() {
    Long convert = Values.convert("1", Long.class);

    Assert.assertNotNull(convert);
    Assert.assertEquals(convert.getClass(), Long.class);
    Assert.assertEquals(convert, Long.valueOf(1));
  }

  @Test
  public void convertStringTest() {
    String convert = Values.convert("1", String.class);

    Assert.assertNotNull(convert);
    Assert.assertEquals(convert.getClass(), String.class);
    Assert.assertEquals(convert, String.valueOf(1));
  }

  @Test
  public void toNullConverterTest() {
    Converter<String> toNullConverter = input -> null;

    Values.registerConverter(String.class, toNullConverter);

    String convert = Values.convert("1", String.class);

    Assert.assertNull(convert);

    Values.registerConverter(String.class, new StringConverter());
  }

  @Test(expected = RuntimeException.class)
  public void unknownConverterTest() {
    Values.convert("1", TargetClass.class);
  }

  @Test
  public void itemsFromListTest() {
    List<String> aList = new ArrayList<>();
    Assert.assertNotNull(aList);
    aList.add("a");
    aList.add("b");
    aList.add("c");
    aList.add("d1");
    Assert.assertEquals(aList.size(), 4);

    List<String> bList = new ArrayList<>();
    Assert.assertNotNull(bList);
    bList.add("a");
    bList.add("b");
    bList.add("c");
    bList.add("d2");
    Assert.assertEquals(bList.size(), 4);

    List<String> cList = new ArrayList<>();
    Assert.assertNotNull(cList);
    cList.add("a");
    cList.add("b");
    cList.add("c");
    cList.add("d3");
    Assert.assertEquals(cList.size(), 4);

    List<String> keysList = new ArrayList<>();
    Assert.assertNotNull(keysList);
    keysList.add("a");
    keysList.add("c");
    Assert.assertEquals(keysList.size(), 2);

    Map<String, List<String>> map = new HashMap<>();
    Assert.assertNotNull(map);
    map.put("a", aList);
    map.put("b", bList);
    map.put("c", cList);
    Assert.assertEquals(map.size(), 3);

    Set<String> valuesForKeys = Values.getValuesForKeys(map, keysList);
    Assert.assertNotNull(valuesForKeys);
    Assert.assertEquals(valuesForKeys.size(), 5);
  }
}
