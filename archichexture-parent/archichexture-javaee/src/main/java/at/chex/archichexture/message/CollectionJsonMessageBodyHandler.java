package at.chex.archichexture.message;

import at.chex.archichexture.helpers.Reflection;
import at.chex.archichexture.model.BaseEntity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.enterprise.context.Dependent;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 12.06.18
 */
@Provider
@Dependent
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CollectionJsonMessageBodyHandler<TYPE extends BaseEntity> implements
    MessageBodyWriter<Collection<TYPE>> {

  @Override
  public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType) {
    try {
      if (type instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        return BaseEntity.class.isAssignableFrom((Class) actualTypeArguments[0]);
      }
    } catch (Exception ex) {
      // do nothing
    }
    return false;
  }

  @Override
  public long getSize(Collection<TYPE> types, Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType) {
    // deprecated anyway
    return 0;
  }

  @Override
  public void writeTo(Collection<TYPE> collection, Class<?> aClass, Type type,
      Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
      throws IOException, WebApplicationException {
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
    JsonArray jsonArray = new JsonArray();
    for (Object obj : collection) {
      JsonObject jsonObject = new JsonObject();
      Reflection.transferValuesToJson(obj, jsonObject);
      jsonArray.add(jsonObject);
    }
    new Gson().toJson(jsonArray, writer);

    writer.flush();
    writer.close();
  }
}
