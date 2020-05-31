package de.ialistannen.eventtracer.interactive.filters.defaults;

import de.ialistannen.eventtracer.interactive.filters.AttributeParserCollection;
import de.ialistannen.eventtracer.util.parsing.ParseException;
import org.bukkit.Material;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.util.Vector;

public class BlockEventFilters {

  /**
   * Registers the default player event filters
   *
   * @param collection the collection to register them to
   */
  public static void registerDefaults(AttributeParserCollection collection) {
    registerType(collection);
    registerBlockLocation(collection);
  }

  private static void registerBlockLocation(AttributeParserCollection collection) {
    UnifyingAttributeParser<Vector, Vector> parser = new UnifyingAttributeParser<>(
        reader -> {
          int x = reader.readInteger();
          reader.assertRead(",");
          reader.readWhile(Character::isWhitespace);

          int y = reader.readInteger();
          reader.assertRead(",");
          reader.readWhile(Character::isWhitespace);

          int z = reader.readInteger();

          return new Vector(x, y, z);
        },
        (target, current) -> target.toBlockVector().equals(current.toBlockVector())
    );

    parser.addExtractor(
        BlockEvent.class,
        blockEvent -> blockEvent.getBlock().getLocation().toVector()
    );

    collection.addParser("at-block-location", parser);
  }

  private static void registerType(AttributeParserCollection collection) {
    UnifyingAttributeParser<Material, Material> parser = new UnifyingAttributeParser<>(
        reader -> {
          Material material = Material.matchMaterial(reader.readRemaining());
          if (material == null) {
            throw new ParseException(reader, "Unknown material");
          }
          return material;
        },
        (target, current) -> target == current
    );

    parser.addExtractor(BlockEvent.class, blockEvent -> blockEvent.getBlock().getType());

    collection.addParser("of-material", parser);
  }
}
