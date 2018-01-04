package at.chex.archichexture.example.ctrl;

import at.chex.archichexture.example.dto.ExampleDto;
import at.chex.archichexture.example.model.Example;
import at.chex.archichexture.example.repository.ExampleRepository;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.rest.TokenBaseRestController;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Path("/read-write-example")
public class ReadWriteExampleController extends TokenBaseRestController<Example, ExampleDto> {

  @Inject
  private ExampleRepository exampleRepository;

  @PostConstruct
  public void init() {
    super.init((token, resetTokenExpiration) -> 0, false);
  }

  @Override
  protected BaseRepository<Example> getEntityRepository() {
    return exampleRepository;
  }

  @Override
  protected ExampleDto transformToDto(Example entity) {
    return new ExampleDto(entity);
  }
}
