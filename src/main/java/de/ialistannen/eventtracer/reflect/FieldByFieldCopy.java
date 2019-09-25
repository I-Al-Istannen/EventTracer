package de.ialistannen.eventtracer.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Makes a field by field copy of an object, by copying all declared fields from source to target.
 */
public class FieldByFieldCopy {

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

        if (Modifier.isFinal(field.getModifiers())) {
          crackFinalField(field);
        }

        if (currentClass.isAssignableFrom(target.getClass())) {
          field.setAccessible(true);
          Object value = field.get(source);
          field.set(target, value);
        }
      }

      currentClass = currentClass.getSuperclass();
    }
  }

  private static void crackFinalField(Field field) throws ReflectiveOperationException {
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
  }
}
