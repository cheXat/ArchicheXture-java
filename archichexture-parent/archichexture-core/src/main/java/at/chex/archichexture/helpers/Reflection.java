package at.chex.archichexture.helpers;

import at.chex.archichexture.HasId;
import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.Exposure;
import at.chex.archichexture.annotation.Exposed.Visibility;
import at.chex.archichexture.annotation.Serialized;
import at.chex.archichexture.annotation.Serialized.ExposureType;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class Reflection {

  private static final Logger log = LoggerFactory.getLogger(Reflection.class);

  private Reflection() {
  }

  /**
   * Will return the method with the given name in the given object and its inherited classes
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @Nonnull
  public static List<Field> getFieldsWithInterface(@Nonnull Class<?> iface,
      @Nonnull Class<?> clazz) {
    List<Field> returnList = new ArrayList<>();

    Class<?> classToWorkWith = clazz;
    do {
      returnList.addAll(innerGetFieldsWithInterface(iface, classToWorkWith));
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));

    return returnList;
  }

  private static List<Field> innerGetFieldsWithInterface(Class<?> iface,
      Class<?> clazz) {
    List<Field> returnList = new ArrayList<>();
    for (Field field : clazz.getDeclaredFields()) {
      if (iface.isAssignableFrom(field.getType())) {
        returnList.add(field);
      }
    }
    return returnList;
  }

  /**
   * Will return the method with the given name in the given object and its inherited classes
   */
  @SuppressWarnings({"WeakerAccess", "unused"})
  @Nonnull
  public static List<Field> getAnnotatedFields(Class<? extends Annotation> annotation,
      Class<?> clazz) {
    List<Field> returnList = new ArrayList<>();

    Class<?> classToWorkWith = clazz;
    do {
      returnList.addAll(innerGetAnnotatedFields(annotation, classToWorkWith));
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));

    return returnList;
  }

  private static List<Field> innerGetAnnotatedFields(Class<? extends Annotation> annotation,
      Class<?> clazz) {
    List<Field> returnList = new ArrayList<>();
    for (Field field : clazz.getDeclaredFields()) {
      if (field.isAnnotationPresent(annotation)) {
        returnList.add(field);
      }
    }
    return returnList;
  }

  /**
   * Returns the first match of a method named like one of the values in the string list
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  public static Field getFieldFromClassInStringList(Class<?> clazz, Collection<String> list) {
    if (null == clazz || null == list || list.isEmpty()) {
      return null;
    }
    Class<?> classToWorkWith = clazz;

    do {
      for (String value : list) {
        try {
          Field field = classToWorkWith.getDeclaredField(value);
          if (null != field) {
            return field;
          }
        } catch (NoSuchFieldException e) {
          // do nothing
        }
      }
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));
    return null;
  }

  /**
   * Will return the method with the given name in the given object and its inherited classes
   */
  @SuppressWarnings("WeakerAccess")
  @Nullable
  public static Method getMethodByReflection(Object object, String methodToInvoke,
      Class<?> argumentType) throws NoSuchMethodException {
    Class<?> classToWorkWith = object.getClass();
    do {
      try {
        return innerGetMethodByReflection(classToWorkWith, methodToInvoke, argumentType);
      } catch (NoSuchMethodException e) {
        // do nothing
      }
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));
    throw new NoSuchMethodException("Method " + methodToInvoke + " not found.");
  }

  private static Method innerGetMethodByReflection(Class<?> clazz, String methodToInvoke,
      Class<?> argumentType) throws NoSuchMethodException {
    try {
      return clazz.getMethod(methodToInvoke, argumentType);
    } catch (NoSuchMethodException e) {
      return clazz.getMethod(methodToInvoke, Object.class);
    }
  }

  public static Object invokeMethodWithCorrectArgumentsType(Object object, Method method,
      Class<?> argumentType, String value)
      throws InvocationTargetException, IllegalAccessException {

    return method.invoke(object, Values.convert(value, argumentType));
  }

  /**
   * Call this to map all values, you need from the left to the right (if both fields exist)
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  public static <TYPE> TYPE transferValuesFromLeftToRight(Object left,
      TYPE right) {
    if (null == left || null == right) {
      log.warn("Object NULL at transferValuesFromLeftToRight! left:{}, right:{}", left, right);
      throw new NullPointerException();
    }

    Class<?> classToWorkWith = right.getClass();

    Serialized serializedClassAnnotation = getAnnotation(classToWorkWith, Serialized.class);
    if (null == serializedClassAnnotation) {
      throw new RuntimeException("Cannot serialize Class without Serialized Annotation present!");
    }

    // loop through the class hierarchy
    do {

      // loop through all fields on the target class
      for (Field fieldToSetOnEntity : classToWorkWith.getDeclaredFields()) {

        // check for @Aspect annotations
        if (fieldToSetOnEntity.isAnnotationPresent(Aspect.class)) {
          Aspect aspect = fieldToSetOnEntity.getAnnotation(Aspect.class);
          if (aspect.modifiable() ||
              HasId.FIELD_NAME_ID.equals(fieldToSetOnEntity.getName())) {
            List<String> filterNames = new ArrayList<>();
            filterNames.add(fieldToSetOnEntity.getName());
            filterNames.add(fieldToSetOnEntity.getName() + serializedClassAnnotation.idAppendix());

            // check for any @AlternativeNames given to this field (and try to find it in the source class)
            if (fieldToSetOnEntity.isAnnotationPresent(AlternativeNames.class)) {
              AlternativeNames alternativeNames = fieldToSetOnEntity
                  .getAnnotation(AlternativeNames.class);
              filterNames.addAll(Arrays.asList(alternativeNames.value()));
            }

            if (fieldToSetOnEntity.isAnnotationPresent(Exposed.class)) {
              Exposed exposed = fieldToSetOnEntity.getAnnotation(Exposed.class);
              if (!Strings.isNullOrEmpty(exposed.exposedName())) {
                filterNames.add(exposed.exposedName());
                filterNames.add(exposed.exposedName() + serializedClassAnnotation.idAppendix());
              }
            }

            // we need to use another accessor for a jsonObject
            if (left instanceof JsonObject) {
              JsonObject jsonObject = (JsonObject) left;
              JsonResult jsonResult = getJsonElementByNameList(jsonObject, filterNames);
              if (null != jsonResult) {
                if (jsonResult.element.isJsonPrimitive()) {
                  String elementValue = jsonResult.element.getAsString();
                  if (!Strings.isNullOrEmpty(elementValue)) {
                    try {
                      fieldToSetOnEntity.setAccessible(true);
                      if (!fieldToSetOnEntity.getType().equals(String.class)) {
                        try {
                          Object convertedValue = Values
                              .convert(elementValue, fieldToSetOnEntity.getType());
                          fieldToSetOnEntity.set(right, convertedValue);
                        } catch (RuntimeException ex) {
                          log.debug(ex.getLocalizedMessage());
                          // not a primitive
                          Long longValue = Values.convert(elementValue, Long.class);
                          if (longValue > 0 && HasId.class
                              .isAssignableFrom(fieldToSetOnEntity.getType())) {
                            Object obj = fieldToSetOnEntity.get(right);
                            if (null == obj) {
                              obj = fieldToSetOnEntity.getType().newInstance();
                              fieldToSetOnEntity.set(right, obj);
                            }
                            ((HasId) obj).setId(longValue);
                          }
                        }
                      } else {
                        fieldToSetOnEntity.set(right, elementValue);
                      }
                    } catch (IllegalAccessException | RuntimeException | InstantiationException e) {
                      // do nothing
                    }
                  }
                } else if (jsonResult.element.isJsonArray()) {
                  log.warn("Unable to process update on Array of jsonObjects yet");
                } else if (jsonResult.element.isJsonObject()) {
                  log.debug("{} is object", jsonResult.key);
                  try {
                    fieldToSetOnEntity.setAccessible(true);
                    Object obj = fieldToSetOnEntity.get(right);
                    if (null == obj) {
                      obj = fieldToSetOnEntity.getType().newInstance();
                      fieldToSetOnEntity.set(right, obj);
                    }
                    Reflection.transferValuesFromLeftToRight(jsonResult.element, obj);
                  } catch (IllegalAccessException | RuntimeException | InstantiationException ex) {
                    log.debug(ex.getLocalizedMessage());
                  }
                }
              }
            } else {
              Field valueFieldFromFormObject = Reflection
                  .getFieldFromClassInStringList(left.getClass(), filterNames);

              if (null != valueFieldFromFormObject) {
                try {
                  valueFieldFromFormObject.setAccessible(true);
                  Object object = valueFieldFromFormObject.get(left);
                  fieldToSetOnEntity.setAccessible(true);
                  if (null != object) {
                    if (object.getClass().equals(String.class) &&
                        !fieldToSetOnEntity.getType().equals(String.class)) {
                      Object convertedValue = Values
                          .convert((String) object, fieldToSetOnEntity.getType());
                      fieldToSetOnEntity.set(right, convertedValue);
                    } else {
                      fieldToSetOnEntity.set(right, object);
                    }
                  }
                } catch (IllegalAccessException e) {
                  // do nothing
                }
              }
            }
          }
        }
      }
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));
    return right;
  }

  private static JsonResult getJsonElementByNameList(JsonObject jsonObject,
      List<String> nameList) {
    for (String name : nameList) {
      JsonElement jsonElement = jsonObject.get(name);
      if (null != jsonElement) {
        return new JsonResult(name, jsonElement);
      }
    }
    return null;
  }

  /**
   * Transfer all correctly annotated values from left to the given {@link JsonObject}
   */
  @SuppressWarnings("WeakerAccess")
  @Nonnull
  public static <TYPE> JsonObject transferValuesToJson(TYPE left, JsonObject jsonObject) {
    if (null == left || null == jsonObject) {
      log.warn("Object NULL at transferValuesToJson! left:{}, jsonObject:{}", left, jsonObject);
      throw new NullPointerException();
    }

    Class<?> classToWorkWith = left.getClass();
    Serialized serializedClassAnnotation = getAnnotation(classToWorkWith, Serialized.class);
    if (null == serializedClassAnnotation) {
      throw new RuntimeException("Cannot serialize Class without Serialized Annotation present!");
    }

    // loop through the class hierarchy
    do {
      // loop through all fields on the target class
      for (Field fieldToSetOnEntity : classToWorkWith.getDeclaredFields()) {

        // check for @Aspect annotations
        if (fieldToSetOnEntity.isAnnotationPresent(Aspect.class)) {
          if (!fieldToSetOnEntity.isAnnotationPresent(Exposed.class) || fieldToSetOnEntity
              .getAnnotation(Exposed.class).exposure().equals(Visibility.PUBLIC)) {
            String keyName = fieldToSetOnEntity.getName();
            boolean exportIfEmpty = false;
            Exposure exposure = Exposure.OBJECT;
            boolean exposureNameOverridden = false;
            if (fieldToSetOnEntity.isAnnotationPresent(Exposed.class)) {
              Exposed exposed = fieldToSetOnEntity.getAnnotation(Exposed.class);
              if (!Strings.isNullOrEmpty(exposed.exposedName())) {
                keyName = exposed.exposedName();
                exposureNameOverridden = true;
              }
              exportIfEmpty = exposed.exposeIfEmpty();
              exposure = exposed.exposeAs();
            }

            try {
              fieldToSetOnEntity.setAccessible(true);
              Object object = fieldToSetOnEntity.get(left);
              if (null != object) {
                if (object instanceof HasId) { // if this is an object, we can actually handle. All ArchicheXture Entities inherit HasId anyway
                  if (exposure.equals(Exposure.ID) || (
                      exposure.equals(Exposure.DEFAULT) &&
                          (ExposureType.ID.equals(serializedClassAnnotation.exposeNested()) ||
                              ExposureType.BOTH.equals(serializedClassAnnotation.exposeNested()))
                  )) {
                    // only append "_id" if the name is not overridden OR if both (id AND object) are exposed
                    String keyNameToSet = exposureNameOverridden && !ExposureType.BOTH
                        .equals(serializedClassAnnotation.exposeNested()) ? keyName
                        : keyName + serializedClassAnnotation.idAppendix();
                    jsonObject.addProperty(keyNameToSet,
                        ((HasId) object).getId());
                  }
                  if (exposure.equals(Exposure.OBJECT) || (
                      exposure.equals(Exposure.DEFAULT) &&
                          (ExposureType.FULL.equals(serializedClassAnnotation.exposeNested()) ||
                              ExposureType.BOTH.equals(serializedClassAnnotation.exposeNested()))
                  )) {
                    jsonObject.add(keyName,
                        transferValuesToJson(fieldToSetOnEntity.getType().cast(object),
                            new JsonObject()));
                  }
                } else {
                  if (object instanceof Number) {
                    jsonObject.addProperty(keyName, (Number) object);
                  } else if (object instanceof Boolean) {
                    jsonObject.addProperty(keyName, (Boolean) object);
                  } else if (object instanceof Character) {
                    jsonObject.addProperty(keyName, (Character) object);
                  } else {
                    // try string if unknown
                    String stringValue = String.valueOf(object);
                    if (!Strings.isNullOrEmpty(stringValue)) {
                      jsonObject.addProperty(keyName, stringValue);
                    } else if (exportIfEmpty) {
                      jsonObject.addProperty(keyName, "");
                    }
                  }
                }
              } else if (exportIfEmpty) {
                jsonObject.add(keyName, null);
              }
            } catch (IllegalAccessException e) {
              // do nothing
            }
          }
        }
      }
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));
    return jsonObject;
  }

  /**
   * Search the whole inheritance Tree (classes and interfaces) of the given {@link Class} for the given {@link Annotation}
   */
  @SuppressWarnings("WeakerAccess")
  public static boolean isAnnotationPresent(@Nonnull Class<?> clazz,
      @Nonnull Class<? extends Annotation> annotation) {
    return null != getAnnotation(clazz, annotation);
  }

  private static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotation) {
    Class<?> classToWorkWith = clazz;
    do {
      if (classToWorkWith.isAnnotationPresent(annotation)) {
        return classToWorkWith.getAnnotation(annotation);
      }
      for (Class<?> ifc : classToWorkWith.getInterfaces()) {
        if (isAnnotationPresent(ifc, annotation)) {
          return ifc.getAnnotation(annotation);
        }
      }
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));
    return null;
  }

  private static class JsonResult {

    private String key;
    private JsonElement element;

    JsonResult(String key, JsonElement element) {
      this.key = key;
      this.element = element;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public JsonElement getElement() {
      return element;
    }

    public void setElement(JsonElement element) {
      this.element = element;
    }
  }
}
