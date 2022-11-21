# <p align=center> RAPPORT TP8 JAVA: TimeSeries

## Exercice 1 - Maven

```xml
       <project xmlns="http://maven.apache.org/POM/4.0.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

         <modelVersion>4.0.0</modelVersion>
         <groupId>fr.uge.series</groupId>
         <artifactId>series</artifactId>
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

## Exercice 2 - TimeSeries

1. Dans un premier temps, on va créer une classe TimeSeries ainsi qu'un record Data à l'intérieur de la classe TimeSeries qui représente une paire contenant une valeur de temps (timestamp) et un élément (element).
   Le record Data est paramétré par le type de l'élément qu'il contient.
   Écrire la classe TimeSeries dans le package fr.uge.serie, ainsi que le record interne public Data et vérifier que les tests marqués "Q1" passent.

```java
public class TimeSeries {
  public record Data<V>(long timestamp, V element) {
    public Data {
      Objects.requireNonNull(element);
    }
  }
}
```

2. On souhaite maintenant écrire les méthodes dans TimeSeries :
   - add(timestamp, element) qui permet d'ajouter un élément avec son timestamp.
   - La valeur de timestamp doit toujours être supérieure ou égale à la valeur du timestamp précédemment inséré (s'il existe).
   - size qui renvoie le nombre d'éléments ajoutés.
   - get(index) qui renvoie l'objet Data se trouvant à la position indiquée par l'index (de 0 à size - 1).

    En interne, la classe TimeSeries stocke des instances de Data dans une liste qui s'agrandit dynamiquement.
    Écrire les 3 méthodes définies ci-dessus et vérifier que les tests marqués "Q2" passent.

```java
public class TimeSeries<T> {
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
}
```

3. On souhaite maintenant créer une classe interne publique Index ainsi qu'une méthode index permettant de créer un Index stockant les indices des données de la TimeSeries sur laquelle la méthode index est appelée. L'objectif est de pouvoir ensuite accéder aux Data correspondantes dans le TimeSeries. Un Index possède une méthode size indiquant combien d'indices il contient.
   Seuls les indices des éléments ajoutés avant l'appel à la méthode index() doivent être présents dans l'Index.
   En interne, un Index stocke un tableau d'entiers correspondants à chaque indice.
   Écrire la méthode index et vérifier que les tests marqués "Q3" passent.
   Indication : Instream.range() permet de créer un Stream d'entiers.

```java
public class TimeSeries<T> {
  public final class Index {
    private final int[] indexArray;

    private Index(int size) {
      if (size < 0) {
        throw new IllegalArgumentException("negative size are forbidden");
      }
      indexArray = IntStream.range(0, size).toArray();
    }
    public int size() {
      return indexArray.length;
    }
  }
  
  public Index index() {
    return new Index(size());
  }
}
```

4. On souhaite pouvoir afficher un Index, c'est à dire afficher les éléments (avec le timestamp) référencés par un Index, un par ligne avec un pipe (|) entre le timestamp et l'élément.
   Faites les changements qui s'imposent dans la classe Index et vérifier que les tests marqués "Q4" passent.

```java
public class TimeSeries<T> {
  public record Data<V>(long timestamp, V element) {
    @Override
    public String toString() {
      return timestamp + " | " + element;
    }
  }

  public final class Index {
    @Override
    public String toString() {
      return Arrays.stream(indexArray)
              .mapToObj(i -> TimeSeries.this.get(i).toString())
              .collect(Collectors.joining("\n"));
    }
  }
}

```

5. On souhaite ajouter une autre méthode index(lambda) qui prend en paramètre une fonction/lambda qui est appelée sur chaque élément de la TimeSeries et indique si l'élément doit ou non faire partie de l'index.
   Par exemple, avec une TimeSeries contenant les éléments "hello", "time" et "series" et une lambda s -> s.charAt(1) == 'e' qui renvoie vrai si le deuxième caractère est un 'e', l'Index renvoyé contient [0, 2].
   Quel doit être le type du paramètre de la méthode index(lambda) ?
   Écrire la méthode index(filter) et vérifier que les tests marqués "Q5" passent.
   Note : On peut remarquer qu'il est possible de ré-écrire la méthode index sans paramètre pour utiliser celle avec un paramètre.

Le type du paramètre de la méthode index(lambda) doit être un Predicate

```java
public class TimeSeries<T> {
  public final class Index {
    private Index(int size, Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate);
      if (size < 0) {
        throw new IllegalArgumentException("negative size are forbidden");
      }
      indexArray = IntStream.range(0, size)
              .filter(i -> predicate.test(TimeSeries.this.get(i).element))
              .toArray();
    }
  }
  
  public Index index() {
    return new Index(size(), t -> true);
  }

  public Index index(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    return new Index(size(), predicate);
  }
}

```

6. Dans la classe Index, écrire une méthode forEach(lambda) qui prend en paramètre une fonction/lambda qui est appelée avec chaque Data référencée par les indices de l'Index.
   Par exemple, avec la TimeSeries contenant les Data (24 | "hello"), (34 | "time") et (70 | "series") et un Index [0, 2], la fonction sera appelée avec les Data (24 | "hello") et (70 | "series").
   Quel doit être le type du paramètre de la méthode forEach(lambda) ?
   Écrire la méthode forEach(lambda) dans la classe Index et vérifier que les tests marqués "Q6" passent.

Le type du paramètre dans la méthode forEach(lambda) est un Consumer.

```java
public void forEach(Consumer<? super Data<T>> consumer) {
  Arrays.stream(indexArray).forEach(i -> consumer.accept(TimeSeries.this.get(i)));
}
```

7. On souhaite maintenant pouvoir parcourir tous les Data d'un Index en utilisant une boucle for comme ceci
   Quelle interface doit implanter la classe Index pour pouvoir être utilisée dans une telle boucle ?
   Quelle méthode de l'interface doit-on implanter ? Et quel est le type de retour de cette méthode ? Faites les modifications qui s'imposent dans la classe Index et vérifiez que les tests marqués "Q7" passent.

On doit implémenter l'interface Iterable, la méthode iterator() le type de retour de cette méthode est un Iterator<Data<T>>

```java
public final class Index implements Iterable<Data<T>> {
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
}
```

8. On veut ajouter une méthode or sur un Index qui prend en paramètre un Index et renvoie un nouvel Index qui contient à la fois les indices de l'Index courant et les indices de l'Index passé en paramètre.
   Il ne doit pas être possible de faire un or avec deux Index issus de TimeSeries différentes.
   En termes d'implantation, on peut faire une implantation en O(n) mais elle est un peu compliquée à écrire. On se propose d'écrire une version en O(n.log(n)) en concaténant les Stream de chaque index puis en triant les indices et en retirant les doublons.
   Expliquer pourquoi on ne peut pas juste concaténer les deux tableaux d'indices ?
   Écrire le code de la méthode or(index) dans la classe Index et vérifier que les tests marqués "Q8" passent.
   Pour concaténer des IntStream il existe une méthode IntStream.concat

```java
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
    
    private TimeSeries<T> getFather() {
      return TimeSeries.this;
    }
    
    public Index or(Index other) {
      if (TimeSeries.this != other.getFather()) {
        throw new IllegalArgumentException("No same TimeSeries");
      }
      return new Index(IntStream.concat(Arrays.stream(indexArray), Arrays.stream(other.indexArray))
              .sorted().distinct().toArray());
    }
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
```

9. Même question que précédemment, mais au lieu de vouloir faire un or, on souhaite faire un and entre deux Index.
   En termes d'implantation, il existe un algorithme en O(n) qui est en O(1) en mémoire. À la place, nous allons utiliser un algorithme en O(n) mais qui utilise O(n) en mémoire. L'idée est de prendre un des tableaux d'indices et de stocker tous les indices dans un ensemble sans doublons puis de parcourir l'autre tableau d'indices et de vérifier que chaque indice est bien dans l'ensemble.
   Écrire le code de la méthode and(index) dans la classe Index et vérifier que les tests marqués "Q9" passent.

```java
public class TimeSeries<T> {
  public class Index implements Iterable<Data<T>> {
    public Index and(Index other) {
      if (TimeSeries.this != other.getFather()) {
        throw new IllegalArgumentException("No same TimeSeries");
      }
      var set = Arrays.stream(other.indexArray).boxed().collect(Collectors.toSet());
      return new Index(Arrays.stream(indexArray).filter(set::contains).toArray());
    }
  }
}
```

10. Pour les plus balèzes, faites en sorte que les tests marqués "Q10" passent.

```java
  public class Index implements Iterable<Data<T>> {
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
```