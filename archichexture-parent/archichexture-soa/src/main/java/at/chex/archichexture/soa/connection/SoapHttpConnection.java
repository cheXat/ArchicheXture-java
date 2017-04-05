package at.chex.archichexture.soa.connection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 05/04/2017
 */
public class SoapHttpConnection<RESULT> extends HttpConnection<RESULT> {
    public SoapHttpConnection(Class<RESULT> resultingClass) throws IOException, JAXBException {
        super(resultingClass);
    }

    public RESULT get(String uri) {
        try {
            HttpURLConnection connection = connectTo(uri);
            connection.setRequestProperty("Accept", "application/xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(getResultingClass());
            this.inputStream = connection.getInputStream();
            return (RESULT) jaxbContext.createUnmarshaller().unmarshal(this.inputStream);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
