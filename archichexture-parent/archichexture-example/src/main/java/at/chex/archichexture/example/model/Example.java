package at.chex.archichexture.example.model;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.Visibility;
import at.chex.archichexture.annotation.RemoveOnDelete;
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
// The {@link at.chex.archichexture.annotation.RemoveOnDelete} Annotation defines, if this {@link Entity} is deactivated (default, when subclassing {@link DocumentedEntity}) or deleted (Annotation present)
@RemoveOnDelete
public class Example extends DateExample {

  @Aspect(filterable = true, strict = true)
  @AlternativeNames({"what", "ever"})
  @Column(name = "whatever")
  @Exposed(exposedName = "ever_what")
  private String whatever;

  @Aspect
  @AlternativeNames({"bla", "blubber"})
  @Column(name = "blub")
  @Exposed
  private String blub;

  @Aspect
  @Column(name = "hidden_field")
  @Exposed(exposure = Visibility.HIDDEN)
  private String hiddenField;

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
    final StringBuilder sb = new StringBuilder("Example{");
    sb.append("whatever='").append(whatever).append('\'');
    sb.append(", blub='").append(blub).append('\'');
    sb.append(", hiddenField='").append(hiddenField).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
