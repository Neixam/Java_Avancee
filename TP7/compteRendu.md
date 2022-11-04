# <p align=center> RAPPORT TP7 JAVA: Faites la queue

## Exercice 1 - Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.fifo</groupId>
    <artifactId>fifo</artifactId>
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

## Exercice 2 - Fifo

1. Cette représentation peut poser problème, car si la tête et la queue correspondent au même indice, il n'est pas facile de détecter si cela veux dire que la file est pleine ou vide.
   Comment doit-on faire pour détecter si la file est pleine ou vide ?
   Cette question a plusieurs réponses possibles :).

On peut mettre un champ supplémentaire qui retient le nombre d'éléments dans la
fifo.

2. Écrire une classe Fifo générique (avec une variable de type E) dans le package fr.uge.fifo prenant en paramètre le nombre maximal d’éléments que peut stocker la structure de données. Pensez à vérifier les préconditions.

```java
public class Fifo<E> {
  private final E[] internTab;
  private int size;
  private int head;
  private int tail;

  @SuppressWarnings("unchecked")
  public Fifo(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must > 0");
    }
    internTab = (E[]) new Object[capacity];
  }
}
```

3. Écrire la méthode offer qui ajoute un élément de type E dans la file. Pensez à vérifier les préconditions sachant que, notamment, on veut interdire le stockage de null.
   Comment détecter que la file est pleine ?
   Que faire si la file est pleine ?

Pour détecter que la file est pleine on peut voir si size est égal à la taille
du tableau interne. On peut throw une exception qui indique que l'état de
l'objet ne permet pas l'appelle de la méthode.

```java
public class Fifo<E> {
  public void offer(E value) {
    Objects.requireNonNull(value);
    if (size == internTab.length) {
      throw new IllegalStateException("the queue are full");
    }
    internTab[tail] = value;
    size++;
    tail = (tail + 1) % internTab.length;
  }
}
```

4. Écrire une méthode poll qui retire un élément de type E de la file. Penser à vérifier les préconditions.
   Que faire si la file est vide ?

Si la file est vide on peut throw une exception qui indique que l'objet n'est
pas en état d'exécuter la méthode.

```java
public class Fifo<E> {
  public E poll() {
    if (size == 0) {
      throw new IllegalStateException("the queue are empty");
    }
    var ret = internTab[head];
    internTab[head] = null;
    head = (head + 1) % internTab.length;
    size--;
    return ret;
  }
}
```

5. Ajouter une méthode d'affichage qui affiche les éléments dans l'ordre dans lequel ils seraient sortis en utilisant poll. L'ensemble des éléments devra être affiché entre crochets ('[' et ']') avec les éléments séparés par des virgules (suivies d'un espace).
   Note : attention à bien faire la différence entre la file pleine et la file vide.
   Note 2 : Il existe une classe StringJoiner qui est ici plus pratique à utiliser qu'un StringBuilder !
   Indication : Vous avez le droit d'utiliser 2 compteurs.

```java
public class Fifo<E> {
  @Override
  public String toString() {
    var joiner = new StringJoiner(", ", "[", "]");
    int current = head;
    for (int i = 0; i < size; i++) {
      joiner.add(internTab[current].toString());
      current = (current + 1) % internTab.length;
    }
    return joiner.toString();
  }
}
```

6. Rappelez ce qu'est un memory leak en Java et assurez-vous que votre implantation n'a pas ce comportement indésirable.

Un memory leak en Java, c'est lorsque l'on a un objet qui n'est pas supprimé
par le garbage collector.

7. Ajouter une méthode size et une méthode isEmpty.

```java
public class Fifo<E> {
  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }
}
```

8. Rappelez quel est le principe d'un itérateur.
   Quel doit être le type de retour de la méthode iterator() ?

Un itérateur est un objet qui permet de parcourir un objet qui contient des
éléments. Elle doit être `Iterator<E>`.

9. Implanter la méthode iterator().
   Note : ici, pour simplifier le problème, on considérera que l'itérateur ne peut pas supprimer des éléments pendant son parcours.

```java
public class Fifo<E> {
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int current = head;
      private int i;

      @Override
      public boolean hasNext() {
        return i < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no next");
        }
        var ret = internTab[current];
        current = (current + 1) % internTab.length;
        i++;
        return ret;
      }
    };
  }
}
```

10. Rappeler à quoi sert l'interface Iterable.
    Faire en sorte que votre file soit Iterable.

L'interface Iterable permet de faire une boucle foreach sur un objet et qui
passe par la méthode iterator() donc par l'iterator de notre objet.

```java
public class Fifo<E> implements Iterable<E>{
  private final E[] internTab;
  private int size;
  private int head;
  private int tail;

  @SuppressWarnings("unchecked")
  public Fifo(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must > 0");
    }
    internTab = (E[]) new Object[capacity];
  }

  public void offer(E value) {
    Objects.requireNonNull(value);
    if (size == internTab.length) {
      throw new IllegalStateException("the queue are full");
    }
    internTab[tail] = value;
    size++;
    tail = (tail + 1) % internTab.length;
  }

  public E poll() {
    if (size == 0) {
      throw new IllegalStateException("the queue are empty");
    }
    var ret = internTab[head];
    internTab[head] = null;
    head = (head + 1) % internTab.length;
    size--;
    return ret;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public String toString() {
    var joiner = new StringJoiner(", ", "[", "]");
    int current = head;
    for (int i = 0; i < size; i++) {
      joiner.add(internTab[current].toString());
      current = (current + 1) % internTab.length;
    }
    return joiner.toString();
  }

  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int current = head;
      private int i;

      @Override
      public boolean hasNext() {
        return i < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no next");
        }
        var ret = internTab[current];
        current = (current + 1) % internTab.length;
        i++;
        return ret;
      }
    };
  }
}
```

## Exercice 3 - ResizeableFifo

1. Indiquer comment agrandir la file si celle-ci est pleine et que l'on veut doubler sa taille. Attention, il faut penser au cas où le début de la liste a un indice qui est supérieur à l'indice indiquant la fin de la file.
   Implanter la solution retenue dans une nouvelle classe ResizeableFifo.
   Note: il existe les méthodes Arrays.copyOf et System.arraycopy.

On peut agrandir le tableau interne en doublant sa taille par deux et écrire
la tête au début du nouveau tableau et toutes les valeurs de la file à suite.

```java
public class ResizeableFifo<E> implements Iterable<E> {
  @SuppressWarnings("unchecked")
  private E[] internTab;
  private int size;
  private int head;
  private int tail;

  @SuppressWarnings("unchecked")
  public ResizeableFifo(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must > 0");
    }
    internTab = (E[]) new Object[capacity];
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private int current = head;
      private int i;

      @Override
      public boolean hasNext() {
        return i < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no next");
        }
        var ret = internTab[current];
        current = (current + 1) % internTab.length;
        i++;
        return ret;
      }
    };
  }

  @Override
  public int size() {
    return size;
  }

  private void upgrade() {
    @SuppressWarnings("unchecked")
    var newInternTab = (E[]) new Object[internTab.length << 1];
    if (head >= tail) {
      System.arraycopy(internTab, head, newInternTab, 0, internTab.length - head);
      System.arraycopy(internTab, 0, newInternTab, internTab.length - head, size - (internTab.length - head));
    }
    else {
      System.arraycopy(internTab, head, newInternTab, 0, size);
    }
    internTab = newInternTab;
    head = 0;
    tail = size;
  }

  @Override
  public void offer(E e) {
    Objects.requireNonNull(e);
    if (size == internTab.length) {
      upgrade();
    }
    internTab[tail] = e;
    size++;
    tail = (tail + 1) % internTab.length;
  }

  @Override
  public E poll() {
    if (size == 0) {
       throw new IllegalStateException("the queue are empty");
    }
    var ret = internTab[head];
    internTab[head] = null;
    head = (head + 1) % internTab.length;
    size--;
    return ret;
  }

  @Override
  public String toString() {
    var joiner = new StringJoiner(", ", "[", "]");
    int current = head;
    for (int i = 0; i < size; i++) {
      joiner.add(internTab[current].toString());
      current = (current + 1) % internTab.length;
    }
    return joiner.toString();
  }
}
```

2. En fait, il existe déjà une interface pour les files dans le JDK appelée java.util.Queue.
   Sachant qu'il existe une classe AbstractQueue qui fournit déjà des implantations par défaut de l'interface Queue indiquer

- quelles sont les méthodes supplémentaires à implanter;

La méthode supplémentaire à ajouté est la méthode `peek()`.

- quelles sont les méthodes dont l'implantation doit être modifiée;

Les méthodes `offer()` et `poll()` qui n'ont pas la même signature ni le 
même comportement.

- quelles sont les méthodes que l'on peut supprimer.

Aucune.

  Faire en sorte que la classe ResizableFifo implante l'interface Queue.
  
```java
public class ResizeableFifo<E> extends AbstractQueue<E> {
  @SuppressWarnings("unchecked")
  private E[] internTab;
  private int size;
  private int head;
  private int tail;

  @SuppressWarnings("unchecked")
  public ResizeableFifo(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("capacity must > 0");
    }
    internTab = (E[]) new Object[capacity];
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private int current = head;
      private int i;

      @Override
      public boolean hasNext() {
        return i < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException("no next");
        }
        var ret = internTab[current];
        current = (current + 1) % internTab.length;
        i++;
        return ret;
      }
    };
  }

  @Override
  public int size() {
    return size;
  }

  private void upgrade() {
    @SuppressWarnings("unchecked")
    var newInternTab = (E[]) new Object[internTab.length << 1];
    if (head >= tail) {
      System.arraycopy(internTab, head, newInternTab, 0, internTab.length - head);
      System.arraycopy(internTab, 0, newInternTab, internTab.length - head, size - (internTab.length - head));
    }
    else {
      System.arraycopy(internTab, head, newInternTab, 0, size);
    }
    internTab = newInternTab;
    head = 0;
    tail = size;
  }

  @Override
  public boolean offer(E e) {
    Objects.requireNonNull(e);
    if (size == internTab.length) {
      upgrade();
    }
    internTab[tail] = e;
    size++;
    tail = (tail + 1) % internTab.length;
    return true;
  }

  @Override
  public E poll() {
    if (size == 0) {
      return null;
    }
    var ret = internTab[head];
    internTab[head] = null;
    head = (head + 1) % internTab.length;
    size--;
    return ret;
  }

  @Override
  public E peek() {
    return internTab[head];
  }

  @Override
  public String toString() {
    var joiner = new StringJoiner(", ", "[", "]");
    int current = head;
    for (int i = 0; i < size; i++) {
      joiner.add(internTab[current].toString());
      current = (current + 1) % internTab.length;
    }
    return joiner.toString();
  }
}
```