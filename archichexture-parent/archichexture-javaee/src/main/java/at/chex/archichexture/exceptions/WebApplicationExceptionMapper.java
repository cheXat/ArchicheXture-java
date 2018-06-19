package at.chex.archichexture.exceptions;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 19.06.18
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  @Inject
  private Logger log;

  @Override
  public Response toResponse(WebApplicationException webApplicationException) {
    int status = webApplicationException.getResponse().getStatus();
    if (status >= 400 && status < 500) {
      log.warn("Returning Error ({}): {}", status, webApplicationException.getLocalizedMessage());
    } else {
      log.error("Exception in Webservice {}", status, webApplicationException);
    }
    return webApplicationException.getResponse();
  }
}