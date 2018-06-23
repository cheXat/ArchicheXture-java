package at.chex.archichexture.annotation;

import static at.chex.archichexture.annotation.Exposed.Visibility.PUBLIC;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This changes the way, an {@link Aspect} is exported to JSON.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 31.01.18
 */
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exposed {

  /**
   * Is this an aspect, that can be filtered? Can i ask the webservice for entities with this attribute?
   */
  Visibility exposure() default PUBLIC;

  /**
   * This will rename this aspect in exports
   */
  String exposedName() default "";

  /**
   * When set to true, this aspect will be exported even if it's empty or null
   */
  boolean exposeIfEmpty() default false;

  /**
   * Select how you want this aspect (object instanceof {@link at.chex.archichexture.HasId}) to be serialized
   */
  Exposure exposeAs() default Exposure.DEFAULT;

  enum Exposure {
    /**
     * The {@link Aspect} will be serialized, as configured by the {@link Serialized} annotation
     */
    DEFAULT,
    /**
     * ArchicheXture will export the complete object
     */
    OBJECT,
    /**
     * Instead of the whole object, ArchicheXture will only export the id if this implements {@link at.chex.archichexture.HasId}.
     * Will be ignored, if there is no id
     */
    ID
  }

  enum Visibility {
    /**
     * This Field will not be exported
     */
    HIDDEN,
    /**
     * This Field will be exported
     */
    PUBLIC
  }
}
