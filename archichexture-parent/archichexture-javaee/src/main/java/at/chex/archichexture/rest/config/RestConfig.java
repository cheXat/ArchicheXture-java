package at.chex.archichexture.rest.config;

import org.aeonbits.owner.Config;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/03/2017
 */
public interface RestConfig extends Config {

  /**
   * The token will be the value of this key
   */
  @Key("keyword_token")
  @DefaultValue("token")
  String getKeywordToken();

  /**
   * The value of this key (boolean) will indicate, if the last access-timer of the token will be
   * reset by this request
   */
  @Key("keyword_token_reset")
  @DefaultValue("reset_token")
  String getKeywordTokenReset();

  /**
   * The value of this key (integer) will be used as the limit parameter in the query
   */
  @Key("keyword_limit")
  @DefaultValue("limit")
  String getKeywordLimit();

  /**
   * The value of this key (integer) will be used as the offset parameter in the query
   */
  @Key("keyword_offset")
  @DefaultValue("offset")
  String getKeywordOffset();

  /**
   * The value of this key (integer) will be used as the default limit parameter in the query
   */
  @Key("default_value_limit")
  @DefaultValue("50")
  String getDefaultValueLimit();

  /**
   * The value of this key (integer) will be used as the default offset parameter in the query
   */
  @Key("default_value_offset")
  @DefaultValue("0")
  String getDefaultValueOffset();
}
