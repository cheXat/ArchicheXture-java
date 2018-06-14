package at.chex.archichexture.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/06/2017
 */
public abstract class AbstractJPAHibernateTest {

  public static final String PERSISTENCE_UNIT_NAME = "archichexture-test";

  protected static EntityManagerFactory emf;
  protected static EntityManager em;

  @BeforeClass
  public static void init() {
    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    em = emf.createEntityManager();
  }

  @AfterClass
  public static void tearDown() {
    if (null != em) {
      em.clear();
      em.close();
    }
    if (null != emf) {
      emf.close();
    }
  }
}