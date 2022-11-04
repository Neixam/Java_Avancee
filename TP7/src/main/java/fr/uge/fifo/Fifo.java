package fr.uge.fifo;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;

public class Fifo<E> implements Iterable<E>{
  private final E[] internTab;
  private int size;
  private int head;
  private int tail;

  @SuppressWarnings("unchecked")
  public Fifo(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must > 0");
    }
    internTab = (E[]) new Object[capacity];
  }

  public void offer(E value) {
    Objects.requireNonNull(value);
    if (size == internTab.length) {
      throw new IllegalStateException("the queue are full");
    }
    internTab[tail] = value;
    size++;
    tail = (tail + 1) % internTab.length;
  }

  public E poll() {
    if (size == 0) {
      throw new IllegalStateException("the queue are empty");
    }
    var ret = internTab[head];
    internTab[head] = null;
    head = (head + 1) % internTab.length;
    size--;
    return ret;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public String toString() {
    var joiner = new StringJoiner(", ", "[", "]");
    int current = head;
    for (int i = 0; i < size; i++) {
      joiner.add(internTab[current].toString());
      current = (current + 1) % internTab.length;
    }
    return joiner.toString();
  }

  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int current = head;
      private int i;

      @Override
      public boolean hasNext() {
        return i < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no next");
        }
        var ret = internTab[current];
        current = (current + 1) % internTab.length;
        i++;
        return ret;
      }
    };
  }
}
