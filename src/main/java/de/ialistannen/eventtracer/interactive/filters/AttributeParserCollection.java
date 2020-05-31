package de.ialistannen.eventtracer.interactive.filters;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.bukkit.event.Event;

/**
 * A singleton collection for {@link AttributeParser}s.
 */
public class AttributeParserCollection {

  private static final AttributeParserCollection INSTANCE = new AttributeParserCollection();

  private final Map<String, AttributeParser<Event>> knownParsers;

  public AttributeParserCollection() {
    this.knownParsers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  }

  /**
   * Adds a parser, overwriting an existing one for the same key.
   *
   * @param keyword the keyword
   * @param parser the parser
   * @param <T> the type of the parser
   */
  @SuppressWarnings("unchecked")
  public <T extends Event> void addParser(String keyword, AttributeParser<T> parser) {
    knownParsers.put(keyword, (AttributeParser<Event>) parser);
  }

  /**
   * Removes a parser.
   *
   * @param keyword the keyword
   */
  public void removeParser(String keyword) {
    knownParsers.remove(keyword);
  }

  /**
   * Returns the parser for the given attribute.
   *
   * @param keyword the keyword
   * @return the parser, if any
   */
  public Optional<AttributeParser<Event>> getParser(String keyword) {
    return Optional.ofNullable(knownParsers.get(keyword));
  }

  /**
   * @return the filter factory instance.
   */
  public static AttributeParserCollection getInstance() {
    return INSTANCE;
  }
}
