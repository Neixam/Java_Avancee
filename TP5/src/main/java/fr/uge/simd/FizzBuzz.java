package fr.uge.simd;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

public class FizzBuzz {
  private static final VectorSpecies<Integer> SPECIES_128 = IntVector.SPECIES_128;
  private static final VectorSpecies<Integer> SPECIES_256 = IntVector.SPECIES_256;
  private static final int[] VALUES = Arrays.copyOf(new int[]{-3,  1,  2, -1,  4, -2, -1,  7,  8, -1, -2, 11, -1, 13, 14}, 15);
  private static final int[] DELTAS = Arrays.copyOf(new int[]{0, 15, 15,  0, 15,  0,  0, 15, 15,  0,  0, 15,  0, 15, 15}, 15);

  public static int[] fizzBuzzVector128(int length) {
    var spLength = SPECIES_128.length();
    var ret = new int[length];
    var loopBound = length - length % 15;
    var mask15 = SPECIES_128.indexInRange(spLength * 3, spLength * 3 + 3);
    var v1 = IntVector.fromArray(SPECIES_128, VALUES, 0);
    var v2 = IntVector.fromArray(SPECIES_128, VALUES, spLength);
    var v3 = IntVector.fromArray(SPECIES_128, VALUES, spLength * 2);
    var v4 = IntVector.fromArray(SPECIES_128, VALUES, spLength * 3, mask15);
    var d1 = IntVector.fromArray(SPECIES_128, DELTAS, 0);
    var d2 = IntVector.fromArray(SPECIES_128, DELTAS, spLength);
    var d3 = IntVector.fromArray(SPECIES_128, DELTAS, spLength * 2);
    var d4 = IntVector.fromArray(SPECIES_128, DELTAS, spLength * 3, mask15);
    var maskEnd = SPECIES_128.indexInRange(0, 3);
    var i = 0;
    for (; i < loopBound; i += 3) {
      v1.intoArray(ret, i);
      i += spLength;
      v2.intoArray(ret, i);
      i += spLength;
      v3.intoArray(ret, i);
      i += spLength;
      v4.intoArray(ret, i, maskEnd);
      v1 = v1.add(d1);
      v2 = v2.add(d2);
      v3 = v3.add(d3);
      v4 = v4.add(d4);
    }
    var mask = SPECIES_128.indexInRange(i, length);
    v1.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_128.indexInRange(i, length);
    v2.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_128.indexInRange(i, length);
    v3.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_128.indexInRange(i, length);
    v4.intoArray(ret, i, mask);
    return ret;
  }

  public static int[] fizzBuzzVector256(int length) {
    var spLength = SPECIES_256.length();
    var ret = new int[length];
    var loopBound = length - length % 15;
    var mask15 = SPECIES_256.indexInRange(spLength, spLength * 2 - 1);
    var v1 = IntVector.fromArray(SPECIES_256, VALUES, 0);
    var v2 = IntVector.fromArray(SPECIES_256, VALUES, spLength, mask15);
    var d1 = IntVector.fromArray(SPECIES_256, DELTAS, 0);
    var d2 = IntVector.fromArray(SPECIES_256, DELTAS, spLength, mask15);
    var maskEnd = SPECIES_256.indexInRange(0, spLength - 1);
    var i = 0;
    for (; i < loopBound; i += spLength - 1) {
      v1.intoArray(ret, i);
      i += spLength;
      v2.intoArray(ret, i, maskEnd);
      v1 = v1.add(d1);
      v2 = v2.add(d2);
    }
    var mask = SPECIES_256.indexInRange(i, length);
    v1.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_256.indexInRange(i, length);
    v2.intoArray(ret, i, mask);
    return ret;
  }

  public static int[] fizzBuzzVector128AddMask(int length) {
    var spLength = SPECIES_128.length();
    var ret = new int[length];
    var loopBound = length - length % 15;
    var mask15 = SPECIES_128.indexInRange(spLength * 3, spLength * 3 + 3);
    var v1 = IntVector.fromArray(SPECIES_128, VALUES, 0);
    var v2 = IntVector.fromArray(SPECIES_128, VALUES, spLength);
    var v3 = IntVector.fromArray(SPECIES_128, VALUES, spLength * 2);
    var v4 = IntVector.fromArray(SPECIES_128, VALUES, spLength * 3, mask15);
    var maskDelta1 = v1.compare(VectorOperators.GT, 0);
    var maskDelta2 = v2.compare(VectorOperators.GT, 0);
    var maskDelta3 = v3.compare(VectorOperators.GT, 0);
    var maskDelta4 = v4.compare(VectorOperators.GT, 0);
    var maskEnd = SPECIES_128.indexInRange(0, 3);
    var i = 0;
    for (; i < loopBound; i += 3) {
      v1.intoArray(ret, i);
      i += spLength;
      v2.intoArray(ret, i);
      i += spLength;
      v3.intoArray(ret, i);
      i += spLength;
      v4.intoArray(ret, i, maskEnd);
      v1 = v1.add(15, maskDelta1);
      v2 = v2.add(15, maskDelta2);
      v3 = v3.add(15, maskDelta3);
      v4 = v4.add(15, maskDelta4);
    }
    var mask = SPECIES_128.indexInRange(i, length);
    v1.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_128.indexInRange(i, length);
    v2.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_128.indexInRange(i, length);
    v3.intoArray(ret, i, mask);
    i += mask.trueCount();
    mask = SPECIES_128.indexInRange(i, length);
    v4.intoArray(ret, i, mask);
    return ret;
  }
}
