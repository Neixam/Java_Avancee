package fr.uge.series;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimeSeries<T> {
  public record Data<V>(long timestamp, V element) {
    public Data {
      Objects.requireNonNull(element);
    }

    @Override
    public String toString() {
      return timestamp + " | " + element;
    }
  }

  public class Index implements Iterable<Data<T>> {
    private final int[] indexArray;

    private Index(int[] indexArray) {
      Objects.requireNonNull(indexArray);
      this.indexArray = indexArray;
    }

    public int size() {
      return indexArray.length;
    }

    @Override
    public String toString() {
      return Arrays.stream(indexArray)
              .mapToObj(i -> TimeSeries.this.get(i).toString())
              .collect(Collectors.joining("\n"));
    }

    public void forEach(Consumer<? super Data<T>> consumer) {
      Arrays.stream(indexArray).forEach(i -> consumer.accept(TimeSeries.this.get(i)));
    }

    @Override
    public Iterator<Data<T>> iterator() {
      return new Iterator<>() {
        private int i;
        @Override
        public boolean hasNext() {
          return i < indexArray.length;
        }

        @Override
        public Data<T> next() {
          if (!hasNext()) {
            throw new NoSuchElementException("Iterator are finito");
          }
          return TimeSeries.this.get(indexArray[i++]);
        }
      };
    }

    private TimeSeries<T> getFather() {
      return TimeSeries.this;
    }
    public Index or(TimeSeries<? extends T>.Index other) {
      if (TimeSeries.this != other.getFather()) {
        throw new IllegalArgumentException("No same TimeSeries");
      }
      return new Index(IntStream.concat(Arrays.stream(indexArray), Arrays.stream(other.indexArray))
              .sorted().distinct().toArray());
    }

    public Index and(TimeSeries<? extends T>.Index other) {
      if (TimeSeries.this != other.getFather()) {
        throw new IllegalArgumentException("No same TimeSeries");
      }
      var set = Arrays.stream(other.indexArray).boxed().collect(Collectors.toSet());
      return new Index(Arrays.stream(indexArray).filter(set::contains).toArray());
    }
  }

  private final ArrayList<Data<T>> datas = new ArrayList<>();

  public void add(long timestamp, T element) {
    Objects.requireNonNull(element);
    if (!datas.isEmpty() && datas.get(datas.size() - 1).timestamp > timestamp) {
      throw new IllegalStateException("Last timestamp are bigger than new timestamp");
    }
    datas.add(new Data<>(timestamp, element));
  }

  public int size() {
    return datas.size();
  }

  public Data<T> get(int index) {
    Objects.checkIndex(index, size());
    return datas.get(index);
  }

  public Index index() {
    return new Index(IntStream.range(0, datas.size()).toArray());
  }

  public Index index(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    return new Index(IntStream.range(0, datas.size())
            .filter(i -> predicate.test(datas.get(i).element))
            .toArray());
  }
}
