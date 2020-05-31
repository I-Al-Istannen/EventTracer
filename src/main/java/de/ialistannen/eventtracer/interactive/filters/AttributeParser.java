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
   * @return the class the filter can be applied to
   */
  Class<T> applicableClass();

  /**
   * @param input the input string reader to parse from. You must consume it fully, everything
   *     else is a parse error.
   * @return the parsed predicate
   * @throws ParseException if the attribute could not be parsed
   */
  Predicate<T> parse(StringReader input) throws ParseException;
}
