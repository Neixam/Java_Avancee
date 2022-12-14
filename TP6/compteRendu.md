# <p align=center> RAPPORT TP6 JAVA: Implantation d'une table de hachage, classe interne

## Exercice 1 - Pas de Maven cette fois-ci

## Exercice 2 - IntHashSet

1. Quels doivent être les champs de la classe Entry correspondant à une case de la table de hachage sachant que l'on veut stocker les collisions dans une liste chaînée (que l'on va fabriquer nous-même, et pas en utilisant LinkedList).
   On cherche à écrire la classe Java Entry correspondante dans le package fr.umlv.set. Quelle doit être la visibilité de cette classe ? Quels doivent être les modificateurs pour chaque champ ? En fait, ne pourrait-on pas utiliser un record plutôt qu'une classe, ici ? Pourquoi ?
   Pour finir, il vaut mieux déclarer Entry en tant que classe interne de la classe IntHashSet plutôt qu'en tant que type dans le package fr.umlv.set .
   Pourquoi ? Quelle doit être la visibilité de Entry ?
   Écrire la classe IntHashSet dans le package fr.umlv.set et ajouter Entry en tant que classe interne.

On souhaite avoir un champ `value` qui contient la valeur de la case et un pointeur sur l'entry suivant.
Cette classe doit être private, ils doivent être finaux. Oui on peut en effet
utiliser un record car celle-ci ne doit ni être hérité ni être modifié. Oui,
il vaut mieux car elle ne sera pas utiliser autre part et ses membres seront
accessibles par la classe parente.

2. Comment écrire la fonction de hachage dans le cas où la taille de la table est 2 ? Pourquoi est-ce que ça n'est pas une bonne idée d'utiliser l'opérateur % ? Écrire une version "rapide" de la fonction de hachage
   Indice : on peut regarder le bit le plus à droite (celui des unités) pour savoir si l'on doit stocker les éléments dans la case 0 ou 1.
   Et si la taille de la table est 8 ?
   En suivant la même idée, modifier votre fonction de hachage dans le cas où la taille de la table est une puissance de 2.
   Note : si vous ne connaissez pas, allez lire le chapitre 2 du Hacker's Delight.

On peut écrire `value % 2`, car l'opérateur % est extrêmement lent.
```java
private static int hachage(int value) {
  return value & 1;
}
```
Si la taille de la table est 8 on peut faire `value & 7`. Pour le cas d'une puissance de 2 :
```java
private static int hachage(int value) {
  return value & (SIZE - 1);
}
```

3. Dans la classe IntHashSet, implanter la méthode add. Écrire également la méthode size avec une implantation en O(1).

```java
public class IntHashSet {
   private static final int SIZE = 8;

   private record Entry(int value, Entry next) {}

   private final Entry[] entries = new Entry[SIZE];
   private int size;

   private static int hachage(int i) {
      return i & (SIZE - 1);
   }

   public void add(int value) {
      var entry1 = entries[hachage(value)];
      for (var entry = entry1; entry != null; entry = entry.next) {
         if (entry.value == value) {
            return;
         }
      }
      size++;
      entries[hachage(value)] = new Entry(value, entry1);
   }

   public int size() {
      return size;
   }
}
```

4. On cherche maintenant à implanter la méthode forEach. Quelle doit être la signature de la functional interface prise en paramètre de la méthode forEach ?
   Quel est le nom de la classe du package java.util.function qui a une méthode ayant la même signature ?
   Écrire la méthode forEach.

La signature de la functional interface prise en paramètre doit être une `void f(int)` donc
on peut utiliser la classe IntConsumer.
```java
public void forEach(IntConsumer intConsumer) {
  Objects.requireNonNull(intConsumer);
  for (var i = 0; i < SIZE; i++) {
    for (var entry = entries[i]; entry != null; entry = entry.next) {
      intConsumer.accept(entry.value);
    }
  }
}
```

5. Écrire la méthode contains.

```java
public boolean contains(int value) {
  for (var entry = entries[hachage(value)]; entry != null; entry = entry.next) {
    if (entry.value == value) {
      return true;
    }
  }
  return false;
}
```

## Exercice 3 - DynamicHashSet

1. Avant tout, nous souhaitons générifier notre table de hachage, pour permettre de ne pas stocker uniquement des entiers mais n'importe quel type de valeur.
   Avant de générifier votre code, quelle est le problème avec la création de tableau ayant pour type un type paramétré ?
   Comment fait-on pour résoudre le problème, même si cela lève un warning.
   Rappeler pourquoi on a ce warning.
   Peut-on supprimer le warning ici, ou est-ce une bêtise ?
   Comment fait-on pour supprimer le warning ?
   Reprendre et générifier le code de l'exercice précédent pour fabriquer une classe de table de hachage générique.

Le problème de la création d'un tableau pour un type paramétré est qu'il y a
l'erasure. On peut faire un tableau de type __< ? >__ et on le cast en tableau de
type __< T >__. On a ce warning car même si on change le type du tableau
le tableau est créé à la base avec un certain type d'objet ce qui veut dire
que dans la mémoire il y a l'espace nécessaire pour ce type d'objet et pas un
autre on peut donc considérer que si l'on essaye de placer d'autre objet
d'un même type parent il peut créer une erreur. On peut supprimer ce warning
avec une annotation `@SuppressWarnings("unchecked")`.

```java
public class DynamicHashSet<T> {
  private record Entry<T>(T value, Entry<T> next) {
  }
  /* On peut faire un cast ici, car l'erasure crée un vide pour les types paramétriques et on sait qu'on ne rangera
  * jamais rien d'autre qu'un Entry<T> dans le tableau. */
  @SuppressWarnings("unchecked")
  private Entry<T>[] entries = (Entry<T>[]) new Entry<?>[2];
  private int size;

  private static int hachage(Object value, int size) {
    return Objects.hashCode(value) & (size - 1);
  }

  public void add(T value) {
    Objects.requireNonNull(value);
    var hachValue = hachage(value, entries.length);
    for (var entry = entries[hachValue]; entry != null; entry = entry.next) {
      if (Objects.equals(entry.value, value)) {
        return;
      }
    }
    size++;
    entries[hachValue] = new Entry<>(value, entries[hachValue]);
  }

  public boolean contains(Object value) {
    Objects.requireNonNull(value);
    for (var entry = entries[hachage(value, entries.length)]; entry != null; entry = entry.next) {
      if (Objects.equals(entry.value, value)) {
        return true;
      }
    }
    return false;
  }

  public int size() {
    return size;
  }
}
```

2. Vérifier la signature de la méthode contains de HashSet et expliquer pourquoi on utilise un type plus général que E.

On utilise un type plus général que E car contains utilise `Objects.equals()` pour
faire la comparaison.

3. Modifier le code de la méthode add pour implanter l'algorithme d'agrandissement de la table.
   L'idée est que si la longueur du tableau est inférieure à la moitié du nombre d’éléments, il faut doubler la taille du tableau et re-stocker tous les éléments (pas besoin de tester si un élément existe déjà dans le nouveau tableau).
   Note : il faut que le champ contenant le tableau ne soit plus final.

```java
public class DynamicHashSet<T> {
  @SuppressWarnings("unchecked")
  private void upgrade() {
    var newEntries = (Entry<T>[]) new Entry<?>[entries.length << 1];
    for (Entry<T> tEntry : entries) {
      for (var entry = tEntry; entry != null; entry = entry.next) {
        var hachValue = hachage(entry.value, newEntries.length);
        newEntries[hachValue] = new Entry<>(entry.value, newEntries[hachValue]);
      }
    }
    entries = newEntries;
  }

  public void add(T value) {
    Objects.requireNonNull(value);
    var hachValue = hachage(value, entries.length);
    for (var entry = entries[hachValue]; entry != null; entry = entry.next) {
      if (Objects.equals(entry.value, value)) {
        return;
      }
    }
    size++;
    entries[hachValue] = new Entry<>(value, entries[hachValue]);
    if (entries.length / 2 < size) {
      upgrade();
    }
  }
}
```

## Exercice 4 - Wild cards (optionel)

1. Écrire une méthode addAll, qui permet de recopier une collection d’éléments dans le DynamicHashSet courant.

```java
public class DynamicHashSet<T> {
  public void addAll(Collection<? extends T> values) {
    Objects.requireNonNull(values);
    values.forEach(this::add);
  }
}
```

2. Regarder la signature de la méthode addAll dans java.util.Collection. Avez-vous la même signature ? Modifier votre code si nécessaire.
   Expliquer ce que veux dire le '?' dans la signature de addAll

Oui, le '?' signifie qu'on veut prendre n'importe quels types qui sont sous-type
de T.

3. Ne devrait-on pas aussi utiliser un '?' dans la signature de la méthode forEach ?

```java
public class DynamicHashSet<T> {
  public void forEach(Consumer<? super T> tConsumer) {
    Objects.requireNonNull(tConsumer);
    Arrays.stream(entries)
            .flatMap(tEntries -> Stream.iterate(tEntries, Objects::nonNull, Entry::next).map(Entry::value))
            .forEach(tConsumer);
  }
}
```
