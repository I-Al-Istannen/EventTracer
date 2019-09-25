package de.ialistannen.eventtracer;

import de.ialistannen.eventtracer.audit.AuditEvent;
import de.ialistannen.eventtracer.audit.AuditableAction;
import de.ialistannen.eventtracer.transform.bukkit.PluginManagerFireEventInterceptor;
import de.ialistannen.eventtracer.transform.eventclasses.EventProxy;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
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

    if (System.getProperty(RELOAD_MARKER) == null) {
      Instrumentation instrumentation = ByteBuddyAgent.install();
      new Default()
          .disableClassFormatChanges()
          .with(RedefinitionStrategy.RETRANSFORMATION)
          .with(RedefinitionStrategy.RETRANSFORMATION)
          .type(ElementMatchers.named(SimplePluginManager.class.getCanonicalName()))
          .transform(
              new ForAdvice().advice(
                  ElementMatchers.named("fireEvent"),
                  PluginManagerFireEventInterceptor.class.getCanonicalName()
              )
                  .include(getClassLoader(), Bukkit.class.getClassLoader())
          )
          .installOn(instrumentation);

      System.setProperty(RELOAD_MARKER, "true");
    } else {
      getLogger().log(Level.INFO, "Reload detected - Not instrumenting classes");
    }

    Bukkit.getScheduler().runTaskTimer(this, () -> {
      System.out.println("Fired!");
      Bukkit.getPluginManager().callEvent(new RandomEvent());
    }, 20, 5 * 20);

    Bukkit.getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onAudit(AuditEvent event) {
    if (!(event.getSourceEvent() instanceof RandomEvent)) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage(
        ChatColor.GRAY + "Audit event for "
            + ChatColor.DARK_AQUA + event.getSourceEvent().getClass().getSimpleName()
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
    System.out.println("\n_");
  }

  @EventHandler
  public void onRandom(RandomEvent event) {
    event.setNumber(50);
    int got = event.getNumber();
  }

  @Override
  public void onDisable() {
    EventProxy.clearCache();
  }
}
