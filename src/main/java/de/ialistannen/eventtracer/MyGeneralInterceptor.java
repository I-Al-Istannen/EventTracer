package de.ialistannen.eventtracer;

import de.ialistannen.eventtracer.transform.EventProxy;
import java.lang.reflect.Method;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

public class MyGeneralInterceptor {

  private static EventProxy proxy = new EventProxy();

  @RuntimeType
  public static void intercept(@Argument(0) Event argument,
      @Origin(cache = false) Method method,
      @This Object thiz
  ) throws Exception {
    if (!(argument instanceof RandomEvent)) {
      System.out.println("Hello world");
      method.setAccessible(true);
      method.invoke(thiz, argument);
      return;
    }
    System.out.println("Ay");
    Event wrapped = proxy.wrap(argument,
        JavaPlugin.getPlugin(EventTracer.class).getClass().getClassLoader());
    method.setAccessible(true);
    method.invoke(thiz, wrapped);
    System.out.println(wrapped);
  }
}
