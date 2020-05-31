package de.ialistannen.eventtracer.util.parsing;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * A simple ring buffer.
 *
 * @param <E> the type of elements in the buffer
 */
public class RingBuffer<E> extends AbstractQueue<E> {

  private final int capacity;
  private final ArrayDeque<E> underlying;

  /**
   * Creates a new ring buffer with the specified capacity.
   *
   * @param capacity the capacity
   */
  public RingBuffer(int capacity) {
    this.capacity = capacity;
    this.underlying = new ArrayDeque<>(capacity);
  }

  @Override
  public Iterator<E> iterator() {
    return underlying.iterator();
  }

  @Override
  public int size() {
    return underlying.size();
  }

  @Override
  public boolean offer(E e) {
    if (size() >= capacity) {
      underlying.removeFirst();
    }
    underlying.addLast(e);
    return true;
  }

  @Override
  public E poll() {
    return underlying.removeLast();
  }

  @Override
  public E peek() {
    return underlying.peekLast();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RingBuffer<?> that = (RingBuffer<?>) o;
    return capacity == that.capacity && Arrays.equals(underlying.toArray(), that.toArray());
  }

  @Override
  public int hashCode() {
    return Objects.hash(capacity, underlying);
  }

  /**
   * Creates a buffer wrapping the given char array.
   *
   * @param chars the char array
   * @return the resulting buffer
   */
  public static RingBuffer<Character> forCharArray(char... chars) {
    RingBuffer<Character> buffer = new RingBuffer<>(chars.length);
    for (char c : chars) {
      buffer.add(c);
    }
    return buffer;
  }
}
