package at.chex.archichexture.test;

import at.chex.archichexture.helpers.Reflection;
import at.chex.archichexture.test.model.SimpleClass;
import at.chex.archichexture.test.model.TargetClass;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 06.01.18
 */
public class ReflectionHelperTest {

  @Test
  public void simpleCopyTest() {
    String stringValue = "abc DEF 123 ..-ä#";
    int intValue = 1337;
    Long longValue = 42L;

    SimpleClass sc = new SimpleClass();
    Assert.assertNotNull(sc);
    sc.publicString = stringValue;
    Assert.assertEquals(sc.publicString, sc.publicString);

    sc.publicInt = intValue;
    Assert.assertEquals(sc.publicInt, sc.publicInt);

    sc.publicLong = longValue;
    Assert.assertEquals(sc.publicLong, sc.publicLong);

    TargetClass tc = new TargetClass();
    Assert.assertNotNull(tc);

    Reflection.transferValuesFromLeftToRight(sc, tc);

    Assert.assertEquals("Int is Aspect -> copy", sc.publicInt, tc.publicInt);
    Assert.assertEquals("String is Aspect -> copy", sc.publicString, tc.publicString);
    Assert.assertNotEquals("Long is no Aspect -> do not copy", sc.publicLong, tc.publicLong);
  }
}
