package de.ialistannen.eventtracer.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import sun.misc.Unsafe;

/**
 * Makes a field by field copy of an object, by copying all declared fields from source to target.
 */
public class FieldByFieldCopy {

  private static final Unsafe myUnsafe;

  static {
    try {
      Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      myUnsafe = (Unsafe) theUnsafe.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Makes a field by field copy of an object, by copying all declared fields from source to
   * target.
   *
   * @param source the source object
   * @param target the target object
   * @throws ReflectiveOperationException if an error occurs getting or setting values
   */
  public static void copyStateOver(Object source, Object target)
      throws ReflectiveOperationException {
    Class<?> currentClass = source.getClass();

    while (currentClass != Object.class) {
      for (Field field : currentClass.getDeclaredFields()) {
        // Static fields are not our business, as they should be no instance state
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }

        if (currentClass.isAssignableFrom(target.getClass())) {
          field.setAccessible(true);
          Object value = field.get(source);

          final long offset = myUnsafe.objectFieldOffset(field);
          if (field.getType().isPrimitive()) {
            switch (field.getType().getSimpleName()) {
              case "boolean":
                myUnsafe.putBoolean(target, offset, (Boolean) value);
                break;
              case "byte":
                myUnsafe.putByte(target, offset, (Byte) value);
                break;
              case "short":
                myUnsafe.putShort(target, offset, (Short) value);
                break;
              case "int":
                myUnsafe.putInt(target, offset, (Integer) value);
                break;
              case "float":
                myUnsafe.putFloat(target, offset, (Float) value);
                break;
              case "double":
                myUnsafe.putDouble(target, offset, (Double) value);
                break;
            }
          } else {
            myUnsafe.putObject(target, offset, value);
          }
        }
      }

      currentClass = currentClass.getSuperclass();
    }
  }
}
