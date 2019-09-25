package de.ialistannen.eventtracer.util.instantiate;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

/**
 * Instantiates objects using sun.Unsafe.
 */
public class UnsafeInstantiator implements ObjectInstantiator {

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

  @Override
  public <T> T instantiate(Class<T> theClass) {
    try {
      @SuppressWarnings("unchecked")
      T t = (T) myUnsafe.allocateInstance(theClass);
      return t;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
  }
}
