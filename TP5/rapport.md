# <p align=center> RAPPORT TP5 JAVA: Single instruction, Multiple Data (SIMD)

## Exercice 1 - Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.simd</groupId>
    <artifactId>simd</artifactId>
    <version>0.0.1-SNAPSHOT</version>

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

        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-core</artifactId>
            <version>1.35</version>
        </dependency>

        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>1.35</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <properties>
        <maven.compiler.source>19</maven.compiler.source>
        <maven.compiler.target>19</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
                <configuration>
                    <argLine>--add-modules jdk.incubator.vector</argLine>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <release>19</release>
                    <compilerArgs>
                        <compilerArg>--add-modules</compilerArg>
                        <compilerArg>jdk.incubator.vector</compilerArg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.openjdk.jmh</groupId>
                            <artifactId>jmh-generator-annprocess</artifactId>
                            <version>1.35</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.3.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <finalName>benchmarks</finalName>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>org.openjdk.jmh.Main</mainClass>
                                </transformer>
                            </transformers>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>**/module-info.class</exclude>
                                        <exclude>META-INF/MANIFEST.MF</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## Exercice 2 - Vectorized Add / Vectorized Min

1. On cherche à écrire une fonction sum qui calcule la somme des entiers d'un tableau passé en paramètre. Pour cela, nous allons utiliser l'API de vectorisation pour calculer la somme sur des vecteurs.

- Quelle est la classe qui représente des vecteurs d'entiers ?

La classe qui représente des vecteurs d'entiers est IntVector.

- Qu'est-ce qu'un VectorSpecies et quelle est la valeur de VectorSpecies que nous allons utiliser dans notre cas ?

Un **VectorSpecies** est une classe qui représente qui représente un type _byte_|_short_|_int_|_long_|_float_|_double_

- Comment créer un vecteur contenant des zéros et ayant un nombre préféré de lanes ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  public static void vectorAtZero() {
    ...
    var vector = IntVector.zero(SPECIES);
    ...
  }
}
```

- Comment calculer la taille de la boucle sur les vecteurs (loopBound) ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  public static void vectorLoopBound(int[] array) {
    ...
    var loopBound = IntVector.loopBound(array.length);
    ...
  }
}
```

- Comment faire la somme de deux vecteurs d'entiers ?

```java
import jdk.incubator.vector.IntVector;

public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

  public static void vectorAdd() {
    ...
    var vector1 = IntVector.zero(SPECIES);
    var vector2 = IntVector.broadcast(SPECIES, 42);
    vector1 = vector1.add(vector2);
    ...
  }
}
```

- Comment faire la somme de toutes les lanes d'un vecteur d'entiers ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

  public static void vectorLanesSum() {
    ...
    var vector2 = IntVector.broadcast(SPECIES, 42);
    var sum = vector2.reduceLanes(VectorOperators.ADD);
    ...
  }
}
```

- Si la longueur du tableau n'est pas un multiple du nombre de lanes, on va utiliser une post-loop, quel doit être le code de la post-loop ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  public static void vectorPostLoop(int[] array) {
    var length = array.length;
    var loopBound = SPECIES.loopBound(length);
    var vector = IntVector.zero(SPECIES);
    var i = 0;
    for (; i < loopBound; i += SPECIES.length()) {
      ...
    }
    ...
    for (; i < array.length; i++) {
      ...
    }
  }
}
```

Une fois que vous avez répondu à toutes ces questions, écrire le code de sum et vérifier que le test nommé "testSum" passe. De plus, vérifier avec les tests de performance dans VectorComputationBenchMark (dé-commenter les annotations correspondantes) que votre code est plus efficace qu'une simple boucle.
Rappel : pour lancer les tests JMH, il suffit d'exécuter java -jar target/benchmarks.jar dans un terminal (et arrêter tout les programmes qui tournent !).

```java
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
}
```

2. On souhaite écrire une méthode sumMask qui évite d'utiliser une post-loop et utilise un mask à la place.

- Comment peut-on faire une addition de deux vecteurs avec un mask ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  public static void vectorMaskAdd(int[] array) {
    ...
    var mask = SPECIES.indexInRange(i, length);
    var v = IntVector.fromArray(SPECIES, array, i, mask);
    var add = vector.add(v).reduceLanes(VectorOperators.ADD);
  }
}
```

- Comment faire pour créer un mask qui allume les bits entre i la variable de boucle et length la longueur du tableau ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  public static void vectorMaskCreator(int[] array) {
    ...
    var mask = SPECIES.indexInRange(i, length);
    ...
  }
}
```

Écrire le code de la méthode sumMask et vérifier que le test "testSumMask" passe.
Que pouvez dire en terme de performance entre sum et sumMask en utilisant les tests de performances JMH ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
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
}
```

En termes de performance les tests entre le sumMask et le sum
nous montre qu'ils sont presque équivalents.

3. On souhaite maintenant écrire une méthode min qui calcule le minimum des valeurs d'un tableau en utilisant des vecteurs et une post-loop.
   Contrairement à la somme qui a 0 comme élément nul, le minimum n'a pas d'élément nul... Quelle doit être la valeur utilisée pour initialiser de toute les lanes du vecteur avant la boucle principale ?
   Écrire le code de la méthode min, vérifier que le test nommé "testMin" passe et vérifier avec les tests JMH que votre code est plus efficace qu'une simple boucle sur les valeurs du tableau.

On peut lui assigner la valeur `Integer.MAX_VALUE`

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

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
}
```

4. On souhaite enfin écrire une méthode minMask qui au lieu d'utiliser une post-loop comme dans le code précédent, utilise un mask à la place.
   Attention, le minimum n'a pas d’élément nul (non, toujours pas !), donc on ne peut pas laisser des zéros "traîner" dans les llanes lorsque l'on fait un minimum sur deux vecteurs.
   Écrire le code de la méthode minMask et vérifier que le test nommé "testMinMask" passe.
   Que pouvez-vous dire en termes de performance entre min et minMask en utilisant les tests de performances JMH ?

```java
public class VectorComputation {
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

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
```

On peut voir que les performances entre avec et sans mask sont
presque équivalentes.

## Exercice 3 - FizzBuzz

1. On souhaite écrire dans la classe FizzBuzz une méthode fizzBuzzVector128 qui prend en paramètre une longueur et renvoie un tableau d'entiers de taille longueur contenant les valeurs de FizzBuzz en utilisant des vecteurs 128 bits d'entiers.
   Ecrire la méthode fizzBuzzVector128 sachant que les tableaux des valeurs et des deltas sont des constantes. Puis vérifier que votre implantation passe le test.
   En exécutant les tests JMH, que pouvez vous conclure en observant les différences de performance entre la version de base et la version utilisant l'API des vecteurs.

```java
public class FizzBuzz {
  private static final VectorSpecies<Integer> SPECIES_128 = IntVector.SPECIES_128;
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
}
```

En utilisant JMH on observe que l'amélioration de la vitesse est moindre.

2. On souhaite maintenant écrie une méthode fizzBuzzVector256 qui utilise des vecteurs 256 bits.
   Une fois la méthode écrite, vérifier que celle-ci passe le test.
   Utiliser les tests JMH pour vérifier la performance de votre implantation. Que pouvez vous en conclure en comparaison de la version utilisant des vecteurs 128 bits.

```java
public class FizzBuzz {
  private static final VectorSpecies<Integer> SPECIES_256 = IntVector.SPECIES_256;
  private static final int[] VALUES = Arrays.copyOf(new int[]{-3,  1,  2, -1,  4, -2, -1,  7,  8, -1, -2, 11, -1, 13, 14}, 15);
  private static final int[] DELTAS = Arrays.copyOf(new int[]{0, 15, 15,  0, 15,  0,  0, 15, 15,  0,  0, 15,  0, 15, 15}, 15);

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
}
```

En utilisant JMH on peut conclure que la version avec 128 bits est plus rapide.

3. Il existe une autre façon d'implanter algorithme, on peut ajouter 15 avec un mask. Écrire la méthode fizzBuzzVector128AddMask qui utilise des vecteurs 128 bits et l'addition avec un mask, puis vérifier avec le test que votre code fonctionne correctement. Comme précédemmment, le tableau des masques doit être une constante.
   Comparer les performances de ce nouvel algorithme par rapport votre autre implantation à base de vecteur 128 bits. Que pouvez-vous en conclure ?
   Note: il y a une astuce sur la taille du tableau des masques.

```java
public class FizzBuzz {
  private static final VectorSpecies<Integer> SPECIES_128 = IntVector.SPECIES_128;
  private static final int[] VALUES = Arrays.copyOf(new int[]{-3,  1,  2, -1,  4, -2, -1,  7,  8, -1, -2, 11, -1, 13, 14}, 15);
  private static final int[] DELTAS = Arrays.copyOf(new int[]{0, 15, 15,  0, 15,  0,  0, 15, 15,  0,  0, 15,  0, 15, 15}, 15);

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
```

On peut conclure que les add avec une value sont plus rapide que celles avec 
2 vecteurs.