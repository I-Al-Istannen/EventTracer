package de.ialistannen.eventtracer.interactive;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LoadedEventsHelper {

  private static volatile List<String> eventClasses = new ArrayList<>();

  /**
   * Starts scanning for plugins.
   *
   * @param plugin the plugin
   */
  public static void scan(Plugin plugin) {
    Future<ScanResult> scanResultFuture = new ClassGraph().enableClassInfo().scanAsync(
        Executors.newFixedThreadPool(5), 5
    );
    new BukkitRunnable() {
      @Override
      public void run() {
        try {
          ScanResult scanResult = scanResultFuture.get();
          eventClasses = new ArrayList<>(
              scanResult.getSubclasses(Event.class.getCanonicalName()).getNames()
          );
          plugin.getLogger()
              .info("Event information loaded. Found " + eventClasses.size() + " classes.");
        } catch (InterruptedException | ExecutionException e) {
          plugin.getLogger().log(
              Level.WARNING,
              "Could not scan for event classes. Tab completion will not work",
              e
          );
        }
      }
    }.runTaskAsynchronously(plugin);
  }

  /**
   * Returns all event classes. Might be empty, if a scan is still in progress.
   *
   * @return a list with all classes
   */
  public static List<String> getEventClasses() {
    return Collections.unmodifiableList(eventClasses);
  }
}
