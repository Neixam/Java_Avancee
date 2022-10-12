# <p align=center> RAPPORT TP3 JAVA: Slices of bread

## Exercice 1 - Maven

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>fr.uge.slice</groupId>
    <artifactId>slice</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
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

## Exercice 2 - The Slice and The furious

1. On va dans un premier temps créer une interface Slice avec une méthode array qui permet de créer un slice à partir d'un tableau en Java.

```java
public sealed interface Slice<U> permits Slice.ArraySlice {
  int size();
  U get(int index);
  final class ArraySlice<V> implements Slice<V> {

    private final V[] internArray;

    private ArraySlice(V[] array) {
      Objects.requireNonNull(array);
      internArray = array;
    }

    @Override
    public int size() {
      return internArray.length;
    }

    @Override
    public V get(int index) {
      Objects.checkIndex(index, internArray.length);
      return internArray[index];
    }
  }
  static <T> Slice<T> array(T[] array) {
    return new ArraySlice<>(array);
  }
}
```
2. On souhaite que l'affichage d'un slice affiche les valeurs séparées par des virgules avec un '[' et un ']' comme préfixe et suffixe.
   Par exemple,

```java
final class ArraySlice<V> implements Slice<V> {
  @Override
  public String toString() {
    return Arrays.toString(internArray);
  }
}
```

3. On souhaite ajouter une surcharge à la méthode array qui, en plus de prendre le tableau en paramètre, prend deux indices from et to et montre les éléments du tableau entre from inclus et to exclus.
   Par exemple

````java
  final class SubArraySlice<V> implements Slice<V> {
    private final V[] internArray;
    private final int from;
    private final int to;

    private SubArraySlice(V[] array, int from, int to) {
      Objects.requireNonNull(array);
      if (from < 0 || from > to || to > array.length) {
        throw new IndexOutOfBoundsException("");
      }
      this.from = from;
      this.to = to;
      internArray = array;
    }
    @Override
    public int size() {
      return to - from;
    }

    @Override
    public V get(int index) {
      Objects.checkIndex(index, size());
      return internArray[index + from];
    }

    @Override
    public String toString() {
      return Arrays.toString(Arrays.stream(internArray, from, to).toArray());
    }
  }
````

4. On souhaite enfin ajouter une méthode subSlice(from, to) à l'interface Slice qui renvoie un sous-slice restreint aux valeurs entre from inclus et to exclu.
   Par exemple,

```java
  final class ArraySlice<V> implements Slice<V> {
    @Override
    public Slice<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, internArray.length);
      return new SubArraySlice<>(internArray, from, to);
    }

  final class SubArraySlice<V> implements Slice<V> {
     @Override
     public Slice<V> subSlice(int from, int to) {
        Objects.checkFromToIndex(from, to, this.to - this.from);
        return new SubArraySlice<>(internArray, this.from + from, this.from + to);
     }
  }
```

## Exercice 3 - 2 Slice 2 Furious

1. Recopier l'interface Slice de l'exercice précédent dans une interface Slice2. Vous pouvez faire un copier-coller de Slice dans même package, votre IDE devrait vous proposer de renommer la copie. Puis supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la ré-implanter aussi, mais plus tard.
   Vérifier que les tests JUnit marqués "Q1" et "Q2" passent.

```java
public sealed interface Slice2<U> permits Slice2.ArraySlice, Slice2.ArraySlice.SubArraySlice {
  int size();
  U get(int index);
 /* Slice<U> subSlice(int from, int to); */
  final class ArraySlice<V> implements Slice2<V> {
    private final V[] internArray;

    private ArraySlice(V[] array) {
      Objects.requireNonNull(array);
      internArray = array;
    }

    @Override
    public int size() {
      return internArray.length;
    }

    @Override
    public V get(int index) {
      Objects.checkIndex(index, internArray.length);
      return internArray[index];
    }
/*
    @Override
    public Slice2<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, internArray.length);
      return new SubArraySlice<>(internArray, from, to);
    }
*/
    @Override
    public String toString() {
      return Arrays.toString(internArray);
    }

 }
  static <T> Slice2<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }
}
```

2. Déclarer une classe SubArraySlice à l'intérieur de la classe ArraySlice comme une inner class donc pas comme une classe statique et implanter cette classe et la méthode array(array, from, to).
   Vérifier que les tests JUnit marqués "Q3" passent.

```java
public sealed interface Slice2<U> permits Slice2.ArraySlice, Slice2.ArraySlice.SubArraySlice {
  int size();
  U get(int index);
 /* Slice<U> subSlice(int from, int to); */
  final class ArraySlice<V> implements Slice2<V> {
    private final V[] internArray;

    private ArraySlice(V[] array) {
      Objects.requireNonNull(array);
      internArray = array;
    }

    @Override
    public int size() {
      return internArray.length;
    }

    @Override
    public V get(int index) {
      Objects.checkIndex(index, internArray.length);
      return internArray[index];
    }
/*
    @Override
    public Slice2<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, internArray.length);
      return new SubArraySlice<>(internArray, from, to);
    }
*/
    @Override
    public String toString() {
      return Arrays.toString(internArray);
    }
    final class SubArraySlice implements Slice2<V> {
      private final int from;
      private final int to;

     private SubArraySlice(int from, int to) {
       Objects.checkFromToIndex(from, to, internArray.length);
       this.from = from;
       this.to = to;
     }
     @Override
     public int size() {
       return to - from;
     }

     @Override
     public V get(int index) {
       Objects.checkIndex(index, size());
       return internArray[index + from];
     }

      @Override
      public String toString() {
        return Arrays.toString(Arrays.stream(internArray, from, to).toArray());
      }
    }

 }
  static <T> Slice2<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }

  static <T> Slice2<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);
    return new ArraySlice<>(array).new SubArraySlice(from, to);
  }
}
```

3. Dé-commenter la méthode >subSlice(from, to) et fournissez une implantation de cette méthode dans la classe SubArraySlice.
   On peut aussi noter que l'on peut simplifier le code de array(array, from, to).
   Vérifier que les tests JUnit marqués "Q4" passent.

```java
  final class ArraySlice<V> implements Slice2<V> {
   @Override
   public Slice2<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, internArray.length);
      return this.new SubArraySlice(from, to);
   }
}

final class SubArraySlice implements Slice2<V> {
   @Override
   public Slice2<V> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, this.to - this.from);
      return ArraySlice.this.new SubArraySlice(this.from + from, this.from + to);
   }
}
```

4. Dans quel cas va-t-on utiliser une inner class plutôt qu'une classe interne ?

On va utiliser une inner class plutôt qu'une classe interne dans le cas où on
a un partage de champs entre deux classes.

## Exercice 4 - The Slice and The Furious: Tokyo Drift

1. Recopier l'interface Slice du premier exercice dans une interface Slice3. Supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la réimplanter plus tard.
   Puis déplacer la classe ArraySlice à l'intérieur de la méthode array(array) et transformer celle-ci en classe anonyme.
   Vérifier que les tests JUnit marqués "Q1" et "Q2" passent.

```java
public interface Slice3<U> {
   int size();
   U get(int index);
   //Slice<U> subSlice(int from, int to);
   
   static <T> Slice3<T> array(T[] array) {
      Objects.requireNonNull(array);
      return new Slice3<>() {
         @Override
         public int size() {
            return array.length;
         }

         @Override
         public T get(int index) {
            Objects.checkIndex(index, array.length);
            return array[index];
         }

         /*   @Override
            public Slice<V> subSlice(int from, int to) {
              Objects.checkFromToIndex(from, to, internArray.length);
            }
        */
         @Override
         public String toString() {
            return Arrays.toString(array);
         }
      };
   }
}
```

2. On va maintenant chercher à implanter la méthode subSlice(from, to) directement dans l'interface Slice3 comme cela l'implantation sera partagée.
   Écrire la méthode subSlice(from, to) en utilisant là encore une classe anonyme.
   Comme l'implantation est dans l'interface, on a pas accès au tableau qui lui n'existe que dans l'implantation dans array(array) mais ce n'est pas gràve car on peut utiliser les méthodes de l'interface.
   Puis fournissez une implantation à la méthode array(array, from, to).
   Vérifier que les tests JUnit marqués "Q3" et "Q4" passent.

```java
public interface Slice3<U> {
  default Slice3<U> subSlice(int from, int to) {
    Objects.checkFromToIndex(from, to, size());
    return new Slice3<>() {
      @Override
      public int size() {
        return to - from;
      }
      @Override
      public U get(int index) {
        Objects.checkIndex(index, size());
        return Slice3.this.get(index + from);
      }

      @Override
      public String toString() {
        return Arrays.toString(IntStream.range(0, size())
                .mapToObj(this::get).toArray());
      }
    };
  }
   static <T> Slice3<T> array(T[] array, int from, int to) {
      Objects.requireNonNull(array);
      Objects.checkFromToIndex(from, to, array.length);
      return array(array).subSlice(from, to);
   }
}
```

3. Dans quel cas va-t-on utiliser une classe anonyme plutôt qu'une classe interne ?

Il est préférable d'utiliser une classe anonyme plutôt qu'une classe interne
lorsque l'on veut seulement redéfinir un comportement d'une classe ou interface
déjà existante.

## Exercice 5 - Slice & Furious

1. Déclarer l'interface Slice4 avec les méthodes size, get(index) et subSlice(from, to) abstraites. De plus, la méthode array(array) peut déléguer son implantation à la méthode array(array, from, to).
   Pour l'instant, commenter la méthode subSlice(from, to) que l'on implantera plus tard.
   À la suite du fichier, déclarer une classe non publique SliceImpl implantant l'interface Slice4 et implanter la méthode array(array, from, to).
   Vérifier que les tests JUnit marqués "Q1", "Q2" et "Q3" passent.

```java
public interface Slice4<U> {
  int size();
  U get(int index);
  //Slice4<U> subSlice(int from, int to);

  static <T> Slice4<T> array(T[] array) {
    Objects.requireNonNull(array);
    return array(array, 0, array.length);
  }
  static <T> Slice4<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);
    return new Slice4<>() {
      @Override
      public int size() {
        return to - from;
      }

      @Override
      public T get(int index) {
        Objects.checkIndex(index, size());
        return array[index + from];
      }

      @Override
      public String toString() {
        return Arrays.toString(Arrays.stream(array, from, to).toArray());
      }
    };
  }
}
```

2. Dé-commenter la méthode subSlice(from, to) et fournissez une implantation de cette méthode.
   Vérifier que les tests JUnit marqués "Q4" passent.

```java
@Override
public Slice4<T> subSlice(int from2, int to) {
  Objects.checkFromToIndex(from2, to, size());
  return array(array, from2 + from, to + from);
}
```

3. On peut remarquer qu'en programmation objet il y a une toujours une tension entre avoir une seule classe et donc avoir des champs qui ne servent pas vraiment pour certaines instances et avoir plusieurs classes ayant des codes très similaires, mais avec un nombre de champs différents.
   L'orthodoxie de la POO voudrait que l'on ait juste le nombre de champs qu'il faut, en pratique, on a tendance à ne pas créer trop de classes, car plus on a de code plus c'est difficile de le faire évoluer.
   À votre avis, pour cet exemple, est-il préférable d'avoir deux classes une pour les tableaux et une pour les tableaux avec des bornes ou une seule classe gérant les deux cas ?

À mon avis il est préférable d'avoir une seule classe qui s'occupe des tableaux avec et sans bornes,
car comme on l'a vu sur la dernière partie, on peut représenter un tableau sans
borne comme un tableau avec des bornes.