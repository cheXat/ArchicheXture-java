package at.chex.archichexture.example.ctrl;

import at.chex.archichexture.example.model.Example;
import at.chex.archichexture.example.repository.ExampleRepository;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.rest.TokenBaseRestController;
import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * This is an example readonly Controller. Try with these requests to see how everything works (after generating the datamodel):
 *
 * bla/blub/blubber is case insensitive wildcard enabled. You can filter via percentage
 * http://localhost:8080/archichexture-example/read-example?bla=%25cd%25&_order_by=id&_order_dir=desc
 *
 * You can sort for all fields including their aliases set by {@link at.chex.archichexture.annotation.AlternativeNames}
 * http://localhost:8080/archichexture-example/read-example?_order_by=ever&_order_dir=asc
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Path("/read-example")
public class ReadonlyExampleController extends TokenBaseRestController<Example> {

  @Inject
  private ExampleRepository exampleRepository;

  @Override
  protected BaseRepository<Example> getEntityRepository() {
    return exampleRepository;
  }
}
