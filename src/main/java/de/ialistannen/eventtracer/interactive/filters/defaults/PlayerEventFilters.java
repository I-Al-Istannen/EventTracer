package de.ialistannen.eventtracer.interactive.filters.defaults;

import static de.ialistannen.eventtracer.interactive.filters.defaults.StringFilters.regexFind;

import de.ialistannen.eventtracer.interactive.filters.AttributeParser;
import de.ialistannen.eventtracer.interactive.filters.AttributeParserCollection;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

/**
 * Default {@link PlayerEvent} filters.
 */
public class PlayerEventFilters {

  /**
   * Registers the default player event filters
   *
   * @param collection the collection to register them to
   */
  public static void registerDefaults(AttributeParserCollection collection) {
    collection.addParser("player-name", playerNameParser());
  }

  private static AttributeParser<Event> playerNameParser() {
    return new SubclassFilter<>(
        PlayerEvent.class,
        reader -> regexFind(e -> e.getPlayer().getName(), reader.readRemaining())
    );
  }
}
