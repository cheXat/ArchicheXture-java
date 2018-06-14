package at.chex.archichexture.soa.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 05/04/2017
 */
public class SoapHttpConnection<RESULT> extends HttpConnection<RESULT> {

  private static final Logger log = LoggerFactory.getLogger(SoapHttpConnection.class);

  public SoapHttpConnection(Class<RESULT> resultingClass) {
    super(resultingClass);
  }

  @SuppressWarnings({"unchecked", "unused"})
  public RESULT get(String uri) {
    try {
      HttpURLConnection connection = connectTo(uri);
      connection.setRequestProperty("Accept", "application/xml");
      JAXBContext jaxbContext = JAXBContext.newInstance(getResultingClass());
      this.inputStream = connection.getInputStream();
      return (RESULT) jaxbContext.createUnmarshaller().unmarshal(this.inputStream);
    } catch (JAXBException | IOException e) {
      log.error(e.getLocalizedMessage(), e);
    }
    return null;
  }
}
