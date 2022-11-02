package fr.uge.set;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DynamicHashSet<T> {
  private record Entry<T>(T value, Entry<T> next) {
  }
  /* On peut faire un cast ici, car l'erasure crée un vide pour les types paramétriques et on sait qu'on ne rangera
  * jamais rien d'autre qu'un Entry<T> dans le tableau. */
  @SuppressWarnings("unchecked")
  private Entry<T>[] entries = (Entry<T>[]) new Entry<?>[2];
  private int size;

  private static int hachage(Object value, int size) {
    return Objects.hashCode(value) & (size - 1);
  }

  @SuppressWarnings("unchecked")
  private void upgrade() {
    var newEntries = (Entry<T>[]) new Entry<?>[entries.length << 1];
    for (Entry<T> tEntry : entries) {
      for (var entry = tEntry; entry != null; entry = entry.next) {
        var hachValue = hachage(entry.value, newEntries.length);
        newEntries[hachValue] = new Entry<>(entry.value, newEntries[hachValue]);
      }
    }
    entries = newEntries;
  }

  public void add(T value) {
    Objects.requireNonNull(value);
    var hachValue = hachage(value, entries.length);
    for (var entry = entries[hachValue]; entry != null; entry = entry.next) {
      if (Objects.equals(entry.value, value)) {
        return;
      }
    }
    size++;
    entries[hachValue] = new Entry<>(value, entries[hachValue]);
    if (entries.length / 2 < size) {
      upgrade();
    }
  }

  public boolean contains(Object value) {
    Objects.requireNonNull(value);
    for (var entry = entries[hachage(value, entries.length)]; entry != null; entry = entry.next) {
      if (Objects.equals(entry.value, value)) {
        return true;
      }
    }
    return false;
  }

  public int size() {
    return size;
  }

  public void forEach(Consumer<? super T> tConsumer) {
    Objects.requireNonNull(tConsumer);
    Arrays.stream(entries)
            .flatMap(tEntries -> Stream.iterate(tEntries, Objects::nonNull, Entry::next).map(Entry::value))
            .forEach(tConsumer);
  }

  public void addAll(Collection<? extends T> values) {
    Objects.requireNonNull(values);
    values.forEach(this::add);
  }
}
