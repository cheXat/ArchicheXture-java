package at.chex.archichexture.model;

import at.chex.archichexture.HasId;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.Visibility;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;

/**
 * Base class for database persistence to be used by all entities that are to be used with
 * ArchicheXture frontends.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
@MappedSuperclass
public abstract class BaseEntity implements HasId, Serializable {

  @SuppressWarnings({"WeakerAccess", "unused"})
  public static final String FIELD_NAME_ID = "id";
  private static final long serialVersionUID = 1L;
  /**
   * We expect, the database takes care about generating the primary key. Of course, you can
   * generate it yourself and set it via {@link #setId(Long)}
   */
  @Aspect(modifiable = false, strict = true)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @XmlElement(name = FIELD_NAME_ID)
  @Exposed(exposure = Visibility.PUBLIC)
  private Long id;

  /**
   * Get the primary key of this entity
   *
   * @return the id of this entity
   */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Set the primary key of this entity
   *
   * @param id the new id
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Convenience method to check if the id is set and valid
   *
   * @return valid id is set?
   */
  @SuppressWarnings("unused")
  public boolean isNew() {
    return null == this.id || this.id < 1L;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseEntity)) {
      return false;
    }
    BaseEntity that = (BaseEntity) o;
    return Objects.equal(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
