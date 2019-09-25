package de.ialistannen.eventtracer.example;

import de.ialistannen.eventtracer.RandomEvent;
import de.ialistannen.eventtracer.audit.AuditEvent;
import de.ialistannen.eventtracer.audit.AuditableAction;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * An example listener to show off some functionality.
 */
public class ExampleListener implements Listener {

  /**
   * Creates a new example listener and registers a fireEvent task.
   *
   * @param plugin the plugin
   */
  public ExampleListener(Plugin plugin) {
    Bukkit.getScheduler().runTaskTimer(
        plugin,
        () -> Bukkit.getPluginManager().callEvent(new RandomEvent()),
        20,
        5 * 20
    );
  }

  @EventHandler
  public void onAudit(AuditEvent event) {
    if (!(event.getSourceEvent() instanceof RandomEvent)) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage(
        ChatColor.GREEN + "Audit event for "
            + ChatColor.DARK_AQUA + ChatColor.BOLD
            + event.getSourceEvent().getClass().getSimpleName()
    );
    for (AuditableAction action : event.getActions()) {
      Bukkit.getConsoleSender().sendMessage(
          ChatColor.GOLD + action.getMethod().getName()
              + ChatColor.GRAY + "("
              + ChatColor.GREEN + Arrays.toString(action.getParameters())
              + ChatColor.GRAY + ") "
              + ChatColor.RED + action.getCallingPlugin()
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
    System.out.println(">>>>>>\n");
  }

  @EventHandler
  public void onRandom(RandomEvent event) {
    event.setNumber(50);
    int got = event.getNumber();
  }
}
