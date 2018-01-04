package at.chex.archichexture.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

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
      Class<?> argumentType,
      String value) throws InvocationTargetException, IllegalAccessException {
    if (argumentType.equals(Boolean.class)) {
      Boolean convertedValue = Boolean.valueOf(value);
      return method.invoke(object, convertedValue);
    } else if (argumentType.equals(Date.class)) {
      Date convertedValue = new Date(Long.valueOf(value));
      return method.invoke(object, convertedValue);
    } else if (argumentType.equals(Integer.class)) {
      Integer convertedValue = Integer.valueOf(value);
      return method.invoke(object, convertedValue);
    } else if (argumentType.equals(Long.class)) {
      Long convertedValue = Long.valueOf(value);
      return method.invoke(object, convertedValue);
    } else if (argumentType.equals(BigDecimal.class)) {
      BigDecimal convertedValue = new BigDecimal(value);
      return method.invoke(object, convertedValue);
    } else {
      return method.invoke(object, value);
    }
  }
}
