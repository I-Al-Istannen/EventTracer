package de.ialistannen.eventtracer.util.instantiate;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisHelper;

/**
 * An instantiator using {@link Objenesis}.
 */
public class ObjenesisInstantiator implements ObjectInstantiator {

  @Override
  public <T> T instantiate(Class<T> theClass) {
    return ObjenesisHelper.newInstance(theClass);
  }
}
