package de.ialistannen.eventtracer.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a method signature with valid equals and hashcode.
 */
public class MethodSignature {

  private String name;
  private Class<?> returnValue;
  private Class<?>[] parameter;

  /**
   * Creates a new signature for a given method.
   *
   * @param method the method
   */
  public MethodSignature(Method method) {
    this.name = method.getName();
    this.returnValue = method.getReturnType();
    this.parameter = method.getParameterTypes();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MethodSignature that = (MethodSignature) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(returnValue, that.returnValue) &&
        Arrays.equals(parameter, that.parameter);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name, returnValue);
    result = 31 * result + Arrays.hashCode(parameter);
    return result;
  }
}
