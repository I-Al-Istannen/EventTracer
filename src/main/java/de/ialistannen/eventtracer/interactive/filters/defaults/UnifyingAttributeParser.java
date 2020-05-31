package de.ialistannen.eventtracer.interactive.filters.defaults;

import de.ialistannen.eventtracer.interactive.filters.AttributeParser;
import de.ialistannen.eventtracer.util.parsing.ParseException;
import de.ialistannen.eventtracer.util.parsing.StatelessParser;
import de.ialistannen.eventtracer.util.parsing.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.event.Event;

/**
 * An attribute filter that will use a common parser and predicate, but different extractors from
 * Event to the needed intermediary representation.
 *
 * All matching extractors are tried in insertion order, until one returns true.
 *
 * @param <T> the type of the parsed attribute value
 * @param <I> the type of the intermediary representation
 */
public class UnifyingAttributeParser<T, I> implements AttributeParser<Event> {

  private final StatelessParser<T> parser;
  private final List<Entry<Event, I>> extractors;
  private final BiPredicate<T, I> predicate;

  public UnifyingAttributeParser(StatelessParser<T> parser, BiPredicate<T, I> predicate) {
    this.parser = parser;
    this.extractors = new ArrayList<>();
    this.predicate = predicate;
  }

  /**
   * Adds a new extractor.
   *
   * @param clazz the class the extractor is for
   * @param extractor the extractor
   * @param <E> the type of the class the extractor is for
   */
  public <E extends Event> void addExtractor(Class<E> clazz, Function<E, I> extractor) {
    @SuppressWarnings({"unchecked"})
    Entry<Event, I> entry = (Entry<Event, I>) new Entry<>(clazz, extractor);

    this.extractors.add(entry);
  }

  @Override
  public boolean isApplicable(Class<? extends Event> event) {
    return extractors.stream().anyMatch(it -> it.clazz.isAssignableFrom(event));
  }

  @Override
  public Predicate<Event> parse(StringReader input) throws ParseException {
    T parsedValue = parser.apply(input);

    return event -> {
      for (Entry<Event, I> entry : extractors) {
        if (entry.getClazz().isInstance(event)) {
          I extracted = entry.getExtractor().apply(event);

          if (predicate.test(parsedValue, extracted)) {
            return true;
          }
        }
      }
      return false;
    };
  }

  private static class Entry<E, U> {

    private final Class<E> clazz;
    private final Function<E, U> extractor;

    private Entry(Class<E> clazz, Function<E, U> extractor) {
      this.clazz = clazz;
      this.extractor = extractor;
    }

    private Function<E, U> getExtractor() {
      return extractor;
    }

    private Class<E> getClazz() {
      return clazz;
    }
  }

}
