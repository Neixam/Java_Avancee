package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public interface Slice3<U> {
  int size();
  U get(int index);
  default Slice3<U> subSlice(int from, int to) {
    Objects.checkFromToIndex(from, to, size());
    return new Slice3<>() {
      @Override
      public int size() {
        return to - from;
      }
      @Override
      public U get(int index) {
        Objects.checkIndex(index, size());
        return Slice3.this.get(index + from);
      }

      @Override
      public String toString() {
        return Arrays.toString(IntStream.range(0, size())
                .mapToObj(this::get).toArray());
      }
    };
  }
  static <T> Slice3<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new Slice3<>() {
      @Override
      public int size() {
        return array.length;
      }

      @Override
      public T get(int index) {
        Objects.checkIndex(index, array.length);
        return array[index];
      }

      @Override
      public String toString() {
        return Arrays.toString(array);
      }
    };
  }
  static <T> Slice3<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);
    return array(array).subSlice(from, to);
  }
}
