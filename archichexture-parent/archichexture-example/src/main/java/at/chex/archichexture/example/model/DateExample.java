package at.chex.archichexture.example.model;

import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.model.DocumentedEntity;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
@MappedSuperclass
public abstract class DateExample extends DocumentedEntity {

  @Aspect
  @Column(name = "date_field")
  @Temporal(TemporalType.TIMESTAMP)
  private Date date = new Date();

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
