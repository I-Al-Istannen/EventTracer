package de.ialistannen.eventtracer.util.parsing;

import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

/**
 * An exception that occurred during parsing.
 */
public class ParseException extends Exception {

  private static final int CONTEXT_LENGTH = 10;

  /**
   * Creates a new parse exception.
   *
   * <p>The message is set to a small context string (the last X characters of the input), a marker
   * to indicate the position and the detail message.</p>
   *
   * @param reader the string reader
   * @param detail the detail error message
   */
  public ParseException(StringReader reader, String detail) {
    super(getContext(reader, detail));
  }

  private static String getContext(StringReader input, String detail) {
    int start = input.getPosition();
    start = Math.max(start - CONTEXT_LENGTH, 0);
    String contextString = input.getUnderlying().substring(start, input.getPosition())
        .replaceAll("\\n", "⏎");

    int end = Math.min(input.getPosition() + CONTEXT_LENGTH, input.getUnderlying().length());
    String contextAfterString = input.getUnderlying().substring(input.getPosition(), end)
        .replaceAll("\\n", "⏎");

    String lineString = " (line " + input.getLineNumber() + ")";

    if (!detail.isEmpty()) {
      return GREEN + detail
          + RED + " at "
          + BLUE + contextString
          + RED + "<-[HERE]"
          + BLUE + contextAfterString
          + RED + lineString;
    }

    return BLUE + contextString
        + RED + "<-[HERE]"
        + BLUE + contextAfterString
        + RED + lineString;
  }
}
