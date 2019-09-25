package de.ialistannen.eventtracer;

import de.ialistannen.eventtracer.transform.ProxyFieldNames;
import java.util.List;
import net.bytebuddy.asm.Advice;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class MyGeneralInterceptor {

  @Advice.OnMethodEnter
  public static Event enter(@Advice.Argument(value = 0, readOnly = false) Event argument)
      throws Exception {
    // No infinite loop today
    if (argument.getClass().getName().equals("de.ialistannen.eventtracer.audit.AuditEvent")) {
      return argument;
    }
    Plugin plugin = Bukkit.getPluginManager().getPlugin("EventTracer");
    ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
    Class<?> eventProxy = pluginClassLoader.loadClass(
        "de.ialistannen.eventtracer.transform.EventProxy"
    );
    Event wrappedEvent = (Event) eventProxy.getMethod("wrap", Event.class, ClassLoader.class)
        .invoke(
            eventProxy.getConstructor().newInstance(), argument, pluginClassLoader
        );
    argument = wrappedEvent;

    return wrappedEvent;
  }

  @Advice.OnMethodExit
  public static void exit(@Advice.Enter Event event) throws Exception {
    // No infinite loop today
    if (event.getClass().getName().equals("de.ialistannen.eventtracer.audit.AuditEvent")) {
      return;
    }

    Plugin plugin = Bukkit.getPluginManager().getPlugin("EventTracer");
    ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
    Class<?> auditEventClass = pluginClassLoader.loadClass(
        "de.ialistannen.eventtracer.audit.AuditEvent"
    );
    List<?> actions = (List<?>) event.getClass().getField(ProxyFieldNames.ACTIONS).get(event);
    Event originalEvent = (Event) event.getClass().getField(ProxyFieldNames.ORIGINAL).get(event);

    Event auditEvent = (Event) auditEventClass
        .getConstructor(List.class, Event.class)
        .newInstance(actions, originalEvent);

    Bukkit.getPluginManager().callEvent(auditEvent);
  }
}
