package fr.uge.graph;

import java.util.*;
import java.util.stream.IntStream;

class MatrixGraph<V> implements Graph<V> {
  private final V[] internArray;
  private final int nodeCount;

  @SuppressWarnings("unchecked")
  MatrixGraph(int nodeCount) {
    if (nodeCount < 0) {
      throw new IllegalArgumentException("nodeCount must >= 0");
    }
    internArray = (V[]) new Object[nodeCount * nodeCount];
    this.nodeCount = nodeCount;
  }

  private int offset(int i, int j) {
    return i + j * nodeCount;
  }
  @Override
  public Optional<V> getWeight(int i, int j) {
    Objects.checkIndex(j, nodeCount);
    Objects.checkIndex(i, nodeCount);
    return Optional.ofNullable(internArray[offset(i, j)]);
  }

  @Override
  public Iterator<Integer> neighborIterator(int src) {
    Objects.checkIndex(src, nodeCount);
    return new Iterator<>() {

      private int j = incrementDst(0);
      private int lastDst = -1;

      @Override
      public boolean hasNext() {
        return j != nodeCount;
      }
      private int incrementDst(int index) {
        for (var i = index; i < nodeCount; i++) {
          if (getWeight(src, i).isPresent()) {
            return i;
          }
        }
        return nodeCount;
      }

      @Override
      public Integer next() {
        if (!hasNext()) {
          throw new NoSuchElementException("Iterator hadn't more element but you call next");
        }
        lastDst = j;
        j = incrementDst(j + 1);
        return lastDst;
      }

      @Override
      public void remove() {
        if (lastDst == -1) {
          throw new IllegalStateException();
        }
        internArray[offset(src, lastDst)] = null;
        lastDst = -1;
      }
    };
  }

  @Override
  public IntStream neighborStream(int src) {
    Objects.checkIndex(src, nodeCount);
    return Graph.super.neighborStream(src);
  }

  @Override
  public int nodeCount() {
    return nodeCount;
  }

  @Override
  public void addEdge(int i, int j, V element) {
    Objects.checkIndex(j, nodeCount);
    Objects.checkIndex(i, nodeCount);
    Objects.requireNonNull(element);
    internArray[offset(i, j)] = element;
  }
}
