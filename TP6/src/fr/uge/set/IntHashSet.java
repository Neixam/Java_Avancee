package fr.uge.set;

import java.util.Objects;
import java.util.function.IntConsumer;

public class IntHashSet {
  private static final int SIZE = 8;
  private record Entry(int value, Entry next) {}
  private final Entry[] entries = new Entry[SIZE];
  private int size;

  private static int hachage(int i) {
    return i & (SIZE - 1);
  }

  public void add(int value) {
    var entry1 = entries[hachage(value)];
    for (var entry = entry1; entry != null; entry = entry.next) {
      if (entry.value == value) {
        return;
      }
    }
    size++;
    entries[hachage(value)] = new Entry(value, entry1);
  }

  public int size() {
    return size;
  }

  public void forEach(IntConsumer intConsumer) {
    Objects.requireNonNull(intConsumer);
    for (var i = 0; i < SIZE; i++) {
      for (var entry = entries[i]; entry != null; entry = entry.next) {
        intConsumer.accept(entry.value);
      }
    }
  }

  public boolean contains(int value) {
    for (var entry = entries[hachage(value)]; entry != null; entry = entry.next) {
      if (entry.value == value) {
        return true;
      }
    }
    return false;
  }
}
