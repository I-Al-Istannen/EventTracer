package de.ialistannen.eventtracer.transform;

import de.ialistannen.eventtracer.audit.AuditableAction;
import de.ialistannen.eventtracer.reflect.FieldByFieldCopy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import sun.misc.Unsafe;

public class EventProxy {

  private static Map<String, Class<? extends Event>> eventCache = new HashMap<>();

  public Event wrap(Event original, ClassLoader targetClassLoader) {
    String canonicalName = original.getClass().getCanonicalName();

    if (!eventCache.containsKey(canonicalName)) {
      eventCache.put(canonicalName, buildProxyClass(original.getClass(), targetClassLoader));
    }

    Class<? extends Event> proxyClass = eventCache.get(canonicalName);

    return instantiate(original, proxyClass);
  }

  private <T extends Event> Class<? extends T> buildProxyClass(Class<T> eventClass,
      ClassLoader loader) {
    DynamicType.Builder<T> buddy = new ByteBuddy()
        .subclass(eventClass)
        .name(eventClass.getSimpleName() + "_IAlEventProxy")
        .implement(ProxiedEvent.class)
        .defineField("original", eventClass, Visibility.PUBLIC)
        .defineField(
            "actions",
            List.class,
            Visibility.PUBLIC, Ownership.MEMBER
        )
        .defineMethod("getActions", List.class)
        .intercept(FieldAccessor.ofField("actions"));

    Class<?> currentClass = eventClass;
    Set<MethodSignature> encountered = new HashSet<>();
    while (currentClass != Object.class) {
      for (Method method : currentClass.getDeclaredMethods()) {
        // we are out of luck. Future work: Redefine existing classes to strip final modifiers
        if (Modifier.isFinal(method.getModifiers())) {
          continue;
        }
        // Makes no sense, they should have no state and won't be dynamic dispatched anyways
        if (Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        // We can not access those, but neither can other plugins
        if (Modifier.isPrivate(method.getModifiers())) {
          continue;
        }
        // Do not add duplicates
        if (!encountered.add(new MethodSignature(method))) {
          continue;
        }
        buddy = buddy.defineMethod(method.getName(), method.getReturnType(), method.getModifiers())
            .withParameters(method.getParameterTypes())
            .intercept(MethodDelegation.to(Delegator.class));
      }
      currentClass = currentClass.getSuperclass();
    }

    return buddy.make()
        .load(loader, Default.INJECTION)
        .getLoaded();
  }

  public static class Delegator {

    @RuntimeType
    public static Object intercept(
        @FieldValue("actions") List<AuditableAction> actions,
        @Origin Method source,
        @FieldValue("original") Object realGuy,
        @AllArguments Object[] arguments
    ) throws Exception {
      StackTraceElement[] elements = Arrays.stream(Thread.currentThread().getStackTrace())
          .skip(3) // getStackTrace, this method and the proxy method call
          .toArray(StackTraceElement[]::new);
      actions.add(new AuditableAction(getPlugin(elements), source, arguments, elements));
      MethodSignature sourceSig = new MethodSignature(source);

      for (Method method : realGuy.getClass().getMethods()) {
        if (new MethodSignature(method).equals(sourceSig)) {
          return method.invoke(realGuy, arguments);
        }
      }
      for (Method method : realGuy.getClass().getDeclaredMethods()) {
        if (new MethodSignature(method).equals(sourceSig)) {
          method.setAccessible(true);
          return method.invoke(realGuy, arguments);
        }
      }
      throw new IllegalArgumentException(":(");
    }

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

  private static class MethodSignature {

    private String name;
    private Class<?> returnValue;
    private Class<?>[] parameter;

    private MethodSignature(Method method) {
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

  private Event instantiate(Event original, Class<? extends Event> proxyClass) {
    try {
      Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      Unsafe unsafe = (Unsafe) theUnsafe.get(null);
      Event proxy = (Event) unsafe.allocateInstance(proxyClass);

      proxy.getClass().getDeclaredField("original").set(proxy, original);
      proxy.getClass().getDeclaredField("actions").set(proxy, new ArrayList<>());

      FieldByFieldCopy.copyStateOver(original, proxy);

      return proxy;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws Exception {
    EventProxy eventProxy = new EventProxy();
    PlayerTeleportEvent wrap = (PlayerTeleportEvent) eventProxy.wrap(
        new PlayerTeleportEvent(
            null,
            new Location(null, 1, 1, 1),
            new Location(null, 1, 1, 1),
            TeleportCause.END_GATEWAY
        ),
        EventProxy.class.getClassLoader()
    );
    System.out.println(wrap.getCause());
    System.out.println(wrap.getClass().getDeclaredField("actions").get(wrap));
  }
}
