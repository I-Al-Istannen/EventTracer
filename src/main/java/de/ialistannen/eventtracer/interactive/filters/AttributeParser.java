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
}
