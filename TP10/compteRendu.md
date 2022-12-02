# <p align=center> RAPPORT TP10 JAVA: Structure de données persistante (fonctionnelle)

## Exercice 1 - Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.seq</groupId>
    <artifactId>seq</artifactId>
    <version>0.0.1-SNAPSHOT</version>

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

## Exercice 2 - Seq

1. Écrire le code de la classe Seq dans le package fr.uge.seq.

```java
public class Seq<T> {
  private final List<? extends T> elements;

  private Seq(List<? extends T> elements) {
    Objects.requireNonNull(elements);
    this.elements = elements;
  }
  public static <T> Seq<T> from(List<? extends T> elements) {
    Objects.requireNonNull(elements);
    return new Seq<>(List.copyOf(elements));
  }

  public int size() {
    return elements.size();
  }

  public T get(int index) {
    Objects.checkIndex(index, size());
    return elements.get(index);
  }
}
```

2. Écrire une méthode d'affichage permettant d'afficher les valeurs d'un Seq séparées par des virgules (suivies d'un espace), l'ensemble des valeurs étant encadré par des chevrons ('<' et '>').
   Par exemple, avec le Seq créé précédemment

```java
public class Seq<T> {
  @Override
  public String toString() {
    return elements.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", ", "<", ">"));
  }
}
```

3. Écrire une méthode of permettant d'initialiser un Seq à partir de valeurs séparées par des virgules.
   Par exemple, on pourra créer le Seq précédent comme ceci. Note : si vous avez des warnings, vous avez un problème.
   Note 2 : si vous pensez un @SuppressWarnings, pensez plus fort !

```java
public class Seq<T> {
  @SafeVarargs
  public static <T> Seq<T> of(T ... elements) {
    Objects.requireNonNull(elements);
    return new Seq<>(List.of(elements));
  }
}
```

4. Écrire une méthode forEach qui prend en paramètre une fonction qui prend en paramètre chaque élément un par un et fait un effet de bord.
   Par exemple, on pourra afficher les éléments un par un de la façon suivante

```java
public class Seq<T> {
  @SafeVarargs
  public void forEach(Consumer<? super T> consumer) {
     Objects.requireNonNull(consumer);
     elements.forEach(consumer);
  }
}
```

5. On souhaite écrire une méthode map qui prend en paramètre une fonction à appliquer à chaque élément d'un Seq pour créer un nouveau Seq. On souhaite avoir une implantation paresseuse, c'est-à-dire une implantation qui ne fait pas de calcul si ce n'est pas nécessaire. Par exemple, tant que personne n'accède à un élément du nouveau Seq, il n'est pas nécessaire d'appliquer la fonction. L'idée est de stoker les anciens éléments ainsi que la fonction et de l'appliquer seulement si c'est nécessaire.
   Bien sûr, cela va nous obliger à changer l'implantation déjà existante de Seq car maintenant tous les Seq vont stocker une liste d'éléments ainsi qu'une fonction de transformation (de mapping).
   Exemple d'utilisationAvant de se lancer dans l'implantation de map, quelle doit être sa signature ?
   Quel doit être le type des éléments de la liste ? Et le type de la fonction stockée ?
   Faire les modifications correspondantes, puis changer le code des méthodes pour les prendre en compte. Enfin, écrire le code de map.
   Note : le code doit fonctionner si l'on appelle map deux fois successivement.

```java
public class Seq<T> {
   private final List<?> elements;
   private final Function<? super Object, ? extends T> mapper;

   private Seq(List<?> elements, Function<? super Object, ? extends T> mapper) {
      Objects.requireNonNull(elements);
      Objects.requireNonNull(mapper);
      this.elements = elements;
      this.mapper = mapper;
   }

   public static <T> Seq<T> from(List<? extends T> elements) {
      Objects.requireNonNull(elements);
      return new Seq<>(List.copyOf(elements), e -> (T) e);
   }

   @SafeVarargs
   public static <T> Seq<T> of(T... elements) {
      Objects.requireNonNull(elements);
      return new Seq<>(List.of(elements), e -> (T) e);
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

   public void forEach(Consumer<? super T> consumer) {
      Objects.requireNonNull(consumer);
      elements.stream().map(mapper).forEach(consumer);
   }

   @Override
   public String toString() {
      return elements.stream()
              .map(mapper)
              .map(Object::toString)
              .collect(Collectors.joining(", ", "<", ">"));
   }
}
```

6. Écrire une méthode findFirst qui renvoie le premier élément du Seq si celui-ci existe.

```java
public class Seq<T> {
  public Optional<T> findFirst() {
    if (elements.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(mapper.apply(elements.get(0)));
  }
}
```

7. Faire en sorte que l'on puisse utiliser la boucle for-each-in sur un Seq

```java
public class Seq<T> implements Iterable<T> {
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
}
```

8. Enfin, on souhaite implanter la méthode stream() qui renvoie un Stream des éléments du Seq. Pour cela, on va commencer par implanter un Spliterator que l'on peut construire à partir du Spliterator déjà existant de la liste (que l'on obtient avec la méthode List.spliterator()).
   Puis en utilisant la méthode StreamSupport.stream, créer un Stream à partir de ce Spliterator.
   Écrire la méthode stream().

```java
public class Seq<T> implements Iterable<T> {
  public Stream<T> stream() {
    var spliterator = elements.spliterator();
    return StreamSupport.stream(new Spliterator<>() {
      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        return spliterator.tryAdvance(e -> action.accept(mapper.apply(e)));
      }

      @Override
      public Spliterator<T> trySplit() {
        return null;
      }

      @Override
      public long estimateSize() {
        return spliterator.estimateSize();
      }

      @Override
      public int characteristics() {
        return spliterator.characteristics() | IMMUTABLE | ORDERED | NONNULL;
      }
    }, true);
  }
}
```

9. (Optionnel) Si vous ne l'avez pas déjà fait, on souhaite que le Stream renvoyé par la méthode stream permette d’effectuer les calculs en parallèle sur les éléments du Seq.
   Modifier votre implantation (commentez l'ancienne) et vérifier que les tests marqués "Q9" passent.

```java
public class Seq<T> implements Iterable<T> {
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
        return spliterator().characteristics() | IMMUTABLE | ORDERED | NONNULL;
      }
    };
  }

  public Stream<T> stream() {
    var spliterator = elements.spliterator();
    return StreamSupport.stream(fromSplit(spliterator), false);
  }
}
```

## Exercice 3 - Seq2 le retour (à la maison)

1. Ré-implanter toutes les méthodes publiques de Seq dans une classe Seq2 en utilisant un tableau d'éléments en interne.

```java
public class Seq2<T> implements Iterable<T> {
  private final Object[] elements;
  private final Function<? super Object, ? extends T> mapper;

  private Seq2(List<?> elements, Function<? super Object, ? extends T> mapper) {
    Objects.requireNonNull(elements);
    Objects.requireNonNull(mapper);
    this.elements = elements.toArray();
    this.mapper = mapper;
  }

  public static <T> Seq2<T> from(List<? extends T> elements) {
    Objects.requireNonNull(elements);
    return new Seq2<>(List.copyOf(elements), e -> (T) e);
  }

  @SafeVarargs
  public static <T> Seq2<T> of(T ... elements) {
    Objects.requireNonNull(elements);
    return from(List.of(elements));
  }

  public int size() {
    return elements.length;
  }

  public T get(int index) {
    Objects.checkIndex(index, size());
    return mapper.apply(elements[index]);
  }

  public <R> Seq2<R> map(Function<? super T, ? extends R> mapper) {
    Objects.requireNonNull(mapper);
    return new Seq2<>(Arrays.asList(elements), mapper.compose(this.mapper));
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
    Arrays.stream(elements).map(mapper).forEach(consumer);
  }

  public Optional<T> findFirst() {
    if (elements.length == 0) {
      return Optional.empty();
    }
    return Optional.of(get(0));
  }

  private Spliterator<T> fromArray(int start, int end) {
    return new Spliterator<>() {
      private int i = start;
      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (i >= end) {
          return false;
        }
        action.accept(get(i++));
        return true;
      }

      @Override
      public Spliterator<T> trySplit() {
        var middle = (end + i) >>> 1;
        if (middle == i) {
          return null;
        }
        var split = fromArray(i, middle);
        i = middle;
        return split;
      }

      @Override
      public long estimateSize() {
        return end - i;
      }

      @Override
      public int characteristics() {
        return IMMUTABLE | ORDERED | NONNULL | SIZED | SUBSIZED;
      }
    };
  }

  public Stream<T> stream() {
    return StreamSupport.stream(fromArray(0, size()), false);
  }

  @Override
  public String toString() {
    return Arrays.stream(elements)
            .map(mapper)
            .map(Object::toString)
            .collect(Collectors.joining(", ", "<", ">"));
  }
}
```