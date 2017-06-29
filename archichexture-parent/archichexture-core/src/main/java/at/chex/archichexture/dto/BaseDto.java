package at.chex.archichexture.dto;

import at.chex.archichexture.model.BaseEntity;
import java.io.Serializable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlElement;

/**
 * This is the base for all dtos that are used to transport {@link BaseEntity}
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
public abstract class BaseDto<ENTITY extends BaseEntity> implements Serializable {

  private static final long serialVersionUID = 1L;

  @DefaultValue("-1")
  @FormParam(BaseEntity.FIELD_NAME_ID)
  @XmlElement(name = BaseEntity.FIELD_NAME_ID)
  protected Long id;

  /**
   * Outgoing constructor
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

  /**
   * The id of the {@link ENTITY}
   */
  public Long getId() {
    return id;
  }

  /**
   * Set the id of this {@link ENTITY}
   */
  public void setId(Long id) {
    this.id = id;
  }
}
