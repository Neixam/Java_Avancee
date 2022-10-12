package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;

public interface Slice4<U> {
  int size();
  U get(int index);
  Slice4<U> subSlice(int from, int to);

  static <T> Slice4<T> array(T[] array) {
    Objects.requireNonNull(array);
    return array(array, 0, array.length);
  }
  static <T> Slice4<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);
    return new Slice4<>() {
      @Override
      public int size() {
        return to - from;
      }

      @Override
      public T get(int index) {
        Objects.checkIndex(index, size());
        return array[index + from];
      }

      @Override
      public Slice4<T> subSlice(int from2, int to) {
        Objects.checkFromToIndex(from2, to, size());
        return array(array, from2 + from, to + from);
      }

      @Override
      public String toString() {
        return Arrays.toString(Arrays.stream(array, from, to).toArray());
      }
    };
  }
}
