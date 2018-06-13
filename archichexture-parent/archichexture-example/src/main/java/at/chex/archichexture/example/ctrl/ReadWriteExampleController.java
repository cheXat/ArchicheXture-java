package at.chex.archichexture.example.ctrl;

import at.chex.archichexture.annotation.TokenSecured;
import at.chex.archichexture.example.model.Example;
import at.chex.archichexture.example.repository.ExampleRepository;
import at.chex.archichexture.extension.token.NotNullOrEmptyTokenCheck;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.rest.TokenBaseRestController;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Path("/read-write-example")
public class ReadWriteExampleController extends TokenBaseRestController<Example> {

  @Inject
  private ExampleRepository exampleRepository;

  /**
   * Initialize the controller with a "always true" condition. This way, we avoid the tokencheck completely.
   */
  @PostConstruct
  public void init() {
    super.init((token, resetTokenExpiration) -> 0, false);
  }

  @Override
  protected BaseRepository<Example> getEntityRepository() {
    return exampleRepository;
  }

  @GET
  @Path("/secured")
  @Produces(MediaType.APPLICATION_JSON)
  @TokenSecured(checkClass = NotNullOrEmptyTokenCheck.class)
  public Response getSecuredResult() {
    return Response.ok(true).build();
  }
}
