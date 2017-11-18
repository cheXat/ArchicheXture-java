package at.chex.archichexture.model;

import com.google.common.base.MoreObjects;
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
  @Column(name = FIELD_NAME_TITLE)
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
}