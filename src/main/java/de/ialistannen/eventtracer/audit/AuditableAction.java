package de.ialistannen.eventtracer.audit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

/**
 * An action (e.g. calling a method) performed by some plugin that might be interesting.
 */
public class AuditableAction {

  private final Plugin callingPlugin;
  private final Method method;
  private final Object[] parameters;
  private final StackTraceElement[] stackTrace;

  public AuditableAction(Plugin callingPlugin, Method method, Object[] parameters,
      StackTraceElement[] stackTrace) {
    this.callingPlugin = callingPlugin;
    this.method = method;
    this.parameters = parameters;
    this.stackTrace = stackTrace;
  }

  /**
   * Returns the calling plugin that performed this action.
   *
   * @return the calling plugin
   */
  public Optional<Plugin> getCallingPlugin() {
    return Optional.ofNullable(callingPlugin);
  }

  /**
   * Returns the called method.
   *
   * @return the called method
   */
  public Method getMethod() {
    return method;
  }

  /**
   * Returns the method parameters.
   *
   * @return the method parameters
   */
  public Object[] getParameters() {
    return parameters;
  }

  /**
   * Returns the stacktrace.
   *
   * @return the stacktrace
   */
  public StackTraceElement[] getStackTrace() {
    return stackTrace;
  }

  @Override
  public String toString() {
    return "AuditableAction{" +
        "callingPlugin=" + callingPlugin +
        ", method=" + method +
        ", parameters=" + Arrays.toString(parameters) +
        ", stackTrace=" + Arrays.toString(stackTrace) +
        '}';
  }
}
