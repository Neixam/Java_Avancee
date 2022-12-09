package fr.uge.numeric;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NumericVec<E> extends AbstractCollection<E> implements Iterable<E> {
  private long[] internTab;
  private int size;
  private final ToLongFunction<E> into;
  private final LongFunction<E> from;

  private NumericVec(long[] args, ToLongFunction<E> into, LongFunction<E> from) {
    internTab = args;
    size = args.length;
    this.into = into;
    this.from = from;
  }
  public static NumericVec<Long> longs(long ... args) {
    return new NumericVec<>(Arrays.copyOf(args, args.length), e -> e, e -> e);
  }

  public static NumericVec<Integer> ints(int ... args) {
    return new NumericVec<>(Arrays.stream(args)
            .mapToLong(Long::valueOf).toArray(), e -> (long) e, e -> (int) e);
  }

  public static NumericVec<Double> doubles(double ... args) {
    return new NumericVec<>(Arrays.stream(args)
            .mapToLong(Double::doubleToRawLongBits)
            .toArray(), Double::doubleToRawLongBits, Double::longBitsToDouble);
  }

  public E get(int index) {
    Objects.checkIndex(index, size);
    return from.apply(internTab[index]);
  }

  private void upgrade() {
    internTab = Arrays.copyOf(internTab, Math.max(size << 1, 16));
  }

  @Override
  public boolean add(E elem) {
    Objects.requireNonNull(elem);
    if (size == internTab.length) {
      upgrade();
    }
    internTab[size++] = into.applyAsLong(elem);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    Objects.requireNonNull(c);
    if (c instanceof NumericVec<?> vec) {
      if (internTab.length < size + vec.size) {
        internTab = Arrays.copyOf(internTab, size + vec.size);
      }
      System.arraycopy(vec.internTab, 0, internTab, size, vec.size);
      size += vec.size;
    } else {
      c.forEach(this::add);
    }
    return true;
  }

  public <T> NumericVec<T> map(Function<? super E, T> function, Supplier<NumericVec<T>> factory) {
    var ret = factory.get();
    ret.addAll(Arrays.stream(internTab, 0, size).mapToObj(from).map(function).toList());
    return ret;
  }

  public int size() {
    return size;
  }

  @Override
  public String toString() {
    return Arrays.stream(internTab, 0, size)
            .mapToObj(from)
            .map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
  }

  public static <T> Collector<T, ?, NumericVec<T>> toNumericVec(Supplier<NumericVec<T>> factory) {
    return Collector.of(factory,
            NumericVec::add,
            (n1, n2) -> { n1.addAll(n2); return n1; });
  }

  private Spliterator<E> fromNumeric(int start, int end) {
    return new Spliterator<>() {
      int index = start;
      @Override
      public boolean tryAdvance(Consumer<? super E> action) {
        if (index == end) {
          return false;
        }
        action.accept(get(index++));
        return true;
      }

      @Override
      public Spliterator<E> trySplit() {
        var middle = (end + index) >>> 1;
        if (middle == index || end - start < 1024) {
          return null;
        }
        var split = fromNumeric(index, middle);
        index = middle;
        return split;
      }

      @Override
      public long estimateSize() {
        return end - start;
      }

      @Override
      public int characteristics() {
        return SIZED | SUBSIZED | NONNULL;
      }
    };
  }

  @Override
  public Stream<E> stream() {
    return StreamSupport.stream(fromNumeric(0, size), size >= 1024);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int index;
      private final int actualSize = size;
      @Override
      public boolean hasNext() {
        return index != actualSize;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        } else if (actualSize != size) {
          throw new ConcurrentModificationException();
        }
        return get(index++);
      }
    };
  }
}
