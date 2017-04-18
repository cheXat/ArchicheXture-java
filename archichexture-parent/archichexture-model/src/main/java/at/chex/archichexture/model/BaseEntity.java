package at.chex.archichexture.model;

import com.google.common.base.MoreObjects;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * Base class for database persistence to be used by all entities that are to be used with ArchicheXture frontends.
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * We expect, the database takes care about generating the primary key.
     * Of course, you can generate it yourself and set it via {@link #setId(Long)}
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlElement(name = "id")
    private Long id;

    /**
     * Get the primary key of this entity
     *
     * @return the id of this entity
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the primary key of this entity
     *
     * @param id
     *         the new id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Convenience method to check if the id is set and valid
     *
     * @return valid id is set?
     */
    public boolean isNew() {
        return null == this.id || this.id < 1L;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
