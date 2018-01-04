package at.chex.archichexture.test.repository.impl;

import at.chex.archichexture.repository.impl.AbstractBaseRepository;
import at.chex.archichexture.test.model.TestEntity;
import at.chex.archichexture.test.repository.TestRepository;
import com.mysema.query.types.path.EntityPathBase;
import javax.inject.Named;
import javax.persistence.EntityManager;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/06/2017
 */
@Named
public class StatelessTestRepository extends AbstractBaseRepository<TestEntity> implements
    TestRepository {

  @Override
  protected EntityPathBase<TestEntity> getEntityPath() {
    return null;
  }

  @Override
  protected EntityManager getEntityManager() {
    return null;
  }
}
