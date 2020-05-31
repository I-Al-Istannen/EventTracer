package de.ialistannen.eventtracer.interactive.filters.defaults;

import static de.ialistannen.eventtracer.interactive.filters.defaults.StringFilters.regexFind;

import de.ialistannen.eventtracer.interactive.filters.AttributeParserCollection;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;

/**
 * Default {@link PlayerEvent} filters.
 */
public class ChatEventFilters {

  /**
   * Registers the default player event filters
   *
   * @param collection the collection to register them to
   */
  public static void registerDefaults(AttributeParserCollection collection) {
    collection.addParser("chat-message-content", messageContent());
    collection.addParser("chat-message-format", messageFormat());
  }

  private static SubclassFilter<AsyncPlayerChatEvent> messageContent() {
    return new SubclassFilter<>(
        AsyncPlayerChatEvent.class,
        stringReader -> regexFind(AsyncPlayerChatEvent::getMessage, stringReader.readRemaining())
    );
  }

  private static SubclassFilter<AsyncPlayerChatEvent> messageFormat() {
    return new SubclassFilter<>(
        AsyncPlayerChatEvent.class,
        stringReader -> regexFind(AsyncPlayerChatEvent::getFormat, stringReader.readRemaining())
    );
  }
}
