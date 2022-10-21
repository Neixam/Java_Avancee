package fr.uge.simd;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  public static int sum(int[] array) {
    var length = array.length;
    var loopBound = SPECIES.loopBound(length);
    var vector = IntVector.zero(SPECIES);
    var i = 0;
    for (; i < loopBound; i += SPECIES.length()) {
      var v = IntVector.fromArray(SPECIES, array, i);
      vector = vector.add(v);
    }
    var ret = vector.reduceLanes(VectorOperators.ADD);
    for (; i < array.length; i++) {
      ret += array[i];
    }
    return ret;
  }

  public static int sumMask(int[] array) {
    var length = array.length;
    var loopBound = SPECIES.loopBound(length);
    var vector = IntVector.zero(SPECIES);
    var i = 0;
    for (; i < loopBound; i += SPECIES.length()) {
      var v = IntVector.fromArray(SPECIES, array, i);
      vector = vector.add(v);
    }
    var mask = SPECIES.indexInRange(i, length);
    var v = IntVector.fromArray(SPECIES, array, i, mask);
    return vector.add(v).reduceLanes(VectorOperators.ADD);
  }

  public static int min(int[] array) {
    var length = array.length;
    var loopBound = SPECIES.loopBound(length);
    var vector = IntVector.broadcast(SPECIES, Integer.MAX_VALUE);
    var i = 0;
    for (; i < loopBound; i += SPECIES.length()) {
      var v = IntVector.fromArray(SPECIES, array, i);
      vector = vector.lanewise(VectorOperators.MIN, v);
    }
    var min = vector.reduceLanes(VectorOperators.MIN);
    for (; i < length; i++) {
      min = Math.min(min, array[i]);
    }
    return min;
  }

  public static int minMask(int[] array) {
    var length = array.length;
    var loopBound = SPECIES.loopBound(length);
    var vector = IntVector.broadcast(SPECIES, Integer.MAX_VALUE);
    var i = 0;
    for (; i < loopBound; i += SPECIES.length()) {
      var v = IntVector.fromArray(SPECIES, array, i);
      vector = vector.min(v);
    }
    var mask = SPECIES.indexInRange(i, length);
    var v = IntVector.fromArray(SPECIES, array, i, mask);
    return vector.lanewise(VectorOperators.MIN, v, mask).reduceLanes(VectorOperators.MIN);
  }
}
