package de.ialistannen.eventtracer.transform;

import de.ialistannen.eventtracer.audit.AuditableAction;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.OnMethodExit;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventProxy {

  private static Map<String, Class<? extends Event>> eventCache = new HashMap<>();

  public Event wrap(Event original) throws NoSuchMethodException {
    String canonicalName = original.getClass().getCanonicalName();

    if (!eventCache.containsKey(canonicalName)) {
      eventCache.put(canonicalName, buildProxy(original.getClass()));
    }

    return instantiate(original, eventCache.get(canonicalName));
  }

  private <T extends Event> Class<? extends T> buildProxy(Class<T> eventClass)
      throws NoSuchMethodException {
    ReceiverTypeDefinition<T> buddy = new ByteBuddy()
        .subclass(eventClass)
        .implement(ProxiedEvent.class)
        .defineField(
            "auditActions",
            List.class,
            Visibility.PRIVATE, Ownership.MEMBER
        )
        .defineField(
            "wrapped",
            Event.class,
            Visibility.PRIVATE, Ownership.MEMBER
        )

        .defineConstructor(Visibility.PUBLIC)
        .withParameters(Event.class)
        .intercept(
            MethodCall.invoke(Object.class.getConstructor())
                .andThen(FieldAccessor.ofField("wrapped").setsArgumentAt(0))
                .andThen(FieldAccessor.ofField("auditActions").setsValue(new ArrayList<>()))
        )

        .defineMethod("getActions", List.class)
        .intercept(FieldAccessor.ofField("auditActions"));

//        .method(ElementMatchers.any())
//        .intercept(Advice.to(ForwardCallsAdvice.class));

    return buddy.make()
        .load(eventClass.getClassLoader())
        .getLoaded();
  }

  private Event instantiate(Event bluePrint, Class<? extends Event> clazz) {
    throw new UnsupportedOperationException("Not implemented");
  }

  private static class ForwardCallsAdvice {

    @OnMethodExit
    public static Object whenCalled(
        @Advice.FieldValue("wrapped") Event wrapped,
        @Advice.FieldValue("auditActions") List<AuditableAction> auditableActions,
        @Advice.AllArguments Object[] arguments,
        @Advice.Origin Method method
    ) throws ReflectiveOperationException {
      System.out.println("Called " + method.getName());
      return method.invoke(wrapped, arguments);
    }
  }

  public static void main(String[] args) throws NoSuchMethodException {
    EventProxy eventProxy = new EventProxy();
    Event wrap = eventProxy.wrap(
        new PlayerTeleportEvent(null, new Location(null, 1, 1, 1), new Location(null, 1, 1, 1))
    );
    System.out.println(wrap.isAsynchronous());
  }
}
