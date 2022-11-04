package fr.uge.fifo;

import java.util.*;

public class ResizeableFifo<E> extends AbstractQueue<E> {
  private E[] internTab;
  private int size;
  private int head;
  private int tail;

  @SuppressWarnings("unchecked")
  public ResizeableFifo(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must > 0");
    }
    internTab = (E[]) new Object[capacity];
  }

  @Override
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

  @Override
  public int size() {
    return size;
  }

  private void upgrade() {
    @SuppressWarnings("unchecked")
    var newInternTab = (E[]) new Object[internTab.length << 1];
    if (head >= tail) {
      System.arraycopy(internTab, head, newInternTab, 0, internTab.length - head);
      System.arraycopy(internTab, 0, newInternTab, internTab.length - head, size - (internTab.length - head));
    }
    else {
      System.arraycopy(internTab, head, newInternTab, 0, size);
    }
    internTab = newInternTab;
    head = 0;
    tail = size;
  }

  @Override
  public boolean offer(E e) {
    Objects.requireNonNull(e);
    if (size == internTab.length) {
      upgrade();
    }
    internTab[tail] = e;
    size++;
    tail = (tail + 1) % internTab.length;
    return true;
  }

  @Override
  public E poll() {
    if (size == 0) {
      return null;
    }
    var ret = internTab[head];
    internTab[head] = null;
    head = (head + 1) % internTab.length;
    size--;
    return ret;
  }

  @Override
  public E peek() {
    return internTab[head];
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
}
