package at.chex.archichexture.rest.config;

import org.aeonbits.owner.Config;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 30/03/2017
 */
public interface RestConfig extends Config {
    @Key("keyword_token")
    @DefaultValue("token")
    String getKeywordToken();

    @Key("keyword_token_reset")
    @DefaultValue("reset_token")
    String getKeywordTokenReset();

    @Key("keyword_limit")
    @DefaultValue("limit")
    String getKeywordLimit();

    @Key("keyword_offset")
    @DefaultValue("offset")
    String getKeywordOffset();

    @Key("default_value_limit")
    @DefaultValue("50")
    String getDefaultValueLimit();

    @Key("default_value_offset")
    @DefaultValue("0")
    String getDefaultValueOffset();
}
