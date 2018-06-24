package at.chex.archichexture;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 13.06.18
 */
public interface HasId {

  String FIELD_NAME_ID = "id";

  @SuppressWarnings("unused")
  Long getId();

  @SuppressWarnings("unused")
  void setId(Long id);
}
