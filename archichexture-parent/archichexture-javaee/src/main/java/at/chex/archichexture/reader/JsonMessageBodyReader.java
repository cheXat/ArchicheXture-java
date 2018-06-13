package at.chex.archichexture.reader;

import at.chex.archichexture.helpers.Reflection;
import at.chex.archichexture.model.BaseEntity;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.enterprise.context.Dependent;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@Provider
@Dependent
@Consumes("application/json")
public class JsonMessageBodyReader<TYPE extends BaseEntity> implements MessageBodyReader<TYPE> {

  private static final Logger log = LoggerFactory.getLogger(JsonMessageBodyReader.class);

  @Override
  public boolean isReadable(Class type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    boolean instance = BaseEntity.class.isAssignableFrom(type);
    log.debug("{}Reading Class of type {}", instance ? "" : "NOT ", type);
    return instance;
  }

  @Override
  public TYPE readFrom(Class<TYPE> aClass, Type type, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> multivaluedMap, InputStream inputStream)
      throws IOException, WebApplicationException {
    if (!BaseEntity.class.isAssignableFrom(aClass)) {
      return null;
    }

    String json = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));

    JsonObject baseEntity = new Gson().fromJson(json, JsonObject.class);
    try {
      TYPE instance = aClass.newInstance();
      Reflection.transferValuesFromLeftToRight(baseEntity, instance);
      return instance;
    } catch (InstantiationException | IllegalAccessException e) {
      log.error("Exception while instantiation of new class: {}", e.getLocalizedMessage(), e);
    }
    log.debug("Transform Class of type {} from json {} to class {}", aClass, json, baseEntity);
    return null;
  }
}
