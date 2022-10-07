package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;

public sealed interface Slice2<U> permits Slice2.ArraySlice, Slice2.ArraySlice.SubArraySlice {
  int size();
  U get(int index);
  Slice2<U> subSlice(int from, int to);
  final class ArraySlice<V> implements Slice2<V> {
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
    public Slice2<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, internArray.length);
      return this.new SubArraySlice(from, to);
    }

    @Override
    public String toString() {
      return Arrays.toString(internArray);
    }
    final class SubArraySlice implements Slice2<V> {
      private final int from;
      private final int to;

     private SubArraySlice(int from, int to) {
       Objects.checkFromToIndex(from, to, internArray.length);
       this.from = from;
       this.to = to;
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
      public Slice2<V> subSlice(int from, int to) {
        Objects.checkFromToIndex(from, to, this.to - this.from);
        return ArraySlice.this.new SubArraySlice(this.from + from, this.from + to);
      }

      @Override
      public String toString() {
        return Arrays.toString(Arrays.stream(internArray, from, to).toArray());
      }
    }

 }
  static <T> Slice2<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }

  static <T> Slice2<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);
    return new ArraySlice<>(array).new SubArraySlice(from, to);
  }
}
