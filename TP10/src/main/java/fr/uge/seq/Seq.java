package fr.uge.seq;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Seq<T> implements Iterable<T> {
  private final List<?> elements;
  private final Function<? super Object, ? extends T> mapper;

  private Seq(List<?> elements, Function<? super Object, ? extends T> mapper) {
    Objects.requireNonNull(elements);
    Objects.requireNonNull(mapper);
    this.elements = elements;
    this.mapper = mapper;
  }
  @SuppressWarnings("unchecked")
  public static <T> Seq<T> from(List<? extends T> elements) {
    Objects.requireNonNull(elements);
    return new Seq<>(List.copyOf(elements), e -> (T) e);
  }

  @SafeVarargs
  public static <T> Seq<T> of(T ... elements) {
    Objects.requireNonNull(elements);
    return from(List.of(elements));
  }

  public int size() {
    return elements.size();
  }

  public T get(int index) {
    Objects.checkIndex(index, size());
    return mapper.apply(elements.get(index));
  }

  public <R> Seq<R> map(Function<? super T, ? extends R> mapper) {
    Objects.requireNonNull(mapper);
    return new Seq<>(elements, mapper.compose(this.mapper));
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
    elements.stream().map(mapper).forEach(consumer);
  }

  public Optional<T> findFirst() {
    return elements.stream().<T>map(mapper).findFirst();
  }

  private Spliterator<T> fromSplit(Spliterator<?> spliterator) {
    return new Spliterator<>() {
      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        return spliterator.tryAdvance(e -> action.accept(mapper.apply(e)));
      }

      @Override
      public Spliterator<T> trySplit() {
        var split = spliterator.trySplit();
        if (split == null) {
          return null;
        }
        return fromSplit(split);
      }

      @Override
      public long estimateSize() {
        return spliterator.estimateSize();
      }

      @Override
      public int characteristics() {
        return spliterator().characteristics() | IMMUTABLE | ORDERED | NONNULL | SIZED | SUBSIZED;
      }
    };
  }

  public Stream<T> stream() {
    var spliterator = elements.spliterator();
    return StreamSupport.stream(fromSplit(spliterator), false);
  }

  @Override
  public String toString() {
    return elements.stream()
            .map(mapper)
            .map(Object::toString)
            .collect(Collectors.joining(", ", "<", ">"));
  }
}
