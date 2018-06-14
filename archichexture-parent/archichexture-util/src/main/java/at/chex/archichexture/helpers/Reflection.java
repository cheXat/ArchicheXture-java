package at.chex.archichexture.helpers;

import at.chex.archichexture.HasId;
import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
import at.chex.archichexture.annotation.Exposed;
import at.chex.archichexture.annotation.Exposed.Exposure;
import at.chex.archichexture.annotation.Exposed.Visibility;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 04.01.18
 */
public class Reflection {

  private Reflection() {
  }

  /**
   * Will return the method with the given name in the given object and its inherited classes
   */
  @SuppressWarnings("WeakerAccess")
  @NotNull
  public static List<Field> getAnnotatedFields(Class<? extends Annotation> annotation,
      Class<?> clazz) {
    List<Field> returnList = new ArrayList<>();

    Class<?> classToWorkWith = clazz;
    do {
      returnList.addAll(innerGetAnnotatedFields(annotation, clazz));
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
  @Nullable
  public static <TYPE> TYPE transferValuesFromLeftToRight(Object left, TYPE right) {
    Class<?> classToWorkWith = right.getClass();
    // loop through the class hierarchy
    do {

      // loop through all fields on the target class
      for (Field fieldToSetOnEntity : classToWorkWith.getDeclaredFields()) {

        // check for @Aspect annotations
        if (fieldToSetOnEntity.isAnnotationPresent(Aspect.class)) {
          Aspect aspect = fieldToSetOnEntity.getAnnotation(Aspect.class);
          if (aspect.modifieable()) {
            List<String> filterNames = new ArrayList<>();
            filterNames.add(fieldToSetOnEntity.getName());

            // check for any @AlternativeNames given to this field (and try to find it in the source class)
            if (fieldToSetOnEntity.isAnnotationPresent(AlternativeNames.class)) {
              AlternativeNames alternativeNames = fieldToSetOnEntity
                  .getAnnotation(AlternativeNames.class);
              filterNames.addAll(Arrays.asList(alternativeNames.value()));
            }

            // we need to use another accessor for a jsonObject
            if (left instanceof JsonObject) {
              JsonObject jsonObject = (JsonObject) left;
              String elementValue = getJsonElementByNameList(jsonObject, filterNames);
              if (!Strings.isNullOrEmpty(elementValue)) {
                try {
                  fieldToSetOnEntity.setAccessible(true);
                  if (!fieldToSetOnEntity.getType().equals(String.class)) {
                    Object convertedValue = Values
                        .convert(elementValue, fieldToSetOnEntity.getType());
                    fieldToSetOnEntity.set(right, convertedValue);
                  } else {
                    fieldToSetOnEntity.set(right, elementValue);
                  }
                } catch (IllegalAccessException e) {
                  // do nothing
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

  private static String getJsonElementByNameList(JsonObject jsonObject, List<String> nameList) {
    for (String name : nameList) {
      JsonElement jsonElement = jsonObject.get(name);
      if (null != jsonElement) {
        return jsonElement.getAsString();
      }
    }
    return null;
  }

  @SuppressWarnings("WeakerAccess")
  public static <TYPE> JsonObject transferValuesToJson(TYPE left, JsonObject jsonObject) {
    Class<?> classToWorkWith = left.getClass();
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
            if (fieldToSetOnEntity.isAnnotationPresent(Exposed.class)) {
              Exposed annotation = fieldToSetOnEntity.getAnnotation(Exposed.class);
              if (!Strings.isNullOrEmpty(annotation.exposedName())) {
                keyName = annotation.exposedName();
              }
              exportIfEmpty = annotation.exposeIfEmpty();
              exposure = annotation.exposeAs();
            }

            try {
              fieldToSetOnEntity.setAccessible(true);
              Object object = fieldToSetOnEntity.get(left);
              if (null != object) {
                if (object instanceof HasId) { // if this is an object, we can actually handle. All ArchicheXture Entities inherit HasId anyway
                  if (exposure.equals(Exposure.ID)) {
                    jsonObject.addProperty(keyName, ((HasId) object).getId());
                  } else {
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
}
