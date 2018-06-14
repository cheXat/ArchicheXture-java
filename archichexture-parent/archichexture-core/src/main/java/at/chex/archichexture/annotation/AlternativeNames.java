package at.chex.archichexture.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlternativeNames {

  /**
   * Additional names, this field can be filtered with. E.g. i defined that attribute to be named "datetime" but my legacy client filters for "date".
   * The actual fieldname is always included in the attribute list!
   */
  String[] value();
}
