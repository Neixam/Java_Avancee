package fr.uge.seq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("static-method")
public class Seq2Test {
  @Nested
  public class Q1  {
    @Test
    public void testFromSimple() {
      Seq2<String> seq = Seq2.from(List.of("foo", "bar"));
      assertEquals(2, seq.size());
    }

    @Test
    public void testFromSimple2() {
      Seq2<Integer> seq = Seq2.from(List.of(12, 45));
      assertEquals(2, seq.size());
    }

    @Test
    public void testFromNullList() {
      assertThrows(NullPointerException.class, () -> Seq2.from(null));
    }

    @Test
    public void testFromNullElement() {
      var list = new ArrayList<String>();
      list.add(null);
      assertThrows(NullPointerException.class, () -> Seq2.from(list));
    }

    @Test
    public void testFromSize() {
      var seq = Seq2.from(List.of("78", "56", "34", "23"));
      assertEquals(4, seq.size());
    }

    @Test
    public void testFromSizeEmpty() {
      var seq = Seq2.from(List.of());
      assertEquals(0, seq.size());
    }

    @Test
    public void testFromGet() {
      var seq = Seq2.from(List.of(101, 201, 301));
      assertAll(
          () -> assertEquals(101, seq.get(0)),
          () -> assertEquals(201, seq.get(1)),
          () -> assertEquals(301, seq.get(2))
      );
    }

    @Test
    public void testFromMutation() {
      var list = new ArrayList<>(List.of(4, 5, 8));
      var seq = Seq2.from(list);
      list.add(10);
      assertEquals(3, seq.size());
    }

    @Test
    public void testFromGetOutOfBounds() {
      var seq = Seq2.from(List.of(24, 36));
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(2))
      );
    }

    @Test
    public void testFromSignature() {
      var list = List.of(76, 87);
      Seq2<Object> seq = Seq2.from(list);  // this should compile
      assertNotNull(seq);
    }

    @Test
    public void testFieldsAreFinal() {
      assertTrue(Arrays.stream(Seq2.class.getDeclaredFields())
          .allMatch(f -> (f.getModifiers() & (PRIVATE | FINAL)) == (PRIVATE | FINAL)));
    }
  }


  @Nested
  public class Q2  {
    @Test
    public void testToString() {
      var seq = Seq2.from(List.of(8, 5, 3));
      assertEquals(seq.toString(), "<8, 5, 3>");
    }

    @Test
    public void testToStringOneElement() {
      var seq = Seq2.from(List.of("hello"));
      assertEquals(seq.toString(), "<hello>");
    }

    @Test
    public void testToStringEmpty() {
      var seq = Seq2.from(List.of());
      assertEquals(seq.toString(), "<>");
    }
  }


  @Nested
  public class Q3  {
    @Test
    public void testOfSimple() {
      Seq2<String> seq = Seq2.of("foo", "bar");
      assertEquals(2, seq.size());
    }

    @Test
    public void testOfSimple2() {
      Seq2<Integer> seq = Seq2.of(12, 45);
      assertEquals(2, seq.size());
    }

    @Test
    public void testOfNullArray() {
      assertThrows(NullPointerException.class, () -> Seq2.of((Object)null));
    }

    @Test
    public void testOfNullElement() {
      assertThrows(NullPointerException.class, () -> Seq2.of((Object[])null));
    }

    @Test
    public void testOfSize() {
      var seq = Seq2.of("78", "56", "34", "23");
      assertEquals(4, seq.size());
    }

    @Test
    public void testOfGet() {
      var seq = Seq2.of(101, 201, 301);
      assertAll(
          () -> assertEquals((Integer)101, seq.get(0)),
          () -> assertEquals((Integer)201, seq.get(1)),
          () -> assertEquals((Integer)301, seq.get(2))
      );
    }

    @Test
    public void testOfGetOutOfBounds() {
      var seq = Seq2.of("foo", "bar");
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(2))
      );
    }

    @Test
    public void testOfToString() {
      var seq = Seq2.of(8, 5, 3);
      assertEquals(seq.toString(), "<8, 5, 3>");
    }

    @Test
    public void testOfToStringOneElement() {
      var seq = Seq2.of("hello");
      assertEquals(seq.toString(), "<hello>");
    }

    @Test
    public void testOfToStringEmpty() {
      var seq = Seq2.of();
      assertEquals(seq.toString(), "<>");
    }
  }


  @Nested
  public class Q4  {
    @Test
    public void testForEachEmpty() {
      var empty = Seq2.of();
      empty.forEach(x -> fail("should not be called"));
    }

    @Test
    public void testForEachSignature() {
      var seq = Seq2.of(1);
      seq.forEach((Object o) -> assertEquals(1, o));
    }

    @Test
    public void testForEachNull() {
      var seq = Seq2.of(1, 2);
      assertThrows(NullPointerException.class, () -> seq.forEach(null));
    }

    @Test
    public void testForEachNullEmpty() {
      var seq = Seq2.of();
      assertThrows(NullPointerException.class, () -> seq.forEach(null));
    }

    @Test
    public void testForEachALot() {
      var list = range(0, 1_000_000).boxed().toList();
      var seq = Seq2.from(list);
      var l = new ArrayList<Integer>();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> seq.forEach(l::add));
      assertEquals(list, l);
    }
  }


  @Nested
  public class Q5  {
    @Test
    public void testMapSimple() {
      Seq2<String> seq = Seq2.of("1", "2");
      Seq2<Integer> seq2 = seq.map(Integer::parseInt);

      ArrayList<Integer> list = new ArrayList<>();
      seq2.forEach(list::add);
      assertEquals(List.of(1, 2), list);
    }

    @Test
    public void testMapNull() {
      var seq = Seq2.of(1, 2);
      assertThrows(NullPointerException.class, () -> seq.map(null));
    }

    @Test
    public void testMapSignature1() {
      var seq = Seq2.of(11, 75);
      UnaryOperator<Object> identity = x -> x;
      Seq2<Object> seq2 = seq.map(identity);
      var list = new ArrayList<>();
      seq2.forEach(list::add);
      assertEquals(List.of(11, 75), list);
    }

    @Test
    public void testMapSignature2() {
      var seq = Seq2.of("foo", "bar");
      UnaryOperator<String> identity = x -> x;
      Seq2<Object> seq2 = seq.map(identity);
      var list = new ArrayList<>();
      seq2.forEach(list::add);
      assertEquals(List.of("foo", "bar"), list);
    }

    @Test
    public void testMapGet() {
      var seq = Seq2.of(101, 201, 301);
      var seq2 = seq.map(x -> 2 * x);
      assertAll(
          () -> assertEquals((Integer)202, seq2.get(0)),
          () -> assertEquals((Integer)402, seq2.get(1)),
          () -> assertEquals((Integer)602, seq2.get(2))
      );
    }

    @Test
    public void testMapGetNotCalledIfOutOfBounds() {
      var seq = Seq2.of(24, 36).map(__ -> fail(""));
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(2))
      );
    }

    @Test
    public void testMapSize() {
      var seq = Seq2.of(101, 201, 301);
      seq = seq.map(x -> 2 * x);
      assertEquals(3, seq.size());
    }

    @Test
    public void testMapNotCalledForSize() {
      var seq = Seq2.of(42, 777);
      var seq2 = seq.map(x -> { fail("should not be called"); return null; });

      assertEquals(2, seq2.size());
    }

    @Test
    public void testMapShouldNotBeCalledForSize() {
      var seq = Seq2.of(42, 777);
      var seq2 = seq.map(x -> { fail("should not be called"); return null; });
      var seq3 = seq2.map(x -> { fail("should not be called"); return null; });

      assertEquals(2, seq3.size());
    }

    @Test
    public void testMapToString() {
      var seq = Seq2.of(10, 20);
      seq = seq.map(x -> 2 * x);
      assertEquals("<20, 40>", seq.toString());
    }

    @Test
    public void testMapToStringShouldNotBeCalledIfEmpty() {
      var seq = Seq2.of().map(__ -> fail(""));
      assertEquals("<>", seq.toString());
    }

    @Test
    public void testMapForEach() {
      var seq = Seq2.of("1", "2", "3");
      var seq2 = seq.map(Integer::parseInt);

      var list = new ArrayList<Integer>();
      seq2.forEach(list::add);
      assertEquals(List.of(1, 2, 3), list);
    }

    @Test
    public void testMapForEachCompose() {
      var seq = Seq2.of("1", "2", "3");
      var seq2 = seq.map(Integer::parseInt);
      var seq3 = seq2.map(String::valueOf);

      var list = new ArrayList<String>();
      seq3.forEach(list::add);
      assertEquals(List.of("1", "2", "3"), list);
    }

    @Test
    public void testMapForEachShouldNotBeCalledIfEmpty() {
      var seq = Seq2.of().map(__ -> fail(""));
      seq.forEach(__ -> fail(""));
    }
  }

  @Nested
  public class Q6  {
    @Test
    public void testFirstSimple() {
      assertAll(
          () -> assertEquals("1", Seq2.of("1", "2").findFirst().orElseThrow()),
          () -> assertEquals((Integer)11, Seq2.of(11, 13).findFirst().orElseThrow())
      );
    }

    @Test
    public void testFirstEmpty() {
      assertAll(
          () -> assertTrue(Seq2.of().findFirst().isEmpty()),
          () -> assertFalse(Seq2.of().findFirst().isPresent())
      );
    }

    @Test
    public void testFirstMap() {
      var seq1 = Seq2.of("1", "3").map(s -> s.concat(" zorg"));
      var seq2 = Seq2.of().map(s -> s + " zorg");
      assertAll(
          () -> assertEquals("1 zorg", seq1.findFirst().orElseThrow()),
          () -> assertTrue(seq2.findFirst().isEmpty())
      );
    }

    @Test
    public void testFirstMapCompose() {
      var seq1 = Seq2.of("1", "3", "2");
      var seq2 = seq1.map(Integer::parseInt);
      var seq3 = seq2.map(String::valueOf);
      assertEquals("1", seq3.findFirst().orElseThrow());
    }

    @Test
    public void testFirstMapNotCalledIfEmpty() {
      var seq = Seq2.of().map(__ -> fail(""));
      assertTrue(seq.findFirst().isEmpty());
    }

    @Test
    public void testFirstMapNotCalledMoreThanOnce() {
      var fun = new Object() {
        int counter;
        Object apply(Object o) {
          counter++;
          return o;
        }
      };
      var seq = Seq2.of(1, 8, 45).map(fun::apply);
      assertEquals(1, seq.findFirst().orElseThrow());
      assertEquals(1, fun.counter);
    }
  }


  @Nested
  public class Q7  {
    @Test
    public void testIteratorEnhancedForIntegers() {
      var seq = Seq2.of(25, 52);
      var list = new ArrayList<Integer>();
      for(Integer value: seq) {
        list.add(value);
      }
      assertEquals(List.of(25, 52), list);
    }

    @Test
    public void testIteratorEnhancedForStrings() {
      var seq = Seq2.of("25", "52");
      var list = new ArrayList<String>();
      for(String value: seq) {
        list.add(value);
      }
      assertEquals(List.of("25", "52"), list);
    }

    @Test
    public void testIterator() {
      var seq = Seq2.of("foo", "bar");
      Iterator<String> it = seq.iterator();
      assertTrue(it.hasNext());
      assertEquals("foo", it.next());
      assertTrue(it.hasNext());
      assertEquals("bar", it.next());
      assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorALot() {
      var seq = Seq2.from(range(0, 10_000).boxed().toList());
      Iterator<Integer> it = seq.iterator();
      for(var i = 0; i < 10_000; i++) {
        IntStream.range(0, 17).forEach(x -> assertTrue(it.hasNext()));
        assertEquals(i, (int)it.next());
      }
      IntStream.range(0, 17).forEach(x -> assertFalse(it.hasNext()));
    }

    @Test
    public void testIteratorAtTheEnd() {
      var seq = Seq2.of(67, 89);
      Iterator<Integer> it = seq.iterator();
      assertEquals(67, (int)it.next());
      assertEquals(89, (int)it.next());
      assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void testIteratorMap() {
      var seq = Seq2.of(13, 666).map(x -> x / 2);
      var list = new ArrayList<Integer>();
      seq.iterator().forEachRemaining(list::add);
      assertEquals(List.of(6, 333), list);
    }

    @Test
    public void testIteratorRemove() {
      var empty = Seq2.of();
      assertThrows(UnsupportedOperationException.class, () -> empty.iterator().remove());
    }

    @Test
    public void testIteratorMapNotCalledIfEmpty() {
      var seq = Seq2.of().map(__ -> fail(""));
      var it = seq.iterator();
      assertFalse(it.hasNext());
    }
  }


  @Nested
  public class Q8  {
    @Test
    public void testStreamSimple() {
      var list = List.of("foo", "bar");
      var seq = Seq2.from(list);
      Stream<String> stream = seq.stream();
      assertEquals(list, stream.toList());
    }

    @Test
    public void testStreamSimple2() {
      var list = new ArrayList<Integer>();
      Stream<Integer> stream = Seq2.of(7, 77).stream();
      stream.forEach(list::add);
      assertEquals(List.of(7, 77), list);
    }

    @Test
    public void testStreamCount() {
      var list = range(0, 1_000).boxed().toList();
      var seq = Seq2.from(list);
      var stream = seq.stream();
      assertEquals(seq.size(), stream.count());
    }

    @Test
    public void testLazyMApAndNoAdditionalDataStructure() {
      var list = List.of("foo", "bar");
      var stream = Seq2.from(list).map(__->fail()).stream();
      assertNotNull(stream);
    }

    @Test
    public void testStreamALot() {
      var list = range(0, 1_000_000).boxed().toList();
      var stream = Seq2.from(list).stream();
      assertEquals(list, stream.toList());
    }

    @Test
    public void testParallelStreamALot() {
      var list = range(0, 1_000_000).boxed().toList();
      var stream = Seq2.from(list).stream().parallel();
      assertEquals(list, stream.toList());
    }

    @Test
    public void testStreamSpliteratorCharacteristic() {
      var spliterator = Seq2.of("foo").stream().spliterator();
      assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
      assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
      assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    public void testStreamConsumerNull() {
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> Seq2.of().stream().spliterator().forEachRemaining(null)),
          () -> assertThrows(NullPointerException.class, () -> Seq2.of().stream().spliterator().tryAdvance(null))
      );
    }

    @Test
    public void testStreamSpliteratorNotSplittable() {
      assertAll(
          () -> assertNull(Seq2.of().stream().spliterator().trySplit()),
          () -> assertNull(Seq2.of("foo").stream().spliterator().trySplit())
      );
    }

    @Test
    public void testMapStream() {
      var seq1 = Seq2.of("1", "3").map(s -> s.concat(" fizz"));
      var seq2 = seq1.map(s -> s + " buzz");
      assertAll(
          () -> assertEquals("1 fizz - 3 fizz",seq1.stream().collect(Collectors.joining(" - "))),
          () -> assertEquals("1 fizz buzz - 3 fizz buzz",seq2.stream().collect(Collectors.joining(" - ")))
      );
    }

    @Test
    public void testFirstMapStream() {
      var seq1 = Seq2.of("1", "3").map(s -> s.concat(" zorg"));
      var seq2 = Seq2.of().map(s -> s + " zorg");
      assertAll(
          () -> assertEquals(seq1.stream().findFirst().orElseThrow(), seq1.findFirst().orElseThrow()),
          () -> assertTrue(seq2.findFirst().isEmpty())
      );
    }
  }


  @Nested
  public class Q9  {
    @Test
    public void testStreamSpliteratorSplittableAndFair() {
      var list = range(0, 1_000_000).boxed().toList();
      var spliterator = Seq2.from(list).stream().spliterator();
      var spliterator2 = spliterator.trySplit();
      assertNotNull(spliterator2);
      assertEquals(1_000_000 / 2, spliterator.estimateSize());
      assertEquals(1_000_000 / 2, spliterator2.estimateSize());
    }

    @Test
    public void testStreamSpliteratorSplittableWithMap() {
      var list = range(0, 1_000_000).boxed().toList();
      var spliterator = Seq2.from(list).map(x->42).stream().spliterator();
      var newSpliterator = spliterator.trySplit();
      assertNotNull(newSpliterator);
      assertEquals(42, StreamSupport.stream(newSpliterator,false).findFirst().orElseThrow());
    }

    @Test
    public void testStreamSpliteratorSplitable() {
      var list = range(0, 1_000_000).boxed().toList();
      var spliterator = Seq2.from(list).stream().spliterator();
      assertNotNull(spliterator.trySplit());
    }

    @Test
    public void testStreamSpliteratorSplitableAndFair() {
      var list = range(0, 1_000_000).boxed().toList();
      var spliterator = Seq2.from(list).stream().spliterator();
      var spliterator2 = spliterator.trySplit();
      assertNotNull(spliterator2);
      assertEquals(1_000_000 / 2, spliterator.estimateSize());
      assertEquals(1_000_000 / 2, spliterator2.estimateSize());
    }
    
    @Test
    public void testStreamSpliteratorSplitableWithMap() {
      var list = range(0, 1_000_000).boxed().toList();
      var spliterator = Seq2.from(list).map(x->42).stream().spliterator();
      var newSpliterator = spliterator.trySplit();
      assertNotNull(newSpliterator);
      assertEquals(42, StreamSupport.stream(newSpliterator,false).findFirst().orElseThrow());
    }
  }
}