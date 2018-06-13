package at.chex.archichexture.annotation;

import static at.chex.archichexture.annotation.Exposed.ExposureType.PUBLIC;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 31.01.18
 */
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exposed {

  /**
   * Is this an aspect, that can be filtered? Can i ask the webservice for entities with this attribute?
   */
  ExposureType exposure() default PUBLIC;

  String exposedName() default "";

  enum ExposureType {
    /**
     * This Field will not be published in the DTO
     */
    HIDDEN,
    /**
     * This Field will be send in the DTO
     */
    PUBLIC
  }
}
