package de.ialistannen.eventtracer.transform.eventclasses;

import de.ialistannen.eventtracer.reflect.FieldByFieldCopy;
import de.ialistannen.eventtracer.util.MethodSignature;
import de.ialistannen.eventtracer.util.ProxiedEvent;
import de.ialistannen.eventtracer.util.instantiate.ObjectInstantiator;
import de.ialistannen.eventtracer.util.instantiate.ObjenesisInstantiator;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import org.bukkit.event.Event;

/**
 * The base class for creating event proxies.
 *
 * <p><br><strong>{@link #clearCache()} must be called in onDisable!</strong></p>
 */
public class EventProxy {

  /**
   * A map with cached proxy classes.
   */
  private static Map<String, Class<? extends Event>> eventCache = new ConcurrentHashMap<>();

  private ObjectInstantiator objectInstantiator = new ObjenesisInstantiator();

  /**
   * Wraps an event in a {@link ProxiedEvent}.
   *
   * @param original the original event
   * @param targetClassLoader the class loader to load the proxy class into. This loader needs
   *     to have access to event tracer classes, so a PluginClassLoader would be a good choice
   * @return the wrapped event
   */
  public Event wrap(Event original, ClassLoader targetClassLoader) {
    String canonicalName = original.getClass().getCanonicalName();

    eventCache.computeIfAbsent(
        canonicalName,
        ignored -> buildProxyClass(original.getClass(), targetClassLoader)
    );

    Class<? extends Event> proxyClass = eventCache.get(canonicalName);

    return instantiate(original, proxyClass);
  }

  private <T extends Event> Class<? extends T> buildProxyClass(Class<T> eventClass,
      ClassLoader loader) {
    DynamicType.Builder<T> buddy = new ByteBuddy()
        .subclass(eventClass)
        .name(eventClass.getSimpleName() + "_IAlEventProxy")
        .implement(ProxiedEvent.class)
        .defineField(
            ProxyFieldNames.ORIGINAL,
            eventClass, Visibility.PUBLIC
        )
        .defineField(
            ProxyFieldNames.ACTIONS,
            List.class,
            Visibility.PUBLIC, Ownership.MEMBER
        )
        .defineMethod("getActions", List.class)
        .intercept(FieldAccessor.ofField(ProxyFieldNames.ACTIONS));

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
            .intercept(MethodDelegation.to(LoggingMethodDelegator.class));
      }
      currentClass = currentClass.getSuperclass();
    }

    return buddy.make()
        .load(loader, Default.INJECTION)
        .getLoaded();
  }

  private Event instantiate(Event original, Class<? extends Event> proxyClass) {
    try {
      Event proxy = objectInstantiator.instantiate(proxyClass);

      proxy.getClass().getDeclaredField(ProxyFieldNames.ORIGINAL).set(proxy, original);
      proxy.getClass().getDeclaredField(ProxyFieldNames.ACTIONS).set(proxy, new ArrayList<>());

      FieldByFieldCopy.copyStateOver(original, proxy);

      return proxy;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Clears all cached instrumented classes.
   *
   * <p><br><strong>Must be called in onDisable!</strong></p>
   */
  public static void clearCache() {
    eventCache = new ConcurrentHashMap<>();
  }
}
