package at.chex.archichexture.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines how this {@link Class} will be serialized by ArchicheXture
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 23.06.18
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Serialized {

  String DEFAULT_ID_APPENDIX = "_id";

  /**
   * When exposing ids, this will be added to the Exposed name
   */
  String idAppendix() default DEFAULT_ID_APPENDIX;

  /**
   * Defines the handling of nested objects
   */
  ExposureType exposeNested() default ExposureType.BOTH;

  enum ExposureType {
    /**
     * Only the ID (exposed name with appended idAppendix)
     */
    ID,
    /**
     * The full object will be serialized
     */
    FULL,
    /**
     * the full object AND the id alongside
     */
    BOTH
  }
}
