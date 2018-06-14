package at.chex.archichexture.soa;

import at.chex.archichexture.soa.connection.HttpConnection;
import at.chex.archichexture.soa.connection.JsonHttpConnection;
import at.chex.archichexture.soa.connection.SoapHttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 04/04/2017
 */
public class WebserviceConsumer {

  private static final Logger log = LoggerFactory.getLogger(WebserviceConsumer.class);

  private WebserviceConsumer() {
  }

  @SuppressWarnings("unused")
  public static <RESULT> RESULT callSoapService(String url, Class<RESULT> resultingClass) {
    try (HttpConnection<RESULT> connection = new SoapHttpConnection<>(resultingClass)) {
      return connection.get(url);
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }
    return null;
  }

  @SuppressWarnings("unused")
  public static <RESULT> RESULT callJsonService(String url, Class<RESULT> resultingClass) {
    try (HttpConnection<RESULT> connection = new JsonHttpConnection<>(resultingClass)) {
      return connection.get(url);
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }
    return null;
  }
}
