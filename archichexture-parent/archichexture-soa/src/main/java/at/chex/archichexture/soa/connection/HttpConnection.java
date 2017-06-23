package at.chex.archichexture.soa.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 05/04/2017
 */
public abstract class HttpConnection<RESULT> implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(HttpConnection.class);
  protected InputStream inputStream;
  private HttpURLConnection connection;
  private Class<RESULT> resultingClass;

  public HttpConnection(Class<RESULT> resultingClass) throws IOException, JAXBException {
    this.resultingClass = resultingClass;
    log.debug("Created new HTTP Connection for class {}", resultingClass.getSimpleName());
  }

  protected Class<RESULT> getResultingClass() {
    return this.resultingClass;
  }

  protected HttpURLConnection connectTo(String uri) throws IOException {
    URL urlObject = null;
    urlObject = new URL(uri);
    log.debug("Opening connection to {}", uri);
    this.connection = (HttpURLConnection) urlObject.openConnection();
    this.connection.setRequestMethod("GET");
    return this.connection;
  }

  public abstract RESULT get(String uri);

  @Override
  public void close() throws Exception {
    if (null != this.inputStream) {
      this.inputStream.close();
    }
    if (null != this.connection) {
      this.connection.disconnect();
    }
  }
}
