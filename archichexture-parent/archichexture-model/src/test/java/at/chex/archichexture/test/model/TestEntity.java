package at.chex.archichexture.test.model;

import at.chex.archichexture.model.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/06/2017
 */
@Entity
public class TestEntity extends BaseEntity {

  @Column(name = "name")
  private String name;

  @SuppressWarnings("unused")
  public String getName() {
    return name;
  }

  @SuppressWarnings("unused")
  public void setName(String name) {
    this.name = name;
  }
}
