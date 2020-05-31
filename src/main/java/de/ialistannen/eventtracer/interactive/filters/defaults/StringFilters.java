package de.ialistannen.eventtracer.interactive.filters.defaults;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringFilters {

  /**
   * Turns an extractor and regex string into a predicate.
   *
   * @param extractor the extractor
   * @param regex the regex
   * @param <T> the type of the source
   * @return the predicate
   */
  public static <T> Predicate<T> regexFind(Function<T, String> extractor, String regex) {
    Pattern pattern = Pattern.compile(regex);
    return t -> pattern.matcher(extractor.apply(t)).find();
  }
}
