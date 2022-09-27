# <p align=center> RAPPORT TP1 JAVA

## Exercice 1 - Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.ymca</groupId>
    <artifactId>ymca</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

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
                    <compilerArgs>
                        <compilerArg>--enable-preview</compilerArg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
                <configuration>
                    <argLine>--enable-preview</argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Exercice 2 - YMCA

#### 1. Écrire le code de VillagePeople tel que l'on puisse créer des VillagePeople avec leur nom et leur sorte.

```java
public enum Kind {
    COP, NATIVE, GI, BIKER, CONSTRUCTION, COWBOY, ADMIRAL, ATHLETE, GIGOLO, SAILOR
}

public record VillagePeople(String name, Kind kind) {
    @Override
    public String toString() {
        return name + " (" + kind + ")";
    }
}
```

#### 2. On veut maintenant introduire une maison House qui va contenir des VillagePeople. Une maison possède une méthode add qui permet d'ajouter un VillagePeople dans la maison (note : il est possible d'ajouter plusieurs fois le même). L'affichage d'une maison doit renvoyer le texte "House with" suivi des noms des VillagePeople ajoutés à la maison, séparés par une virgule. Dans le cas où une maison est vide, le texte est "Empty House".

```java
public final class House {
    private final ArrayList<VillagePeople> peoples = new ArrayList<>();

    void add(VillagePeople people) {
        peoples.add(Objects.requireNonNull(people));
    }

    @Override
    public String toString() {
        if (peoples.isEmpty())
            return "Empty House";
        return peoples.stream()
                .map(VillagePeople::name)
                .collect(Collectors
                        .joining(", ", "House with ", ""));
    }
}
```

#### 3. En fait on veut que l'affichage affiche les noms des VillagePeople dans l'ordre alphabétique, il va donc falloir trier les noms avant de les afficher. On pourrait créer une liste intermédiaire des noms puis les trier avec un appel à list.sort(null) mais cela commence à faire beaucoup de code pour un truc assez simple. Heureusement, il y a plus simple, on va utiliser un Stream pour faire l'affichage. Dans un premier temps, ré-écrire le code de l'affichage (commenter l'ancien) pour utiliser un Stream sans se préoccuper du tri et vérifier que les tests de la question précédente passent toujours. Puis demander au Stream de se trier et vérifier que les tests marqués "Q3" passent.

```java
public final class House {
    @Override
    public String toString() {
        if (peoples.isEmpty())
            return "Empty House";
        return peoples.stream()
                .map(VillagePeople::name)
                .sorted()
                .collect(Collectors
                        .joining(", ", "House with ", ""));
    }
}
```

#### 4. En fait, avoir une maison qui ne peut accepter que des VillagePeople n'est pas une bonne décision en termes de business, ils ne sont pas assez nombreux. YMCA décide donc qu'en plus des VillagePeople ses maisons permettent maintenant d'accueillir aussi des Minions, une autre population sans logement. On souhaite donc ajouter un type Minion (qui possède juste un nom name) et changer le code de House pour permettre d'ajouter des VillagePeople ou des Minion. Un Minion affiche son nom suivi entre parenthèse du texte "MINION".

```java
public interface People {
    String name();
}

public record VillagePeople(String name, Kind kind) implements People {}

public record Minion(String name) implements People {
    public Minion {
        Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return name + " (MINION)";
    }
}

public final class House {
    private final ArrayList<People> peoples = new ArrayList<>();

    void add(People people) {
        peoples.add(Objects.requireNonNull(people));
    }

    @Override
    public String toString() {
        if (peoples.isEmpty())
            return "Empty House";
        return peoples.stream()
                .map(People::name)
                .sorted()
                .collect(Collectors
                        .joining(", ", "House with ", ""));
    }
}
```

#### 5. On cherche à ajouter une méthode averagePrice à House qui renvoie le prix moyen pour une nuit sachant que le prix pour une nuit pour un VillagePeople est 100 et le prix pour une nuit pour un Minion est 1 (il vaut mieux être du bon côté du pistolet à prouts). Le prix moyen (renvoyé par averagePrice) est la moyenne des prix des VillagePeople et Minion présent dans la maison. Écrire la méthode averagePrice en utilisant le polymorphisme (late dispatch) pour trouver le prix de chaque VillagePeople ou Minion.

```java
public interface People {
    double price();
}

public record Minion(String name) implements People {
    @Override
    public double price() {
        return 1;
    }
}

public record VillagePeople(String name, Kind kind) implements People {
    @Override
    public double price() {
        return 100;
    }
}

public final class House {
    public double averagePrice() {
        return peoples.stream().mapToDouble(this::price).average().orElse(Double.NaN);
    }
}
```

#### 6. En fait, cette implantation n'est pas satisfaisante car elle ajoute une méthode publique dans VillagePeople et Minion alors que c'est un détail d'implantation. Au lieu d'utiliser la POO (programmation orienté objet), on va utiliser la POD (programmation orienté data) qui consiste à utiliser le pattern matching pour connaître le prix par nuit d'un VillagePeople ou un Minion. Modifier votre code pour introduire une méthode privée qui prend en paramètre un VillagePeople ou un Minion et renvoie son prix par nuit puis utilisez cette méthode pour calculer le prix moyen par nuit d'une maison.

```java
public final class House {
    private static int price(People people) {
        return switch (people) {
            case VillagePeople ignored -> 100;
            case Minion ignored -> 1;
            default -> 0;
        };
    }

    public double averagePrice() {
      return peoples.stream().mapToDouble(this::price).average().orElse(Double.NaN);
    }
}
```

#### 7. L'implantation précédente pose problème : il est possible d'ajouter une autre personne qu'un VillagePeople ou un Minion, mais celle-ci ne sera pas prise en compte par le pattern matching. Pour cela, on va interdire qu'une personne soit autre chose qu'un VillagePeople ou un Minion en scellant le super type commun.

```java
public sealed interface People permits Minion, VillagePeople {
    String name();
}

public final class House {
    private static double price(People people) {
        return switch (people) {
            case VillagePeople ignored -> 100;
            case Minion ignored -> 1;
        };
    }
}
```

#### 8. On veut périodiquement faire un geste commercial pour une maison envers une catégorie/sorte de VillagePeople en appliquant une réduction de 80% pour tous les VillagePeople ayant la même sorte (par exemple, pour tous les BIKERs). Pour cela, on se propose d'ajouter une méthode addDiscount qui prend une sorte en paramètre et offre un discount pour tous les VillagePeople de cette sorte. Si l'on appelle deux fois addDiscount avec la même sorte, le discount n'est appliqué qu'une fois.

```java
public final class House {
    private final HashSet<Kind> discount = new HashSet<>();
    
    private double price(People people) {
        return switch (people) {
            case VillagePeople(String ignored,Kind kind) when discount.contains(kind) -> 20;
            case VillagePeople ignored -> 100;
            case Minion ignored -> 1;
        };
    }

    public double averagePrice() {
        return peoples.stream().mapToDouble(this::price).average().orElse(Double.NaN);
    }

    public void addDiscount(Kind kind) {
        discount.add(Objects.requireNonNull(kind));
    }
}
```

#### 9. Enfin, on souhaite pouvoir supprimer l'offre commerciale (discount) en ajoutant la méthode removeDiscount qui supprime le discount si celui-ci a été ajouté précédemment ou plante s'il n'y a pas de discount pour la sorte prise en paramètre.

```java
public final class House {
    private final HashSet<Kind> discount = new HashSet<>();

    public void removeDiscount(Kind kind) {
        if (!discount.remove(Objects.requireNonNull(kind))) {
            throw new IllegalStateException("No discount for " + kind);
        }
    }
}
```

#### 10. Optionnellement, faire en sorte que l'on puisse ajouter un discount suivi d'un pourcentage de réduction, c'est à dire un entier entre 0 et 100, en implantant une méthode addDiscount(kind, percent). Ajouter également une méthode priceByDiscount qui renvoie une table associative qui a un pourcentage renvoie la somme des prix par nuit auxquels on a appliqué ce pourcentage (la somme est aussi un entier). La somme totale doit être la même que la somme de tous les prix par nuit (donc ne m'oubliez pas les minions). Comme précédemment, les pourcentages ne se cumulent pas si on appelle addDiscount plusieurs fois.

```java
import java.util.stream.Collectors;

public final class House {
    private final HashMap<Kind, Integer> discount = new HashMap<>();

    private int price(People people) {
        return switch (people) {
            case VillagePeople(String ignored,Kind kind) when discount.containsKey(kind) -> 100 - discount.get(kind);
            case VillagePeople ignored -> 100;
            case Minion ignored -> 1;
        };
    }

    public void addDiscount(Kind kind) {
        Objects.requireNonNull(kind);
        this.addDiscount(kind, 80);
    }

    public void addDiscount(Kind kind, int percent) {
        Objects.requireNonNull(kind);
        if (percent < 0 || percent > 100)
            throw new IllegalArgumentException(percent + " must have a value between 0 to 100");
        discount.compute(kind, (k, v) -> percent);
    }

    public Map<Integer, Integer> priceByDiscount() {
        return peoples.stream().collect(Collectors.groupingBy(p -> switch (p) {
            case VillagePeople(String ignored,Kind kind)when discount.containsKey(kind) -> discount.get(kind);
            default -> 0;
        }, HashMap::new, Collectors.summingInt(this::price)));
    }

    public void removeDiscount(Kind kind) {
        Objects.requireNonNull(kind);
        if (discount.remove(kind) == null) {
            throw new IllegalStateException("No discount for " + kind);
        }
    }
}
```