package at.chex.archichexture.rest.token;

/**
 * This provides an interface, that is used to check the validity of an incoming token.
 *
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 30/03/2017
 */
public interface TokenCheck {
    /**
     * This  will return any {@link Integer}. Apart from zero, this is considered, as an invalid token.
     *
     * @param token                the {@link String} to check
     * @param resetTokenExpiration if the expiration timer of the token should be reset (e.g. human website action) or stay, as it is (e.g. scheduled poller)
     * @return 0 if the token is valid
     */
    int getTokenResponseCode(String token, boolean resetTokenExpiration);
}
