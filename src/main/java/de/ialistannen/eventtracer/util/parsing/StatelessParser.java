package de.ialistannen.eventtracer.util.parsing;

/**
 * A simple parser functional interface.
 *
 * @param <T> the type of the parser
 */
public interface StatelessParser<T> {

  T apply(StringReader reader) throws ParseException;
}
