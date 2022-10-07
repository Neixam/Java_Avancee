package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;

public sealed interface Slice<U> permits Slice.ArraySlice, Slice.SubArraySlice {
  int size();
  U get(int index);
  Slice<U> subSlice(int from, int to);
  final class ArraySlice<V> implements Slice<V> {
    private final V[] internArray;

    private ArraySlice(V[] array) {
      Objects.requireNonNull(array);
      internArray = array;
    }

    @Override
    public int size() {
      return internArray.length;
    }

    @Override
    public V get(int index) {
      Objects.checkIndex(index, internArray.length);
      return internArray[index];
    }

    @Override
    public Slice<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, internArray.length);
      return new SubArraySlice<>(internArray, from, to);
    }

    @Override
    public String toString() {
      return Arrays.toString(internArray);
    }
  }
  static <T> Slice<T> array(T[] array) {
    return new ArraySlice<>(array);
  }

  final class SubArraySlice<V> implements Slice<V> {
    private final V[] internArray;
    private final int from;
    private final int to;

    private SubArraySlice(V[] array, int from, int to) {
      Objects.requireNonNull(array);
      Objects.checkFromToIndex(from, to, array.length);
      this.from = from;
      this.to = to;
      internArray = array;
    }
    @Override
    public int size() {
      return to - from;
    }

    @Override
    public V get(int index) {
      Objects.checkIndex(index, size());
      return internArray[index + from];
    }

    @Override
    public Slice<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, this.to - this.from);
      return new SubArraySlice<>(internArray, this.from + from, this.from + to);
    }

    @Override
    public String toString() {
      return Arrays.toString(Arrays.stream(internArray, from, to).toArray());
    }
  }

  static <T> Slice<T> array(T[] array, int from, int to) {
    return new SubArraySlice<>(array, from, to);
  }
}
