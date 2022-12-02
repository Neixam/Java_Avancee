package fr.uge.seq;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Seq2<T> implements Iterable<T> {
  private final Object[] elements;
  private final Function<? super Object, ? extends T> mapper;

  private Seq2(List<?> elements, Function<? super Object, ? extends T> mapper) {
    Objects.requireNonNull(elements);
    Objects.requireNonNull(mapper);
    this.elements = elements.toArray();
    this.mapper = mapper;
  }

  @SuppressWarnings("unchecked")
  public static <T> Seq2<T> from(List<? extends T> elements) {
    Objects.requireNonNull(elements);
    return new Seq2<>(List.copyOf(elements), e -> (T) e);
  }

  @SafeVarargs
  public static <T> Seq2<T> of(T ... elements) {
    Objects.requireNonNull(elements);
    return from(List.of(elements));
  }

  public int size() {
    return elements.length;
  }

  public T get(int index) {
    Objects.checkIndex(index, size());
    return mapper.apply(elements[index]);
  }

  public <R> Seq2<R> map(Function<? super T, ? extends R> mapper) {
    Objects.requireNonNull(mapper);
    return new Seq2<>(Arrays.asList(elements), mapper.compose(this.mapper));
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<>() {
      private int index;

      @Override
      public boolean hasNext() {
        return index != size();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no more element in this iterator");
        }
        return get(index++);
      }
    };
  }

  public void forEach(Consumer<? super T> consumer) {
    Objects.requireNonNull(consumer);
    Arrays.stream(elements).map(mapper).forEach(consumer);
  }

  public Optional<T> findFirst() {
    if (elements.length == 0) {
      return Optional.empty();
    }
    return Optional.of(get(0));
  }

  private Spliterator<T> fromArray(int start, int end) {
    return new Spliterator<>() {
      private int i = start;
      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (i >= end) {
          return false;
        }
        action.accept(get(i++));
        return true;
      }

      @Override
      public Spliterator<T> trySplit() {
        var middle = (end + i) >>> 1;
        if (middle == i) {
          return null;
        }
        var split = fromArray(i, middle);
        i = middle;
        return split;
      }

      @Override
      public long estimateSize() {
        return end - i;
      }

      @Override
      public int characteristics() {
        return IMMUTABLE | ORDERED | NONNULL | SIZED | SUBSIZED;
      }
    };
  }

  public Stream<T> stream() {
    return StreamSupport.stream(fromArray(0, size()), false);
  }

  @Override
  public String toString() {
    return Arrays.stream(elements)
            .map(mapper)
            .map(Object::toString)
            .collect(Collectors.joining(", ", "<", ">"));
  }
}
