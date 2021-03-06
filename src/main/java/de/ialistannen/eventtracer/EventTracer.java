package de.ialistannen.eventtracer;

import de.ialistannen.eventtracer.interactive.InteractiveListener;
import de.ialistannen.eventtracer.interactive.ListWatchesCommand;
import de.ialistannen.eventtracer.interactive.LoadedEventsHelper;
import de.ialistannen.eventtracer.interactive.WatchCommand;
import de.ialistannen.eventtracer.transform.bukkit.PluginManagerFireEventInterceptor;
import de.ialistannen.eventtracer.transform.eventclasses.EventProxy;
import java.lang.instrument.Instrumentation;
import java.util.Objects;
import java.util.logging.Level;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin base.
 */
public final class EventTracer extends JavaPlugin implements org.bukkit.event.Listener {

  private static final String RELOAD_MARKER = "I_Al_EventTracer_Has_Retransformed";

  @Override
  public void onEnable() {
    if (!AttachmentProvider.DEFAULT.attempt().isAvailable()) {
      getLogger().log(Level.SEVERE, "Attachment not possible :(");
      getPluginLoader().disablePlugin(this);
      return;
    }

    LoadedEventsHelper.scan(this);

    if (System.getProperty(RELOAD_MARKER) == null) {
      Instrumentation instrumentation = ByteBuddyAgent.install();
      new Default()
          .disableClassFormatChanges()
          .with(RedefinitionStrategy.RETRANSFORMATION)
          .with(RedefinitionStrategy.RETRANSFORMATION)
          .type(ElementMatchers.named(SimplePluginManager.class.getCanonicalName()))
          .transform(
              new ForAdvice().advice(
                  ElementMatchers.named("callEvent"),
                  PluginManagerFireEventInterceptor.class.getCanonicalName()
              )
                  .include(getClassLoader(), Bukkit.class.getClassLoader())
          )
          .installOn(instrumentation);

      System.setProperty(RELOAD_MARKER, "true");
    } else {
      getLogger().log(Level.INFO, "Reload detected - Not instrumenting classes");
    }

    InteractiveListener listener = new InteractiveListener();
    Objects.requireNonNull(getCommand("watch")).setExecutor(new WatchCommand(this, listener));
    Objects.requireNonNull(getCommand("listWatches")).setExecutor(new ListWatchesCommand(listener));
  }

  @Override
  public void onDisable() {
    EventProxy.clearCache();
  }
}
