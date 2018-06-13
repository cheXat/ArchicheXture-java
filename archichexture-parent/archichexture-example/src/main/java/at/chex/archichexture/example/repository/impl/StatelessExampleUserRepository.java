package at.chex.archichexture.example.repository.impl;

import at.chex.archichexture.example.model.ExampleUser;
import at.chex.archichexture.example.model.QExampleUser;
import at.chex.archichexture.example.repository.ExampleUserRepository;
import at.chex.archichexture.extension.repository.impl.AbstractUserRepository;
import com.querydsl.core.types.dsl.EntityPathBase;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@Stateless
public class StatelessExampleUserRepository extends AbstractUserRepository<ExampleUser> implements
    ExampleUserRepository {

  private QExampleUser q = QExampleUser.exampleUser;

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  protected EntityPathBase<ExampleUser> getEntityPath() {
    return q;
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }
}
