package de.ialistannen.eventtracer.interactive;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main event tracer command that allows you to watch and unwatch an event.
 */
public class EventTracerCommand implements CommandExecutor, TabCompleter {

  private final InteractiveListener listener;

  public EventTracerCommand(Plugin plugin) {
    this.listener = new InteractiveListener();
    Bukkit.getPluginManager().registerEvents(listener, plugin);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(ChatColor.RED + "Usage: " + label + " <event to watch>");
      return true;
    }

    try {
      Class<?> theClass = Class.forName(args[0]);

      if (!Event.class.isAssignableFrom(theClass)) {
        sender.sendMessage(ChatColor.RED + "That is no event class :(");
        return true;
      }

      @SuppressWarnings("unchecked")
      Class<? extends Event> asEventClass = (Class<? extends Event>) theClass;

      if(listener.toggleAuditEvent(sender, asEventClass)) {
        sender.sendMessage(ChatColor.GREEN + "You are now watching for that event.");
      } else {
        sender.sendMessage(ChatColor.RED + "You are no longer watching for that event.");
      }
    } catch (ClassNotFoundException e) {
      sender.sendMessage(ChatColor.RED + "Class not found :/");
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias,
      String[] args) {
    return Arrays.stream(Bukkit.getPluginManager().getPlugins())
        .flatMap(it -> getLoadedEvents(it).stream())
        .filter(it -> args.length == 0 || it.getSimpleName().startsWith(args[0]))
        .map(Class::getCanonicalName)
        .collect(Collectors.toList());
  }

  private List<Class<?>> getLoadedEvents(Plugin plugin) {
    try {
      Field classLoaderField = JavaPlugin.class.getDeclaredField("classLoader");
      classLoaderField.setAccessible(true);
      Object pluginClassLoader = classLoaderField.get(plugin);

      Field classesField = pluginClassLoader.getClass().getDeclaredField("classes");
      classesField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, Class<?>> classes = (Map<String, Class<?>>) classesField.get(pluginClassLoader);

      return classes.values().stream()
          .filter(Event.class::isAssignableFrom)
          .collect(Collectors.toList());
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
