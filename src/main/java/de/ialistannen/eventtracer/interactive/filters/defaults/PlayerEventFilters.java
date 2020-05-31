package de.ialistannen.eventtracer.interactive.filters.defaults;

import de.ialistannen.eventtracer.interactive.filters.AttributeParser;
import de.ialistannen.eventtracer.interactive.filters.AttributeParserCollection;
import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.server.TabCompleteEvent;

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
    UnifyingAttributeParser<Pattern, CommandSender> parser = new UnifyingAttributeParser<>(
        reader -> Pattern.compile(reader.readRemaining()),
        (pattern, sender) -> pattern.matcher(sender.getName()).find()
    );

    parser.addExtractor(PlayerEvent.class, PlayerEvent::getPlayer);
    parser.addExtractor(EntityDamageByEntityEvent.class, EntityDamageByEntityEvent::getDamager);
    parser.addExtractor(InventoryEvent.class, event -> event.getView().getPlayer());
    parser.addExtractor(PlayerLeashEntityEvent.class, PlayerLeashEntityEvent::getPlayer);
    parser.addExtractor(PlayerLeashEntityEvent.class, PlayerLeashEntityEvent::getEntity);
    parser.addExtractor(TabCompleteEvent.class, TabCompleteEvent::getSender);
    parser.addExtractor(EntityEvent.class, EntityEvent::getEntity);

    return parser;
  }
}
