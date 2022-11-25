# <p align=center> RAPPORT TP9 JAVA: Trop Graph

## Exercice 1 - Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.graph</groupId>
    <artifactId>graph</artifactId>
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

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
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

## Exercice 2 - MatrixGraph

1. Indiquer comment trouver la case (i, j) dans un tableau à une seule dimension de taille nodeCount * nodeCount.
   Si vous n'y arrivez pas, faites un dessin !

Pour trouver la case (i, j) dans un tableau à seule dimension il faut faire 
`interArray[j * nodeCount + i]`

2. Rappeler pourquoi, en Java, il n'est pas possible de créer des tableaux de variables de type puis implanter la classe MatrixGraph et son constructeur.
   Pouvez-vous supprimer le warning à la construction ? Pourquoi?
   Vérifier que les tests marqués "Q2" passent.

En java les types paramétrés n'existent pas après la compilation donc on ne
peut pas faire un tableau de type paramétré sans passer par un tableau 
d'Object que l'on cast dans le type paramétré. Oui car je sais le contenue du
tableau respectera toujours le contrat que j'ai instauré lors de la création
du tableau qui est que je ne stockerai que des objets du type paramétré. 

```java
public class MatrixGraph<V> implements Graph<V> {
  private final V[] internArray;

  @SuppressWarnings("unchecked")
  public MatrixGraph(int nodeCount) {
    if (nodeCount < 0) {
      throw new IllegalArgumentException("nodeCount must >= 0");
    }
    internArray = (V[]) new Object[nodeCount * nodeCount];
  }
}
```

3. On peut remarquer que la classe MatrixGraph n'apporte pas de nouvelles méthodes par rapport aux méthodes de l'interface Graph donc il n'est pas nécessaire que la classe MatrixGraph soit publique.
   Ajouter une méthode factory nommée createMatrixGraph dans l'interface Graph et déclarer la classe MatrixGraph non publique.
   Vérifier que les tests marqués "Q3" passent.

```java
public interface Graph<V> {
  static <T> Graph<T> createMatrixGraph(int nodeCount) {
    return new MatrixGraph<>(nodeCount);
  }
}

class MatrixGraph<V> implements Graph<V> {
   private final V[] internArray;

   @SuppressWarnings("unchecked")
   MatrixGraph(int nodeCount) {
      if (nodeCount < 0) {
         throw new IllegalArgumentException("nodeCount must >= 0");
      }
      internArray = (V[]) new Object[nodeCount * nodeCount];
   }
}
```

4. Afin d'implanter correctement la méthode getWeight, rappeler à quoi sert la classe java.util.Optional en Java.
   Implanter la méthode addEdge sachant que l'on ne peut pas créer un arc sans valeur.
   Implanter la méthode getWeight.
   Vérifier que les tests marqués "Q4" passent.

La classe Optional en Java permet d'avoir des valeurs d'erreur ou de non existence.

```java
class MatrixGraph<V> implements Graph<V> {
  @Override
  public Optional<V> getWeight(int i, int j) {
    Objects.checkIndex(j, nodeCount);
    Objects.checkIndex(i, nodeCount);
    return Optional.ofNullable(internArray[j * nodeCount + i]);
  }

  @Override
  public void addEdge(int i, int j, V element) {
    Objects.checkIndex(j, nodeCount);
    Objects.checkIndex(i, nodeCount);
    Objects.requireNonNull(element);
    internArray[j * nodeCount + i] = element;
  }
}
```

5. Implanter la méthode edges puis vérifier que les tests marqués "Q5" passent.

```java
class MatrixGraph<V> implements Graph<V> {
  private int offset(int i, int j) {
    return i + j * nodeCount;
  }
  
  @Override
  public void edges(int src, EdgeConsumer<? super V> consumer) {
    Objects.requireNonNull(consumer);
    Objects.checkIndex(src, nodeCount);
    IntStream.range(0, nodeCount).forEach(j -> getWeight(src, j).ifPresent(v -> consumer.edge(src, j, v)));
  }
}
```

6. Rappeler le fonctionnement d'un itérateur et de ses méthodes hasNext et next.
   Que renvoie next si hasNext retourne false ?
   Expliquer pourquoi il n'est pas nécessaire, dans un premier temps, d'implanter la méthode remove qui fait pourtant partie de l'interface.
   Implanter la méthode neighborsIterator(src) qui renvoie un itérateur sur tous les nœuds ayant un arc dont la source est src.
   Vérifier que les tests marqués "Q6" passent.
   Note: ça pourrait être une bonne idée de calculer quel est le prochain arc valide AVANT que l'on vous demande s'il existe.

Un itérateur est un objet qui permet de parcourir une structure de données,
ses méthodes hasNext et next permettent respectivement à savoir si il y a un
suivant et à l'obtenir. Il n'est pas nécessaire car on ne veut pas retirer
les éléments.

```java
class MatrixGraph<V> implements Graph<V> {
   @Override
   public Iterator<Integer> neighborIterator(int src) {
      return new Iterator<>() {
         private int j = incrementDst(0);

         @Override
         public boolean hasNext() {
            return j != nodeCount;
         }

         private int incrementDst(int index) {
            for (var i = index; i < nodeCount; i++) {
               if (getWeight(src, i).isPresent()) {
                  return i;
               }
            }
            return nodeCount;
         }

         @Override
         public Integer next() {
            if (!hasNext()) {
               throw new NoSuchElementException("Iterator hadn't more element but you call next");
            }
            var ret = j;
            j = incrementDst(j + 1);
            return ret;
         }
      };
   }
}
```

7. Pourquoi le champ nodeCount ne doit pas être déclaré private avant Java 11 ?
   Est-ce qu'il y a d'autres champs qui ne doivent pas être déclarés private avant Java 11 ?



8. On souhaite écrire la méthode neighborStream(src) qui renvoie un IntStream contenant tous les nœuds ayant un arc sortant par src.
   Pour créer le Stream ,nous allons utiliser StreamSupport.intStream qui prend en paramètre un Spliterator.OfInt. Rappeler ce qu'est un Spliterator, à quoi sert le OfInt et quelles sont les méthodes qu'il va falloir redéfinir.
   Écrire la méthode neighborStream sachant que l'on implantera le Spliterator en utilisant l'itérateur défini précédemment.
   Vérifier que les tests marqués "Q8" passent.

Un Spliterator est un objet qui compose un stream de manière interne, c'est
un genre d'iterator qui va séparer les contenues et utiliser plusieurs
iterateur qui parcourt chaque segment.

```java
class MatrixGraph<V> implements Graph<V> {
   @Override
   public IntStream neighborStream(int src) {
      Objects.checkIndex(src, nodeCount);
      var it = neighborIterator(src);
      return StreamSupport.intStream(new Spliterator.OfInt() {
         @Override
         public OfInt trySplit() {
            return null;
         }

         @Override
         public long estimateSize() {
            return Long.MAX_VALUE;
         }

         @Override
         public int characteristics() {
            return 0;
         }

         @Override
         public boolean tryAdvance(IntConsumer action) {
            if (it.hasNext()) {
               action.accept(it.next());
               return true;
            }
            return false;
         }
      }, false);
   }
}
```

9. On peut remarquer que neighborStream dépend de neighborsIterator et donc pas d'une implantation spécifique. On peut donc écrire neighborStream directement dans l'interface Graph comme ça le code sera partagé.
   Rappeler comment on fait pour avoir une méthode 'instance avec du code dans une interface.
   Déplacer neighborStream dans Graph et vérifier que les tests unitaires passent toujours.



10. Expliquer le fonctionnement précis de la méthode remove de l'interface Iterator.
    Implanter la méthode remove de l'itérateur.
    Vérifier que les tests marqués "Q10" passent.

```java
class MatrixGraph<V> implements Graph<V> {
  @Override
  public Iterator<Integer> neighborIterator(int src) {
    Objects.checkIndex(src, nodeCount);
    return new Iterator<>() {
      private int j = incrementDst(0);
      private int lastDst = -1;
      
      @Override
      public Integer next() {
        if (!hasNext()) {
          throw new NoSuchElementException("Iterator hadn't more element but you call next");
        }
        lastDst = j;
        j = incrementDst(j + 1);
        return lastDst;
      }

      @Override
      public void remove() {
        if (lastDst == -1) {
          throw new IllegalStateException();
        }
        internArray[offset(src, lastDst)] = null;
        lastDst = -1;
      }
    };
  }
}
```

11. On peut remarquer que l'on peut ré-écrire edges en utilisant neighborsStream, en une ligne :) et donc déplacer edges dans Graph.
    Déplacer le code de la méthode edges dans Graph.


```java
class MatrixGraph<V> implements Graph<V> {
   @Override
   public void edges(int src, EdgeConsumer<? super V> consumer) {
      Objects.requireNonNull(consumer);
      Objects.checkIndex(src, nodeCount);
      neighborStream(src).forEach(dst -> consumer.edge(src, dst, internArray[offset(src, dst)]));
   }
}
```

## Exercice 3 - NodeMapGraph

1. Écrire dans l'interface Graph la méthode createNodeMapGraph et implanter la classe NodeMapGraph (toujours non publique).
   Note: chaque méthode est sensée ne pas prendre plus de 2 ou 3 lignes, tests des préconditions compris.
