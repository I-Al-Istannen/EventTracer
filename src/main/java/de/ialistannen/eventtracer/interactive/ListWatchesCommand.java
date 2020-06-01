package de.ialistannen.eventtracer.interactive;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

/**
 * Lists all events you are watching.
 */
public class ListWatchesCommand implements CommandExecutor {

  private final InteractiveListener listener;

  public ListWatchesCommand(InteractiveListener listener) {
    this.listener = listener;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    List<Class<? extends Event>> watchedClasses = listener.getWatchedEventClasses(sender);

    String events = watchedClasses.stream()
        .map(Class::getCanonicalName)
        .map(it -> ChatColor.AQUA + it)
        .collect(Collectors.joining(ChatColor.GRAY + ", "));

    if (watchedClasses.isEmpty()) {
      sender.sendMessage(ChatColor.RED + "You are not watching for any event.");
    } else {
      sender.sendMessage(ChatColor.GREEN + "You are currently watching for " + events + ".");
    }

    return true;
  }
}
