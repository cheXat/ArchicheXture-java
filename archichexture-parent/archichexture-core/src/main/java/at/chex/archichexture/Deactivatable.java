package at.chex.archichexture;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 14.06.18
 */
public interface Deactivatable {

  String FIELD_NAME_ACTIVE = "active";

  Boolean getActive();

  void setActive(Boolean active);
}
