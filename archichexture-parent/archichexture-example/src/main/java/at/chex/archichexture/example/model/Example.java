package at.chex.archichexture.example.model;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.RemoveOnDelete;
import at.chex.archichexture.model.DocumentedEntity;
import com.google.common.base.MoreObjects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Entity
@Table(name = "examples")
/**
 * The {@link at.chex.archichexture.annotation.RemoveOnDelete} Annotation defines, if this {@link Entity} is deactivated (default, when subclassing {@link DocumentedEntity}) or deleted (Annotation present)
 */
@RemoveOnDelete
public class Example extends DocumentedEntity {

  @Aspect(filterable = true, strict = true)
  @AlternativeNames({"what", "ever"})
  @Column(name = "whatever")
  private String whatever;

  @Aspect
  @AlternativeNames({"bla", "blubber"})
  @Column(name = "blub")
  private String blub;

  public String getWhatever() {
    return whatever;
  }

  public void setWhatever(String whatever) {
    this.whatever = whatever;
  }

  public String getBlub() {
    return blub;
  }

  public void setBlub(String blub) {
    this.blub = blub;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("whatever", whatever)
        .add("blub", blub)
        .toString();
  }
}
