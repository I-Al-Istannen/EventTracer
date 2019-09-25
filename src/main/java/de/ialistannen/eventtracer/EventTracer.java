package de.ialistannen.eventtracer;

import com.google.common.io.Files;
import de.ialistannen.eventtracer.audit.AuditEvent;
import de.ialistannen.eventtracer.audit.AuditableAction;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer.ForAdvice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin base.
 */
public final class EventTracer extends JavaPlugin implements org.bukkit.event.Listener {

  @Override
  public void onEnable() {
    if (!AttachmentProvider.DEFAULT.attempt().isAvailable()) {
      getLogger().log(Level.SEVERE, "Attachment not possible :(");
      getPluginLoader().disablePlugin(this);
      return;
    }

    File tempDir = Files.createTempDir();
    tempDir.deleteOnExit();

    Instrumentation instrumentation = ByteBuddyAgent.install();

    new Default()
        .disableClassFormatChanges()
        .with(RedefinitionStrategy.RETRANSFORMATION)
        .with(RedefinitionStrategy.RETRANSFORMATION)
        .with(new Listener() {
          @Override
          public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module,
              boolean loaded) {

          }

          @Override
          public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader,
              JavaModule module, boolean loaded, DynamicType dynamicType) {
            System.out.println(
                "typeDescription = " + typeDescription + ", classLoader = " + classLoader
                    + ", module = " + module + ", loaded = " + loaded + ", dynamicType = "
                    + dynamicType);
          }

          @Override
          public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader,
              JavaModule module, boolean loaded) {

          }

          @Override
          public void onError(String typeName, ClassLoader classLoader, JavaModule module,
              boolean loaded, Throwable throwable) {
            System.out.println(
                "typeName = " + typeName + ", classLoader = " + classLoader + ", module = " + module
                    + ", loaded = " + loaded + ", throwable = " + throwable);
          }

          @Override
          public void onComplete(String typeName, ClassLoader classLoader, JavaModule module,
              boolean loaded) {

          }
        })
        .type(ElementMatchers.named(SimplePluginManager.class.getCanonicalName()))
        .transform(
            new ForAdvice().advice(
                ElementMatchers.named("fireEvent"),
                MyGeneralInterceptor.class.getCanonicalName()
            )
                .include(getClassLoader(), Bukkit.class.getClassLoader())
        )
        .installOn(instrumentation);

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

  @Override
  public void onDisable() {
  }
}
