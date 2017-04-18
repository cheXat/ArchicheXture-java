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
public class TitledBaseEntity extends BaseEntity {
    /**
     * The title of this Entity
     */
    @Column(name = "title")
    private String title;

    public String getTitle() {
        return title;
    }

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
