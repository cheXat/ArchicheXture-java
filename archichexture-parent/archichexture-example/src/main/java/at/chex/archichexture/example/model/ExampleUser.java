package at.chex.archichexture.example.model;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.extension.model.User;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This extends the data of {@link User} (which already takes care about username, passwords, tokens, etc) with some sample data, that could be useful
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@Entity
@Table(name = "users")
public class ExampleUser extends User {

  @Aspect
  @Exposed
  @Column(name = "firstname")
  private String firstname;
  @Aspect
  @Exposed
  @Column(name = "lastname")
  private String lastname;
  @Aspect
  @Exposed
  @AlternativeNames({"e-mail"})
  @Column(name = "email")
  private String email;
  @Aspect
  @Exposed
  @AlternativeNames({"telefone", "tel"})
  @Column(name = "telephone")
  private String telephone;

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }
}
