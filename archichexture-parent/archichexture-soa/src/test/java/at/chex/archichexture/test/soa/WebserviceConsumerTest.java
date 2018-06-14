package at.chex.archichexture.test.soa;

import at.chex.archichexture.soa.WebserviceConsumer;
import at.chex.archichexture.test.soa.dto.IpDto;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakob Galbavy <code>jg@chex.at</code>
 * @since 05/04/2017
 */

public class WebserviceConsumerTest {

  private static final Logger log = LoggerFactory.getLogger(WebserviceConsumerTest.class);

  @Test
  public void consumeMyIpJsonTest() {
    IpDto ip = WebserviceConsumer.callJsonService("http://ip.jsontest.com/", IpDto.class);
    Assert.assertNotNull(ip);
    Assert.assertNotNull(ip.ip);
    log.trace("Returned ip:{}", ip.ip);
  }
}
