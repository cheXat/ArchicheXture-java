package at.chex.archichexture.soa.connection;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 05/04/2017
 */
public abstract class HttpConnection<RESULT> implements AutoCloseable {
    protected InputStream inputStream;

    private HttpURLConnection connection;
    private Class<RESULT> resultingClass;

    public HttpConnection(Class<RESULT> resultingClass) throws IOException, JAXBException {
        this.resultingClass = resultingClass;
    }

    protected Class<RESULT> getResultingClass() {
        return this.resultingClass;
    }

    protected HttpURLConnection connectTo(String uri) throws IOException {
        URL urlObject = null;
        urlObject = new URL(uri);
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
