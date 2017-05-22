package at.chex.archichexture.dto;

import at.chex.archichexture.model.BaseEntity;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 24/03/2017
 */
public abstract class BaseDto<ENTITY extends BaseEntity> implements Serializable {
    private static final long serialVersionUID = 1L;
    @DefaultValue("-1")
    @FormParam("id")
    @XmlElement(name = "id")
    public Long id;

    /**
     * Outgoing constructor
     *
     * @param entity
     */
    public BaseDto(ENTITY entity) {
        if (null != entity) {
            this.id = entity.getId();
        }
    }

    /**
     * Incoming (RESTEasy) default constructor
     */
    public BaseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
