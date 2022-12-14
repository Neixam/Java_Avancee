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

1. On cherche ?? ??crire une fonction sum qui calcule la somme des entiers d'un tableau pass?? en param??tre. Pour cela, nous allons utiliser l'API de vectorisation pour calculer la somme sur des vecteurs.

- Quelle est la classe qui repr??sente des vecteurs d'entiers ?

La classe qui repr??sente des vecteurs d'entiers est IntVector.

- Qu'est-ce qu'un VectorSpecies et quelle est la valeur de VectorSpecies que nous allons utiliser dans notre cas ?

Un **VectorSpecies** est une classe qui repr??sente qui repr??sente un type _byte_|_short_|_int_|_long_|_float_|_double_

- Comment cr??er un vecteur contenant des z??ros et ayant un nombre pr??f??r?? de lanes ?

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

- Si la longueur du tableau n'est pas un multiple du nombre de lanes, on va utiliser une post-loop, quel doit ??tre le code de la post-loop ?

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

Une fois que vous avez r??pondu ?? toutes ces questions, ??crire le code de sum et v??rifier que le test nomm?? "testSum" passe. De plus, v??rifier avec les tests de performance dans VectorComputationBenchMark (d??-commenter les annotations correspondantes) que votre code est plus efficace qu'une simple boucle.
Rappel : pour lancer les tests JMH, il suffit d'ex??cuter java -jar target/benchmarks.jar dans un terminal (et arr??ter tout les programmes qui tournent !).

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

2. On souhaite ??crire une m??thode sumMask qui ??vite d'utiliser une post-loop et utilise un mask ?? la place.

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

- Comment faire pour cr??er un mask qui allume les bits entre i la variable de boucle et length la longueur du tableau ?

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

??crire le code de la m??thode sumMask et v??rifier que le test "testSumMask" passe.
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
nous montre qu'ils sont presque ??quivalents.

3. On souhaite maintenant ??crire une m??thode min qui calcule le minimum des valeurs d'un tableau en utilisant des vecteurs et une post-loop.
   Contrairement ?? la somme qui a 0 comme ??l??ment nul, le minimum n'a pas d'??l??ment nul... Quelle doit ??tre la valeur utilis??e pour initialiser de toute les lanes du vecteur avant la boucle principale ?
   ??crire le code de la m??thode min, v??rifier que le test nomm?? "testMin" passe et v??rifier avec les tests JMH que votre code est plus efficace qu'une simple boucle sur les valeurs du tableau.

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

4. On souhaite enfin ??crire une m??thode minMask qui au lieu d'utiliser une post-loop comme dans le code pr??c??dent, utilise un mask ?? la place.
   Attention, le minimum n'a pas d?????l??ment nul (non, toujours pas !), donc on ne peut pas laisser des z??ros "tra??ner" dans les llanes lorsque l'on fait un minimum sur deux vecteurs.
   ??crire le code de la m??thode minMask et v??rifier que le test nomm?? "testMinMask" passe.
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
presque ??quivalentes.

## Exercice 3 - FizzBuzz

1. On souhaite ??crire dans la classe FizzBuzz une m??thode fizzBuzzVector128 qui prend en param??tre une longueur et renvoie un tableau d'entiers de taille longueur contenant les valeurs de FizzBuzz en utilisant des vecteurs 128 bits d'entiers.
   Ecrire la m??thode fizzBuzzVector128 sachant que les tableaux des valeurs et des deltas sont des constantes. Puis v??rifier que votre implantation passe le test.
   En ex??cutant les tests JMH, que pouvez vous conclure en observant les diff??rences de performance entre la version de base et la version utilisant l'API des vecteurs.

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

En utilisant JMH on observe que l'am??lioration de la vitesse est moindre.

2. On souhaite maintenant ??crie une m??thode fizzBuzzVector256 qui utilise des vecteurs 256 bits.
   Une fois la m??thode ??crite, v??rifier que celle-ci passe le test.
   Utiliser les tests JMH pour v??rifier la performance de votre implantation. Que pouvez vous en conclure en comparaison de la version utilisant des vecteurs 128 bits.

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

3. Il existe une autre fa??on d'implanter algorithme, on peut ajouter 15 avec un mask. ??crire la m??thode fizzBuzzVector128AddMask qui utilise des vecteurs 128 bits et l'addition avec un mask, puis v??rifier avec le test que votre code fonctionne correctement. Comme pr??c??demmment, le tableau des masques doit ??tre une constante.
   Comparer les performances de ce nouvel algorithme par rapport votre autre implantation ?? base de vecteur 128 bits. Que pouvez-vous en conclure ?
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