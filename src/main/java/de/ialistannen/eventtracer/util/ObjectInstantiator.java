package de.ialistannen.eventtracer.util;

/**
 * Creates instances of objects.
 */
@FunctionalInterface
public interface ObjectInstantiator {

  /**
   * Instantiates a raw, blank object-
   *
   * @param theClass the class to instantiate
   * @param <T> the type of the class
   * @return the instantiated object
   */
  <T> T instantiate(Class<T> theClass);

}
