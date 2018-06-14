package at.chex.archichexture.extension.model;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.Visibility;
import at.chex.archichexture.model.BaseEntity;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@MappedSuperclass
public abstract class User extends BaseEntity {

  public static final String FIELD_NAME_TOKEN = "token";
  public static final String FIELD_NAME_USERNAME = "username";

  @Aspect(filterable = false, modifiable = false, strict = true)
  @Exposed
  @Column(name = FIELD_NAME_TOKEN)
  private String token;
  @Aspect
  @Exposed
  @Column(name = "token_issuing_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date tokenIssuingDate;
  @Aspect
  @Exposed
  @Column(name = FIELD_NAME_USERNAME)
  @AlternativeNames({"user_name"})
  private String username;
  @Exposed(exposure = Visibility.HIDDEN)
  @Column(name = "password")
  private String password;
  @Exposed(exposure = Visibility.HIDDEN)
  @Column(name = "password_salt", columnDefinition = "BLOB", length = 256)
  @Lob
  private byte[] passwordSalt;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @SuppressWarnings("unused")
  public Date getTokenIssuingDate() {
    return tokenIssuingDate;
  }

  public void setTokenIssuingDate(Date tokenIssuingDate) {
    this.tokenIssuingDate = tokenIssuingDate;
  }

  @SuppressWarnings("unused")
  public String getUsername() {
    return username;
  }

  @SuppressWarnings("unused")
  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public byte[] getPasswordSalt() {
    return passwordSalt;
  }

  public void setPasswordSalt(byte[] passwordSalt) {
    this.passwordSalt = passwordSalt;
  }
}
