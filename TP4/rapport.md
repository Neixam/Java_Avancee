# <p align=center> RAPPORT TP4 JAVA: JSON, réflexion et annotations

## Exercice 1 - Maven 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.json</groupId>
    <artifactId>json</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>19</maven.compiler.source>
        <maven.compiler.target>19</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.9.0</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.13.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <release>19</release>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
            </plugin>
        </plugins>
    </build>
</project>
```

## Exercice 2 - JSON Encoder

1. Écrire la méthode toJSON qui prend en paramètre un java.lang.Record, utilise la réflexion pour accéder à l'ensemble des composants d'un record (java.lang.Class.getRecordComponent), sélectionne les accesseurs, puis affiche les couples nom du composant, valeur associée.
   Puis vérifier que les tests marqués "Q1" passent.

```java
public class JSONPrinter {
   private static String escape(Object o) {
      return o instanceof String ? "\"" + o + "\"": "" + o;
   }
   public static String toJSON(Record record) {
      Objects.requireNonNull(record);
      return Arrays.stream(record.getClass().getRecordComponents())
              .map(recordComponent -> {
                 try {
                    return "\t\""
                            + recordComponent.getName()
                            + "\": "
                            + escape(recordComponent.getAccessor().invoke(record));
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
              })
              .collect(Collectors.joining(",\n", "{\n", "\n}\n"));
   }
}
```

2. En fait, on peut avoir des noms de clé d'objet JSON qui ne sont pas des noms valides en Java, par exemple "book-title", pour cela on se propose d'utiliser un annotation pour indiquer quel doit être le nom de clé utilisé pour générer le JSON.
   Déclarez l'annotation JSONProperty visible à l'exécution et permettant d'annoter des composants de record, puis modifiez le code de toJSON pour n'utiliser que les propriétés issues de méthodes marquées par l'annotation JSONProperty.
   Puis vérifier que les tests marqués "Q2" passent (et uniquement ceux-là pour l'instant).

```java
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONProperty {
   String value() default "";
}

public class JSONPrinter {
   private static String escape(Object o) {
      return o instanceof String ? "\"" + o + "\"" : "" + o;
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
      return recordComponent.isAnnotationPresent(JSONProperty.class) ?
              recordComponent.getAnnotation(JSONProperty.class).value() : recordComponent.getName();
   }

   public static String toJSON(Record record) {
      Objects.requireNonNull(record);
      return Arrays.stream(record.getClass().getRecordComponents())
              .map(recordComponent -> "\t\"" + trueName(recordComponent) + "\": " + invoke(record, recordComponent))
              .collect(Collectors.joining(",\n", "{\n", "\n}\n"));
   }
}
```

3. En fait, on veut aussi gérer le fait que l'annotation peut ne pas être présente et aussi le fait que si l'annotation est présente mais sans valeur spécifiée alors le nom du composant est utilisé avec les '_' réécrits en '-'.
   Modifier le code dans JSONPrinter et la déclaration de l'annotation en conséquence.
   Pour tester, vérifier que tous les tests jusqu'à ceux marqués "Q3" passent.
   Rappel : la valeur par défaut d'un attribut d'une annotation ne peut pas être null.

```java
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONProperty {
   String value() default "";
}

public class JSONPrinter {
   private static String trueName(RecordComponent recordComponent) {
      var annotation = recordComponent.getAnnotation(JSONProperty.class);
      if (annotation == null) {
         return recordComponent.getName();
      }
      return annotation.value().isEmpty() ? recordComponent.getName().replace('_', '-') : annotation.value();
   }
}
```

4. En fait, l'appel à getRecordComponents est lent; regardez la signature de cette méthode et expliquez pourquoi...

L'appel à cette méthode est lent, car il crée à chaque fois un nouveau tableau des composants du record.

5. Nous allons donc limiter les appels à getRecordComponents en stockant le résultat de getRecordComponents dans un cache pour éviter de faire l'appel à chaque fois qu'on utilise toJSON.
   Utilisez la classe java.lang.ClassValue pour mettre en cache le résultat d'un appel à getRecordComponents pour une classe donnée.

```java
public class JSONPrinter {
   private final static ClassValue<RecordComponent[]> CACHE = new ClassValue<RecordComponent[]>() {
      @Override
      protected RecordComponent[] computeValue(Class<?> type) {
         return type.getRecordComponents();
      }
   };

   public static String toJSON(Record record) {
      Objects.requireNonNull(record);
      return Arrays.stream(CACHE.get(record.getClass()))
              .map(recordComponent -> "\t\"" + trueName(recordComponent) + "\": " + invoke(record, recordComponent))
              .collect(Collectors.joining(",\n", "{\n", "\n}\n"));
   }
}
```

6. En fait, on peut mettre en cache plus d'informations que juste les méthodes, on peut aussi pré-calculer le nom des propriétés pour éviter d'accéder aux annotations à chaque appel.
   Écrire le code qui pré-calcule le maximum de choses pour que l'appel à toJSON soit le plus efficace possible.
   Indication : quelle est la lettre grecque entre kappa et mu?

```java
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

   public static String toJSON(Record record) {
      Objects.requireNonNull(record);
      return CACHE.get(record.getClass()).stream()
              .map(f -> f.apply(record))
              .collect(Collectors.joining(",\n", "{\n", "\n}\n"));
   }
}
```