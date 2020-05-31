package de.ialistannen.eventtracer.util.parsing;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility reader for a string.
 */
public class StringReader {

  private static Set<Character> QUOTE_CHARS = new HashSet<>(Arrays.asList('"', '\''));

  private final String underlying;
  private int position;

  /**
   * Creates a new string reader.
   *
   * @param underlying the underlying string
   */
  public StringReader(String underlying) {
    this(underlying, 0);
  }

  /**
   * Creates a new string reader.
   *
   * @param underlying the underlying string
   * @param position the initial position
   */
  public StringReader(String underlying, int position) {
    this.underlying = underlying;
    this.position = position;
  }

  /**
   * @return a copy of this string reader
   */
  public StringReader copy() {
    return new StringReader(getUnderlying(), getPosition());
  }

  /**
   * Returns true if there is more to read.
   *
   * @return true if there is more to read
   */
  public boolean canRead() {
    return position < underlying.length();
  }

  /**
   * Returns true if there is enough input to read {@code amount} chars.
   *
   * @param amount the amount of chars to read
   * @return true if there is more to read
   */
  public boolean canRead(int amount) {
    return position + amount <= underlying.length();
  }

  /**
   * Peeks at a single char.
   *
   * @return the char
   */
  public char peek() {
    return underlying.charAt(position);
  }

  /**
   * Returns the next {@code amount} chars or less, if the input ends before it.
   *
   * @param amount the amount of chars to peek at
   * @return the read text
   */
  public String peek(int amount) {
    return underlying.substring(position, Math.min(underlying.length(), position + amount));
  }

  /**
   * Returns the next chars that match the predicate.
   *
   * @param predicate the predicate
   * @return the read text
   */
  public String peekWhile(Predicate<Character> predicate) {
    int start = position;
    String text = readWhile(predicate);

    position = start;

    return text;
  }

  /**
   * Reads a single char.
   *
   * @return the read char
   */
  public char readChar() {
    return underlying.charAt(position++);
  }

  /**
   * Reads the given amount of characters.
   *
   * @param count the amount of characters to read
   * @return the read string
   */
  public String readChars(int count) {
    int oldPos = this.position;
    position = this.position + count;

    return underlying.substring(oldPos, position);
  }

  /**
   * Reads for as long as {@link #canRead()} is true and the predicate matches.
   *
   * Will place the cursor at the first char that did not match.
   *
   * @param predicate the predicate
   * @return the read string
   */
  public String readWhile(Predicate<Character> predicate) {
    int start = position;
    while (canRead() && predicate.test(peek())) {
      readChar();
    }

    return underlying.substring(start, position);
  }

  /**
   * Reads for as long as {@link #canRead()} is true and the given sequence was not found.
   *
   * Will place the cursor at the first char of the marker sequence.
   *
   * @param sequence the end sequence
   * @return the read string
   */
  public String readUntil(String sequence) {
    int start = position;
    RingBuffer<Character> target = new RingBuffer<>(sequence.length());
    sequence.chars().forEach(it -> target.add((char) it));

    RingBuffer<Character> current = new RingBuffer<>(sequence.length());

    while (canRead()) {
      char next = readChar();
      current.add(next);

      if (current.equals(target)) {
        position -= sequence.length();
        break;
      }
    }

    return underlying.substring(start, position);
  }

  /**
   * Reads the whole string matching the regex.
   *
   * @param pattern the pattern to use
   * @return the read string or an empty String, if the regex didn't match
   */
  public String readRegex(Pattern pattern) {
    Matcher matcher = pattern.matcher(getUnderlying());
    boolean resultFound = matcher.find(position);

    if (!resultFound) {
      return "";
    }

    int start = position;
    position = matcher.end();

    return underlying.substring(start, position);
  }

  /**
   * Reads an integer.
   *
   * @return the read integer
   * @throws ParseException if no integer can be read
   */
  public int readInteger() throws ParseException {
    String read = readRegex(Pattern.compile("[+-]?\\d+"));

    if (read.isEmpty()) {
      throw new ParseException(this, "Expected an integer");
    }

    try {
      return Integer.parseInt(read);
    } catch (NumberFormatException e) {
      throw new ParseException(this, "Expected an integer");
    }
  }

  /**
   * Reads a single line.
   *
   * @return the read line
   */
  public String readLine() {
    String read = readLineIncludingNewline();
    if (read.endsWith(System.lineSeparator())) {
      return read.substring(0, read.length() - System.lineSeparator().length());
    }
    return read;
  }

  /**
   * Reads a single line.
   *
   * @return the read line
   */
  public String readLineIncludingNewline() {
    return readRegex(Pattern.compile(".*?(\\n|$)"));
  }

  /**
   * A parser that reads a single word or a quoted phrase.
   *
   * @return a parser that reads a single word or a quoted phrase
   */
  public String readPhrase() {
    if (!QUOTE_CHARS.contains(peek())) {
      return readWhile(it -> !Character.isWhitespace(it));
    }

    char quoteChar = readChar();

    StringBuilder readString = new StringBuilder();

    boolean escaped = false;
    while (canRead()) {
      char read = readChar();

      if (escaped) {
        escaped = false;
        readString.append(read);
        continue;
      }

      if (read == '\\') {
        escaped = true;
      } else if (read == quoteChar) {
        break;
      } else {
        readString.append(read);
      }
    }
    return readString.toString();
  }

  /**
   * Reads a string that is enclosed by parenthesis i.e. '(' and ')'.
   *
   * @return the enclosed text
   * @throws ParseException if there is a syntax error
   */
  public String readEnclosedByParentheses() throws ParseException {
    assertRead("(");
    StringBuilder readString = new StringBuilder();

    int depth = 1;
    while (canRead()) {
      char read = readChar();

      if (read == '(') {
        depth++;
      } else if (read == ')') {
        depth--;
        if (depth == 0) {
          return readString.toString();
        }
      }
      readString.append(read);
    }
    throw new ParseException(this, "Did not find a closing parenthesis");
  }

  /**
   * Reads the next characters and asserts that they equal the passed string.
   *
   * <p>Will not advance the cursor if it could not read the whole input.</p>
   *
   * @param input the input string
   * @throws ParseException if they don't match
   */
  public void assertRead(String input) throws ParseException {
    if (!canRead(input.length())) {
      String remaining = readRemaining();
      String rest = input.replaceFirst(Pattern.quote(remaining), "");
      throw new ParseException(this, "Expected '" + rest + "'");
    }
    int startPos = position;
    if (!readChars(input.length()).equals(input)) {
      // Show the discrepancy beginning at the start
      reset(startPos);
      throw new ParseException(this, "Expected '" + input + "'");
    }
  }

  /**
   * Reads the remaining string.
   *
   * @return the remaining string
   */
  public String readRemaining() {
    return readWhile(it -> true);
  }

  /**
   * Peeks the remaining string.
   *
   * @return the remaining string
   */
  public String peekRemaining() {
    int start = position;
    String read = readWhile(it -> true);
    reset(start);
    return read;
  }

  /**
   * Returns the underlying string.
   *
   * @return the underlying string
   */
  public String getUnderlying() {
    return underlying;
  }

  /**
   * Returns the current position of this reader.
   *
   * @return the current position of this reader
   */
  public int getPosition() {
    return position;
  }

  /**
   * Sets the position of the reader.
   *
   * @param position the new position
   */
  public void reset(int position) {
    this.position = position;
  }

  /**
   * Returns the current line number, starting with 1.
   *
   * @return the line number
   */
  public int getLineNumber() {
    return countOccurrences(System.lineSeparator()) + 1;
  }

  /**
   * Returns the amount of times a sequence occurred in the read string.
   *
   * @return the amount of occurrences
   */
  public int countOccurrences(String sequence) {
    String substring = underlying.substring(0, position);
    if (sequence.length() == 1) {
      return (int) substring.chars()
          .filter(it -> it == sequence.charAt(0))
          .count();
    }

    int count = 0;
    RingBuffer<Character> expected = RingBuffer.forCharArray(sequence.toCharArray());

    RingBuffer<Character> rollingLast = new RingBuffer<>(sequence.length());
    for (char c : underlying.toCharArray()) {
      rollingLast.add(c);
      if (expected.equals(rollingLast)) {
        count++;
      }
    }

    return count;
  }
}
