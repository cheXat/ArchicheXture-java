package at.chex.archichexture.producer;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LoggerProducer {

  @Produces
  public Logger produceLogger(InjectionPoint ip) {
    return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
  }
}
