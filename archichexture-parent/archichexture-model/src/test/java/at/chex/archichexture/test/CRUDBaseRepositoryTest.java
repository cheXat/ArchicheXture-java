package at.chex.archichexture.test;

import at.chex.archichexture.test.model.TestEntity;
import at.chex.archichexture.test.repository.TestRepository;
import at.chex.archichexture.test.repository.impl.StatelessTestRepository;
import javax.inject.Inject;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/06/2017
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(StatelessTestRepository.class)
public class CRUDBaseRepositoryTest extends AbstractJPAHibernateTest {

  @Inject
  private TestRepository testRepository;

  @Ignore
  @Test
  public void createAndPersistTest() {
    TestEntity entity = testRepository.create();

  }
}
