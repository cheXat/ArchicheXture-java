package at.chex.archichexture.model;

import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.Visibility;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Convenience Class with Title
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 16/04/2017
 */
@MappedSuperclass
public abstract class TitledEntity extends BaseEntity {

  public static final String FIELD_NAME_TITLE = "title";
  /**
   * The title of this Entity
   */
  @Aspect
  @Column(name = FIELD_NAME_TITLE)
  @Exposed(exposure = Visibility.PUBLIC)
  private String title;

  /**
   * The title of this Entity
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title of this Entity
   */
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("title", title)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TitledEntity)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TitledEntity that = (TitledEntity) o;
    return Objects.equal(getTitle(), that.getTitle());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), getTitle());
  }
}
