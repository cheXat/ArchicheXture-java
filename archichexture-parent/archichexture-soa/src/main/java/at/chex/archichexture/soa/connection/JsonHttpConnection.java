package at.chex.archichexture.soa.connection;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 05/04/2017
 */
public class JsonHttpConnection<RESULT> extends HttpConnection<RESULT> {

  private static final Logger log = LoggerFactory.getLogger(JsonHttpConnection.class);

  public JsonHttpConnection(Class<RESULT> resultingClass) {
    super(resultingClass);
  }

  public RESULT get(String uri) {
    try {
      URL url = new URL(uri);
      InputStreamReader reader = new InputStreamReader(url.openStream());
      return new Gson().fromJson(reader, getResultingClass());
    } catch (IOException e) {
      log.error(e.getLocalizedMessage(), e);
    }
    return null;
  }
}
