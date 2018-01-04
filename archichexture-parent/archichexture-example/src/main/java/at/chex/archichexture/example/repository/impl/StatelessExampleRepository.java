package at.chex.archichexture.example.repository.impl;

import at.chex.archichexture.example.model.Example;
import at.chex.archichexture.example.model.QExample;
import at.chex.archichexture.example.repository.ExampleRepository;
import at.chex.archichexture.repository.impl.AbstractBaseRepository;
import com.mysema.query.types.path.EntityPathBase;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@Stateless
public class StatelessExampleRepository extends AbstractBaseRepository<Example> implements
    ExampleRepository {

  private QExample q = QExample.example;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  protected EntityPathBase<Example> getEntityPath() {
    return q;
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}
