package at.chex.archichexture.service;

import at.chex.archichexture.HasId;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 24.06.18
 */
public interface TransferEntitiesService {

  <T extends HasId> void transfer(T newEntity, T entityToBeChanged);
}
