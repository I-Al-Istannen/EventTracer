package de.ialistannen.eventtracer.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldByFieldCopy {

  public static void copyStateOver(Object source, Object target)
      throws ReflectiveOperationException {
    Class<?> currentClass = source.getClass();

    while (currentClass != Object.class) {
      for (Field field : currentClass.getDeclaredFields()) {
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
