package de.ialistannen.eventtracer.interactive.filters;

import de.ialistannen.eventtracer.util.parsing.ParseException;
import de.ialistannen.eventtracer.util.parsing.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.event.Event;

/**
 * A parser for an {@link ParsedFilter}.
 */
public class EventFilterParser {

  private final StringReader stringReader;
  private Class<? extends Event> eventClass;

  /**
   * Creates a new parser for the given string.
   *
   * @param underlying the string to parse
   */
  public EventFilterParser(String underlying) {
    this.stringReader = new StringReader(underlying);
  }

  public Class<? extends Event> getEventClass() {
    return eventClass;
  }

  /**
   * Parses a filter specification to an event predicate.
   *
   * @return the predicate
   * @throws ParseException if a parse error occurs
   */
  public Predicate<Event> parse() throws ParseException {
    eventClass = parseEvent();

    stringReader.readWhile(Character::isWhitespace);

    if (!stringReader.canRead()) {
      System.out.println("Returning instance of " + eventClass);
      return event -> eventClass.isAssignableFrom(event.getClass());
    }

    stringReader.assertRead("{");
    List<FilterAttribute> attributes = parseFilterAttributes();
    stringReader.assertRead("}");

    List<Predicate<Event>> predicates = new ArrayList<>();
    predicates.add(it -> eventClass.isAssignableFrom(it.getClass()));

    for (FilterAttribute attribute : attributes) {
      Predicate<Event> filter = attribute.getParser().parse(new StringReader(attribute.getValue()));
      predicates.add(filter);
    }

    return event -> predicates.stream().allMatch(p -> p.test(event));
  }

  private List<FilterAttribute> parseFilterAttributes() throws ParseException {
    List<FilterAttribute> attributes = new ArrayList<>();

    while (stringReader.canRead() && stringReader.peek() != '}') {
      attributes.add(parseFilterAttribute());
      stringReader.readWhile(Character::isWhitespace);
    }

    return attributes;
  }

  private Class<? extends Event> parseEvent() throws ParseException {
    String eventClassName = stringReader.readUntil("{");

    try {
      @SuppressWarnings("unchecked")
      Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(eventClassName);

      if (!Event.class.isAssignableFrom(eventClass)) {
        throw new ParseException(stringReader, "Class is no event :/");
      }

      return eventClass;
    } catch (ClassNotFoundException e) {
      throw new ParseException(stringReader, "Class not found :/");
    }
  }

  private FilterAttribute parseFilterAttribute() throws ParseException {
    stringReader.readWhile(Character::isWhitespace);

    AttributeParser<Event> parser = parseAttributeKeyword();
    stringReader.assertRead("=");

    String value = stringReader.readPhrase();

    return new FilterAttribute(parser, value);
  }

  private AttributeParser<Event> parseAttributeKeyword() throws ParseException {
    String name = stringReader.readUntil("=");

    Optional<AttributeParser<Event>> parser = AttributeParserCollection.getInstance()
        .getParser(name);

    if (!parser.isPresent()) {
      throw new ParseException(stringReader, "Unknown attribute '" + name + "'");
    }

    if (!parser.get().isApplicable(eventClass)) {
      throw new ParseException(stringReader, "The filter can not be applied to this event!");
    }

    return parser.get();
  }

  /**
   * A raw unverified parsed filter.
   */
  private static class ParsedFilter {

    private final Class<? extends Event> eventClass;
    private final List<FilterAttribute> filterAttributes;

    ParsedFilter(Class<? extends Event> eventClass, List<FilterAttribute> filterAttributes) {
      this.eventClass = eventClass;
      this.filterAttributes = new ArrayList<>(filterAttributes);
    }

    Class<? extends Event> getEventClass() {
      return eventClass;
    }

    List<FilterAttribute> getFilterAttributes() {
      return Collections.unmodifiableList(filterAttributes);
    }
  }

  /**
   * An attribute of a filter.
   */
  private static class FilterAttribute {

    private final AttributeParser<Event> parser;
    private final String value;

    FilterAttribute(AttributeParser<Event> parser, String value) {
      this.parser = parser;
      this.value = value;
    }

    AttributeParser<Event> getParser() {
      return parser;
    }

    String getValue() {
      return value;
    }
  }
}
