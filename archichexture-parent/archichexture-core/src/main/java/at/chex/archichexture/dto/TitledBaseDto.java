package at.chex.archichexture.dto;

import at.chex.archichexture.model.TitledBaseEntity;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.xml.bind.annotation.XmlElement;

/**
 * Use this to transport {@link TitledBaseEntity}
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @version 1.0
 * @since 24/03/2017
 */
public abstract class TitledBaseDto<ENTITY extends TitledBaseEntity> extends BaseDto<ENTITY> {

  private static final long serialVersionUID = 1L;
  @DefaultValue("-1")
  @FormParam(TitledBaseEntity.FIELD_NAME_TITLE)
  @XmlElement(name = TitledBaseEntity.FIELD_NAME_TITLE)
  public String title;

  /**
   * Outgoing constructor
   */
  public TitledBaseDto(ENTITY entity) {
    super(entity);
    if (null != entity) {
      this.title = entity.getTitle();
    }
  }

  /**
   * Incoming (RESTEasy) default constructor
   */
  public TitledBaseDto() {
    super();
  }

  /**
   * Get the title of the {@link ENTITY}
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title of the {@link ENTITY}
   */
  public void setTitle(String title) {
    this.title = title;
  }
}
