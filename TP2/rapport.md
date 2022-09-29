# <p align=center> RAPPORT TP2 JAVA: Sed, the stream editor

## Exercice 1 - Maven

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.sed</groupId>
    <artifactId>sed</artifactId>
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

## Exercice 2 - Astra inclinant, sed non obligeant

Le but de cet exercice est de créer un petit éditeur comme [sed](https://en.wikipedia.org/wiki/Sed).
Pour ceux qui ne connaîtraient pas sed, c'est un utilitaire en ligne de commande qui prend en entrée
un fichier et génère en sortie un nouveau fichier en effectuant des transformations ligne à ligne.
[sed](https://en.wikipedia.org/wiki/Sed) permet facilement de supprimer une ligne soit spécifiée
par son numéro, soit si elle contient une expression régulière ou de remplacer un mot
(en fait une regex) par un mot.
L'utilitaire sed traite le fichier ligne à ligne, il ne stocke pas tout le fichier en mémoire
(ce n'était pas une solution viable à la création de sed en 1974). On parle de traitement en flux,
en stream en Anglais, d'où le nom de Stream EDitor, sed.

1. Dans un premier temps, on va créer une classe StreamEditor dans le package fr.uge.sed avec une méthode d'instance transform qui prend en paramètre un LineNumberReader et un Writer et écrit, ligne à ligne, le contenu du LineNumberReader dans le Writer.
   Rappel, un BufferedReader possède une méthode readLine() et un Writer une méthode append().
   Comme on veut que le programme fonctionne de la même façon, quelle que soit la plate-forme, le retour à la ligne écrit dans le Writer est toujours '\n'.
   Vérifier que les tests JUnit marqués "Q1" passent.

```java
public class StreamEditor {
  public void transform(LineNumberReader lineNumberReader, Writer writer) throws IOException {
    Objects.requireNonNull(lineNumberReader);
    Objects.requireNonNull(writer);
    for (var line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
      writer.append(line).append('\n');
    }
  }
}
```

2. On veut maintenant pouvoir spécifier une commande à la création du StreamEditor pour transformer les lignes du fichier en entrée. Ici, lineDelete renvoie un record LineDeleteCommand qui indique la ligne à supprimer (la première ligne d'un fichier est 1, pas 0).
   L'exemple ci-dessous montre comment supprimer la ligne 2 d'un fichier.

```java
public record LineDeleteCommand(int numLine) {
   public LineDeleteCommand {
      if (numLine < 0) {
         throw new IllegalArgumentException("Negative line impossible");
      }
   }
}

public class StreamEditor {
  private final LineDeleteCommand lineDeleteCommand;
  public StreamEditor() {
    lineDeleteCommand = new LineDeleteCommand(0);
  }

  public StreamEditor(LineDeleteCommand lineDeleteCommand) {
    this.lineDeleteCommand = lineDeleteCommand;
  }
  public void transform(LineNumberReader lineNumberReader, Writer writer) throws IOException {
    Objects.requireNonNull(lineNumberReader);
    Objects.requireNonNull(writer);
    for (var line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
      if (lineNumberReader.getLineNumber() != lineDeleteCommand.numLine())
        writer.append(line).append('\n');
    }
  }

  public static LineDeleteCommand lineDelete(int numLine) {
    return new LineDeleteCommand(numLine);
  }
}
```

3. On souhaite maintenant écrire un main qui prend en paramètre sur la ligne de commande un nom de fichier, supprime la ligne 2 de ce fichier et écrit le résultat sur la sortie standard.
   Vérifier que les tests JUnit marqués "Q3" passent.
   Note : on présupposera que le fichier et la sortie standard utilisent l'encodage UTF-8 (StandardCharsets.UTF_8)
   Note 2 : pour transformer un OutputStream (un PrintStream est une sorte d'OutputStream) en Writer, on utilise un OutputStreamWriter et comme on veut spécifier l'encodage, on va utiliser le constructeur qui prend aussi un Charset en paramètre.
   Rappel : vous devez utiliser un try-with-resources pour fermer correctement les ressources ouvertes.

```java
public class StreamEditor {
   public static void main(String[] args) {
      if (args.length != 1) {
         System.exit(1);
      }
      var command = StreamEditor.lineDelete(2);
      var sed = new StreamEditor(command);
      try (var reader = new LineNumberReader(Files.newBufferedReader(Path.of(args[0])));
           var writer = new OutputStreamWriter(System.out)) {
         sed.transform(reader, writer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
```

4. On souhaite introduire une nouvelle commande qui permet de supprimer une ligne si elle contient une expression régulière. Avant de coder cela, on va faire un peu de re-factoring pour préparer le fait que l'on puisse avoir plusieurs commandes.
   L'idée est que, pour chaque commande, on appelle celle-ci avec la ligne courante et le numéro de ligne et la commande nous renvoie une action à effectuer. L'action peut être soit DELETE pour indiquer que la ligne ne doit pas être affichée, soit PRINT pour indiquer que la ligne doit être affichée.
5. Maintenant que l'on a bien préparé le terrain, on peut ajouter une nouvelle commande renvoyée par la méthode findAndDelete qui prend en paramètre un java.util.regex.Pattern telle que le code suivant fonctionne
6. En fait, cette implantation n'est pas satisfaisante, car les records LineDeleteCommand et FindAndDeleteCommand ont beaucoup de code qui ne sert à rien. Il serait plus simple de les transformer en lambdas, car la véritable information intéressante est comment effectuer la transformation d'une ligne.
   Modifier votre code pour que les implantations des commandes renvoyées par les méthodes lineDelete et findAndDelete soit des lambdas.
   Vérifier que les tests JUnit marqués "Q6" passent.
7. On souhaite maintenant introduire une commande substitute(pattern, replacement) qui dans une ligne remplace toutes les occurrences du motif par une chaîne de caractère de remplacement. Malheureusement, notre enum Action n'est pas à même de gérer ce cas, car il faut que la commande puisse renvoyer PRINT mais avec une nouvelle ligne.
   On se propose pour cela de remplacer l'enum Action par une interface et DELETE et PRINT par des records implantant cette interface comme ceci
8. On peut enfin ajouter la commande substitute(pattern, replacement) telle que le code suivant fonctionne

