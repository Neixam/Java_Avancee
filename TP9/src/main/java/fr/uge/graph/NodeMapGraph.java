package fr.uge.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

class NodeMapGraph<T> implements Graph<T> {
  private final int nodeCount;
  private final HashMap<Integer, T>[] internHashMap;

  @SuppressWarnings("unchecked")
  NodeMapGraph(int nodeCount) {
    if (nodeCount < 0) {
      throw new IllegalArgumentException("nodeCount must >= 0");
    }
    this.nodeCount = nodeCount;
    internHashMap = (HashMap<Integer, T>[]) new HashMap<?, ?>[nodeCount];
    for (var i = 0; i < nodeCount; i++) {
      internHashMap[i] = new HashMap<>();
    }
  }

  @Override
  public int nodeCount() {
    return nodeCount;
  }

  @Override
  public void addEdge(int src, int dst, T weight) {
    Objects.requireNonNull(weight);
    Objects.checkIndex(src, nodeCount);
    Objects.checkIndex(dst, nodeCount);
    internHashMap[src].put(dst, weight);
  }

  @Override
  public Optional<T> getWeight(int src, int dst) {
    Objects.checkIndex(src, nodeCount);
    Objects.checkIndex(dst, nodeCount);
    return Optional.ofNullable(internHashMap[src].get(dst));
  }


  @Override
  public Iterator<Integer> neighborIterator(int src) {
    Objects.checkIndex(src, nodeCount);
    return internHashMap[src].keySet().iterator();
  }

  @Override
  public IntStream neighborStream(int src) {
    Objects.checkIndex(src, nodeCount);
    return Graph.super.neighborStream(src);
  }
}
