package de.ialistannen.eventtracer;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;
import java.util.logging.Level;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider;
import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin base.
 */
public final class EventTracer extends JavaPlugin {

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
    try {
      instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(getFile()));
    } catch (IOException e) {
      getLogger().log(Level.SEVERE, "Oh no", e);
    }

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
        .transform((builder, typeDescription, classLoader, module) -> {
          System.out.println("Called for " + typeDescription);
          return builder
              .method(ElementMatchers.named("fireEvent"))
              .intercept(MethodDelegation.to(MyGeneralInterceptor.class));
        })
        .installOn(instrumentation);

    Bukkit.getScheduler().runTaskTimer(this, () -> {
      System.out.println("Fired!");
      Bukkit.getPluginManager().callEvent(new RandomEvent());
    }, 1 * 20, 5 * 20);
  }

  @Override
  public void onDisable() {
  }
}
