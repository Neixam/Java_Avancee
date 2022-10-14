package fr.uge.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JSONPrinter {
  private final static ClassValue<List<Function<Record, String>>> CACHE = new ClassValue<>() {
    @Override
    protected List<Function<Record, String>> computeValue(Class<?> type) {
      return Arrays.stream(type.getRecordComponents())
              .<Function<Record, String>>map(recordComponent -> {
                var name = trueName(recordComponent);
                return record -> "\"" + name + "\": " + invoke(record, recordComponent);
              }).toList();
    }
  };
  private static String escape(Object o) {
    return o instanceof String ? "\"" + o + "\"": "" + o;
  }
  private static String invoke(Record record, RecordComponent recordComponent) {
    try {
      return escape(recordComponent.getAccessor().invoke(record));
    } catch (InvocationTargetException e) {
      var cause = e.getCause();
      if (cause instanceof RuntimeException exception) {
        throw exception;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw new UndeclaredThrowableException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  private static String trueName(RecordComponent recordComponent) {
    var annotation = recordComponent.getAnnotation(JSONProperty.class);
    if (annotation == null) {
      return recordComponent.getName();
    }
    return annotation.value().isEmpty() ? recordComponent.getName().replace('_', '-') : annotation.value();
  }
  public static String toJSON(Record record) {
    Objects.requireNonNull(record);
    return CACHE.get(record.getClass()).stream()
            .map(f -> f.apply(record))
            .collect(Collectors.joining(",\n", "{\n", "\n}\n"));
  }

  public static void main(String[] args) {
    var person = new Person("John", "Doe");
    System.out.println(toJSON(person));
    var alien = new Alien(100, "Saturn");
    System.out.println(toJSON(alien));
  }
}
