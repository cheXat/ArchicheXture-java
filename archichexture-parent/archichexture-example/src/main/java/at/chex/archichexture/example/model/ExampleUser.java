package at.chex.archichexture.example.model;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.extension.model.User;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
  @Exposed(value = "first_name")
  @AlternativeNames({"first_name", "first-name"})
  @Column(name = "firstname")
  private String firstname;
  @Aspect
  @Exposed
  @AlternativeNames({"last_name", "last-name"})
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
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "example")
  @Aspect
  @Exposed
  private Example example;

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

  public Example getExample() {
    return example;
  }

  public void setExample(Example example) {
    this.example = example;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ExampleUser{");
    sb.append("firstname='").append(firstname).append('\'');
    sb.append(", lastname='").append(lastname).append('\'');
    sb.append(", email='").append(email).append('\'');
    sb.append(", telephone='").append(telephone).append('\'');
    sb.append(", example=").append(example);
    sb.append('}');
    return sb.toString();
  }
}
