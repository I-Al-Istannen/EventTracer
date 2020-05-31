package de.ialistannen.eventtracer.interactive;

import de.ialistannen.eventtracer.audit.AuditEvent;
import de.ialistannen.eventtracer.audit.AuditableAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * A listener for audit events tied to an {@link EventTracerCommand}.
 */
public class InteractiveListener implements Listener {

  private final Map<CommandSender, List<FilteredEvent<? extends Event>>> interestingEvents;

  public InteractiveListener() {
    this.interestingEvents = new HashMap<>();
  }

  /**
   * Adds some events to audit.
   *
   * @param sender the sender to send messages to
   * @param theClass the event class to watch or unwatch
   * @param filter a filter to apply
   * @return true if the sender is now watching the event
   */
  public <T extends Event> boolean toggleAuditEvent(CommandSender sender, Class<T> theClass,
      Predicate<T> filter) {
    boolean watching;
    interestingEvents.putIfAbsent(sender, new ArrayList<>());
    List<FilteredEvent<? extends Event>> watchedEvents = interestingEvents.get(sender);

    Optional<FilteredEvent<? extends Event>> filteredEvent = watchedEvents.stream()
        .filter(it -> it.clazz == theClass)
        .findFirst();

    if (filteredEvent.isPresent()) {
      watchedEvents.remove(filteredEvent.get());
      watching = false;
    } else {
      watchedEvents.add(new FilteredEvent<>(theClass, filter));
      watching = true;
    }

    if (watchedEvents.isEmpty()) {
      interestingEvents.remove(sender);
    }

    return watching;
  }

  @EventHandler
  public void onAuditEvent(AuditEvent event) {
    for (Entry<CommandSender, List<FilteredEvent<? extends Event>>> entry : interestingEvents.entrySet()) {
      entry.getValue()
          .stream()
          .filter(filteredEvent -> filteredEvent.test(event.getSourceEvent()))
          .forEach(filteredEvent -> notifyPlayer(entry.getKey(), event));
    }
  }

  private void notifyPlayer(CommandSender sender, AuditEvent event) {
    sender.sendMessage(
        ChatColor.GREEN + "Audit event for "
            + ChatColor.DARK_AQUA + ChatColor.BOLD
            + event.getSourceEvent().getClass().getSimpleName()
    );

    for (AuditableAction action : event.getActions()) {
      String pluginName = action.getCallingPlugin()
          .map(Plugin::getName)
          .orElse("Unknown plugin");
      String parameters = Arrays.stream(action.getParameters())
          .map(Objects::toString)
          .collect(Collectors.joining(", "));

      sender.sendMessage(
          ChatColor.GOLD + action.getMethod().getName()
              + ChatColor.GRAY + "("
              + ChatColor.GREEN + parameters
              + ChatColor.GRAY + ") "
              + ChatColor.RED + pluginName
              + ChatColor.GRAY + "\n" + Arrays.stream(action.getStackTrace())
              .limit(2)
              .map(it -> {
                int end = it.getClassName().lastIndexOf('.');
                String name = it.getClassName();
                if (end > 0) {
                  name = it.getClassName().substring(end + 1);
                }
                return "  " + name + "#" + it.getMethodName();
              })
              .collect(Collectors.joining(", "))
      );
    }

    sender.sendMessage(
        ChatColor.LIGHT_PURPLE + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 20)
    );
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerLeave(PlayerQuitEvent event) {
    interestingEvents.remove(event.getPlayer());
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setFormat(
        ChatColor.RED + "<" + ChatColor.AQUA + "%s" + ChatColor.RED + "> " + ChatColor.DARK_GRAY
            + "%s"
    );
  }

  private static class FilteredEvent<T extends Event> implements Predicate<Event> {

    private final Class<T> clazz;
    private final Predicate<T> filter;

    FilteredEvent(Class<T> clazz, Predicate<T> filter) {
      this.clazz = clazz;
      this.filter = filter;
    }

    @Override
    public boolean test(Event event) {
      return event.getClass() == clazz && filter.test(clazz.cast(event));
    }
  }
}
