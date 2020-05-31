package de.ialistannen.eventtracer.interactive.filters.defaults;

import de.ialistannen.eventtracer.interactive.filters.AttributeParserCollection;
import java.util.regex.Pattern;
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
    UnifyingAttributeParser<Pattern, String> parser = new UnifyingAttributeParser<>(
        reader -> Pattern.compile(reader.readRemaining()),
        (pattern, string) -> pattern.matcher(string).find()
    );
    parser.addExtractor(AsyncPlayerChatEvent.class, AsyncPlayerChatEvent::getFormat);
    parser.addExtractor(AsyncPlayerChatEvent.class, AsyncPlayerChatEvent::getMessage);

    collection.addParser("chat-message-content", parser);
    collection.addParser("chat-message-format", parser);
  }

}
