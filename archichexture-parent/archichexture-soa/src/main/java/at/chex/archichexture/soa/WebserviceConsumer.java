package at.chex.archichexture.soa;

import at.chex.archichexture.soa.connection.HttpConnection;
import at.chex.archichexture.soa.connection.JsonHttpConnection;
import at.chex.archichexture.soa.connection.SoapHttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 04/04/2017
 */
public class WebserviceConsumer {
    private static final Logger log = LoggerFactory.getLogger(WebserviceConsumer.class);

    private WebserviceConsumer() {
    }

    public static <RESULT> RESULT callSoapService(String url, Class<RESULT> resultingClass) {
        try (HttpConnection<RESULT> connection = new SoapHttpConnection<>(resultingClass)) {
            return connection.get(url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <RESULT> RESULT callJsonService(String url, Class<RESULT> resultingClass) {
        try (HttpConnection<RESULT> connection = new JsonHttpConnection<>(resultingClass)) {
            return connection.get(url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
