package de.ialistannen.eventtracer.audit;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.plugin.Plugin;

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

  public Plugin getCallingPlugin() {
    return callingPlugin;
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getParameters() {
    return parameters;
  }

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
