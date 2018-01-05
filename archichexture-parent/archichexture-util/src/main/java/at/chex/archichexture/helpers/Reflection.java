package at.chex.archichexture.helpers;

import at.chex.archichexture.annotation.AlternativeNames;
import at.chex.archichexture.annotation.Aspect;
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

  public static Field getFieldFromClassInStringList(Class<?> clazz, Collection<String> list) {
    if (null == clazz || null == list || list.isEmpty()) {
      return null;
    }
    for (String value : list) {
      try {
        Field field = clazz.getDeclaredField(value);
        if (null != field) {
          return field;
        }
      } catch (NoSuchFieldException e) {
        // do nothing
      }
    }
    return null;
  }

  public static Method getMethodByReflection(Object field, String methodToInvoke,
      Class<?> argumentType)
      throws NoSuchMethodException {
    try {
      return field.getClass()
          .getMethod(methodToInvoke, argumentType);
    } catch (NoSuchMethodException e) {
      return field.getClass()
          .getMethod(methodToInvoke, Object.class);
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
  public static <TYPE> TYPE transferValuesFromLeftToRight(Object left, TYPE right) {
    Class<?> classToWorkWith = right.getClass();
    do {
      for (Field fieldToSetOnEntity : classToWorkWith.getDeclaredFields()) {
        if (fieldToSetOnEntity.isAnnotationPresent(Aspect.class)) {
          Aspect aspect = fieldToSetOnEntity.getAnnotation(Aspect.class);
          if (aspect.modifieable()) {
            List<String> filterNames = new ArrayList<>();
            filterNames.add(fieldToSetOnEntity.getName());
            if (fieldToSetOnEntity.isAnnotationPresent(AlternativeNames.class)) {
              AlternativeNames alternativeNames = fieldToSetOnEntity
                  .getAnnotation(AlternativeNames.class);
              filterNames.addAll(Arrays.asList(alternativeNames.value()));
            }

            Field valueFieldFromFormObject = Reflection
                .getFieldFromClassInStringList(left.getClass(), filterNames);

            if (null != valueFieldFromFormObject) {
              Object object = null;
              try {
                valueFieldFromFormObject.setAccessible(true);
                object = valueFieldFromFormObject.get(left);
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
    } while (null != (classToWorkWith = classToWorkWith.getSuperclass()));
    return right;
  }
}
