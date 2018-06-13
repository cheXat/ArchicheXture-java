package at.chex.archichexture.model;

import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.ExposureType;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 09.11.17
 */
@MappedSuperclass
public class DocumentedEntity extends TitledEntity {

  public static final String FIELD_NAME_ACTIVE = "active";

  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  @Exposed(exposure = ExposureType.HIDDEN)
  private Date createdAt = new Date();
  @Column(name = "updated_at")
  @Temporal(TemporalType.TIMESTAMP)
  @Exposed(exposure = ExposureType.HIDDEN)
  private Date updatedAt = new Date();
  @Column(name = "deleted_at")
  @Temporal(TemporalType.TIMESTAMP)
  @Exposed(exposure = ExposureType.HIDDEN)
  private Date deletedAt = null;
  @Aspect
  @Column(name = FIELD_NAME_ACTIVE)
  @Exposed(exposure = ExposureType.HIDDEN)
  private Boolean active = true;

  @PrePersist
  protected void onCreate() {
    this.createdAt = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = new Date();
    if (null == this.active) {
      this.active = true;
    }
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Date getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Date deletedAt) {
    this.deletedAt = deletedAt;
  }

  public Boolean getActive() {
    return null != active && active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
