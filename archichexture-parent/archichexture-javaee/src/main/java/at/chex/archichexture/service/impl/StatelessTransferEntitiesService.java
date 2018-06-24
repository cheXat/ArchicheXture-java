package at.chex.archichexture.service.impl;

import at.chex.archichexture.HasId;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.repository.BaseRepository;
import at.chex.archichexture.service.TransferEntitiesService;
import at.chex.archichexture.slh.Reflection;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import org.slf4j.Logger;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 24.06.18
 */
@Stateless
public class StatelessTransferEntitiesService implements TransferEntitiesService {

  @Inject
  private Logger log;
  @Inject
  @Any
  private Instance<BaseRepository<?>> repositories;
  private Map<Class<? extends BaseEntity>, BaseRepository<? extends BaseEntity>> repositoryMap = new HashMap<>();

  @PostConstruct
  public void init() {
    for (BaseRepository<?> repository : repositories) {
      repositoryMap.put(repository.getEntityClass(), repository);
    }
    log.debug("Found {} Repositories", repositoryMap.size());
  }

  private <T> boolean fieldValueDifferent(Field field, T implLeft, T implRight)
      throws IllegalAccessException {
    Object fieldValueLeft = field.get(implLeft);
    Object fieldValueRight = field.get(implRight);

    if ((null != fieldValueLeft) && (null != fieldValueRight)) {
      if (HasId.class.isAssignableFrom(fieldValueLeft.getClass()) &&
          HasId.class.isAssignableFrom(fieldValueRight.getClass())) {
        return null == ((HasId) fieldValueLeft).getId() ||
            !((HasId) fieldValueLeft).getId().equals(((HasId) fieldValueRight).getId());
      }
      return true;
    }
    return ((null == fieldValueLeft) != (null == fieldValueRight));
  }

  @Override
  public <T extends HasId> void transfer(T newEntity, T entityToBeChanged) {
    List<Field> fieldsWithInterface = Reflection
        .getFieldsWithInterface(HasId.class, newEntity.getClass());
    for (Field field : fieldsWithInterface) {
      try {
        field.setAccessible(true);
        if (fieldValueDifferent(field, newEntity, entityToBeChanged)) {
          if (!repositoryMap.containsKey(field.getType())) {
            continue;
          }
          BaseRepository<?> repository = repositoryMap.get(field.getType());
          Object newEntityValue = field.get(newEntity);
          if (null != newEntityValue) {
            BaseEntity entityById = repository.findEntityById(((HasId) newEntityValue).getId());
            field.set(entityToBeChanged, entityById);
          }
        }
      } catch (IllegalAccessException e) {
        log.debug(e.getLocalizedMessage());
      }
    }
  }
}
