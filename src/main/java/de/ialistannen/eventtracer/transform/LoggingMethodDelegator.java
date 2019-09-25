package de.ialistannen.eventtracer.transform;

import de.ialistannen.eventtracer.audit.AuditableAction;
import de.ialistannen.eventtracer.util.MethodSignature;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.bukkit.plugin.Plugin;

/**
 * A byte buddy method call delegator that logs information into the {@link ProxyFieldNames#ACTIONS}
 * field.
 */
public class LoggingMethodDelegator {

  /**
   * Intercepts a call and logs it.
   *
   * @param actions the stored actions
   * @param realObject the real object to forward calls to
   * @param calledMethod the source method
   * @param arguments the arguments to the called method
   * @return the result of the method call
   * @throws Exception if anything happens
   */
  @RuntimeType
  public static Object intercept(
      @FieldValue(ProxyFieldNames.ACTIONS) List<AuditableAction> actions,
      @FieldValue(ProxyFieldNames.ORIGINAL) Object realObject,
      @Origin Method calledMethod,
      @AllArguments Object[] arguments
  ) throws Exception {
    StackTraceElement[] elements = Arrays.stream(Thread.currentThread().getStackTrace())
        .skip(3) // getStackTrace, this method and the proxy method call
        .toArray(StackTraceElement[]::new);
    actions.add(new AuditableAction(getPlugin(elements), calledMethod, arguments, elements));

    MethodSignature sourceSig = new MethodSignature(calledMethod);

    for (Method method : realObject.getClass().getMethods()) {
      if (new MethodSignature(method).equals(sourceSig)) {
        return method.invoke(realObject, arguments);
      }
    }
    for (Method method : realObject.getClass().getDeclaredMethods()) {
      if (new MethodSignature(method).equals(sourceSig)) {
        method.setAccessible(true);
        return method.invoke(realObject, arguments);
      }
    }
    throw new IllegalArgumentException(
        "No forwardable method found for " + calledMethod + " on " + realObject.getClass()
    );
  }

  /**
   * Finds the first class loaded by a PluginClassLoader and returns its associated plugin.
   *
   * @param elements the stack trace
   * @return the found plugin or null if none
   * @throws ReflectiveOperationException if an error occurs
   */
  private static Plugin getPlugin(StackTraceElement[] elements)
      throws ReflectiveOperationException {
    for (StackTraceElement element : elements) {
      try {
        ClassLoader classLoader = Class.forName(element.getClassName()).getClassLoader();
        if (classLoader.getClass().getSimpleName().equalsIgnoreCase("PluginClassLoader")) {
          Field pluginField = classLoader.getClass().getDeclaredField("plugin");
          pluginField.setAccessible(true);
          return (Plugin) pluginField.get(classLoader);
        }
      } catch (ClassNotFoundException ignored) {
      }
    }
    return null;
  }
}
