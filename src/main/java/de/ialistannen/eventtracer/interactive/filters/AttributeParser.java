package de.ialistannen.eventtracer.interactive.filters;

import de.ialistannen.eventtracer.util.parsing.ParseException;
import de.ialistannen.eventtracer.util.parsing.StringReader;
import java.util.function.Predicate;
import org.bukkit.event.Event;

/**
 * A parser for an attribute applied to a class.
 */
public interface AttributeParser<T extends Event> {

  /**
   * @param event the event to check
   * @return true if the filter can be applied.
   */
  boolean isApplicable(Class<? extends Event> event);

  /**
   * @param input the input string reader to parse from. You must consume it fully, everything
   *     else is a parse error.
   * @return the parsed predicate
   * @throws ParseException if the attribute could not be parsed
   */
  Predicate<T> parse(StringReader input) throws ParseException;

  /**
   * @param other the other parser
   * @return a parser that applies this parser and, if it fails, the other one.
   */
  default AttributeParser<T> or(AttributeParser<T> other) {
    return new AttributeParser<T>() {
      @Override
      public boolean isApplicable(Class<? extends Event> event) {
        return AttributeParser.this.isApplicable(event) || other.isApplicable(event);
      }

      @Override
      public Predicate<T> parse(StringReader input) throws ParseException {
        StringReader copiedInput = input.copy();
        try {
          return AttributeParser.this.parse(copiedInput);
        } catch (ParseException e) {
          return other.parse(input);
        }
      }
    };
  }
}
