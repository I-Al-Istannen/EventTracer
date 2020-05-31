package de.ialistannen.eventtracer.interactive.filters.defaults;

import de.ialistannen.eventtracer.interactive.filters.AttributeParser;
import de.ialistannen.eventtracer.util.parsing.AtomicParser;
import de.ialistannen.eventtracer.util.parsing.ParseException;
import de.ialistannen.eventtracer.util.parsing.StringReader;
import java.util.function.Predicate;
import org.bukkit.event.Event;

/**
 * An {@link AttributeParser} that only accepts subclasses of a given class and provides specialized
 * functions.
 *
 * @param <T> the type
 */
public class SubclassFilter<T extends Event> implements AttributeParser<Event> {

  private final Class<T> targetClass;
  private final AtomicParser<Predicate<T>> parser;

  public SubclassFilter(Class<T> targetClass, AtomicParser<Predicate<T>> parser) {
    this.targetClass = targetClass;
    this.parser = parser;
  }

  @Override
  public boolean isApplicable(Class<? extends Event> event) {
    return targetClass.isAssignableFrom(event);
  }

  @Override
  public Predicate<Event> parse(StringReader input) throws ParseException {
    @SuppressWarnings("unchecked")
    Predicate<Event> predicate = (Predicate<Event>) parser.apply(input);
    return predicate;
  }
}
