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

```java
public class StreamEditor {
   private enum Action {
      DELETE, PRINT
   }
   
   public Action deleteOrPrint(String line, int numLine) {
      if (lineDeleteCommand.numLine() == numLine) {
         return Action.DELETE;
      }
      return Action.PRINT;
   }

   public void transform(LineNumberReader lineNumberReader, Writer writer) throws IOException {
      Objects.requireNonNull(lineNumberReader);
      Objects.requireNonNull(writer);
      for (var line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
         if (deleteOrPrint(line, lineNumberReader.getLineNumber()) == Action.PRINT) {
            writer.append(line).append('\n');
         }
      }
   }
}
```

5. Maintenant que l'on a bien préparé le terrain, on peut ajouter une nouvelle commande renvoyée par la méthode findAndDelete qui prend en paramètre un java.util.regex.Pattern telle que le code suivant fonctionne

```java
public interface Command {
   boolean deleteOrPrint(String line, int numLine);
}

public record FindAndDeleteCommand(Pattern pattern) implements Command {
   public FindAndDeleteCommand {
      Objects.requireNonNull(pattern);
   }

   @Override
   public boolean deleteOrPrint(String line, int numLine) {
      return pattern.matcher(line).find();
   }
}

public record LineDeleteCommand(int numLine) implements Command {
   public LineDeleteCommand {
      if (numLine < 0) {
         throw new IllegalArgumentException("Negative line impossible");
      }
   }

   @Override
   public boolean deleteOrPrint(String line, int numLine) {
      return numLine == this.numLine;
   }
}

public class StreamEditor {
   private final Command command;

   public Action deleteOrPrint(String line, int numLine) {
      if (command.deleteOrPrint(line, numLine)) {
         return Action.DELETE;
      }
      return Action.PRINT;
   }

   public StreamEditor(Command command) {
      Objects.requireNonNull(command);
      this.command = command;
   }

   public void transform(LineNumberReader lineNumberReader, Writer writer) throws IOException {
      Objects.requireNonNull(lineNumberReader);
      Objects.requireNonNull(writer);
      for (var line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
         if (deleteOrPrint(line, lineNumberReader.getLineNumber()) == Action.PRINT) {
            writer.append(line).append('\n');
         }
      }
   }

   public static FindAndDeleteCommand findAndDelete(Pattern pattern) {
      return new FindAndDeleteCommand(pattern);
   }
}
```

6. En fait, cette implantation n'est pas satisfaisante, car les records LineDeleteCommand et FindAndDeleteCommand ont beaucoup de code qui ne sert à rien. Il serait plus simple de les transformer en lambdas, car la véritable information intéressante est comment effectuer la transformation d'une ligne.
   Modifier votre code pour que les implantations des commandes renvoyées par les méthodes lineDelete et findAndDelete soit des lambdas.
   Vérifier que les tests JUnit marqués "Q6" passent.

```java
public class StreamEditor {
   @FunctionalInterface
   public interface Command {
      Action deleteOrPrint(String line, int numLine);
   }

   private enum Action {
      DELETE, PRINT
   }

   private final Command command;

   public StreamEditor() {
      command = (line, numLine) -> Action.PRINT;
   }

   public StreamEditor(Command command) {
      Objects.requireNonNull(command);
      this.command = command;
   }

   public void transform(LineNumberReader lineNumberReader, Writer writer) throws IOException {
      Objects.requireNonNull(lineNumberReader);
      Objects.requireNonNull(writer);
      for (var line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
         if (command.deleteOrPrint(line, lineNumberReader.getLineNumber()) == Action.PRINT) {
            writer.append(line).append('\n');
         }
      }
   }

   public static Command lineDelete(int numLine) {
      if (numLine < 0) {
         throw new IllegalArgumentException("Negative value are forbidden");
      }
      return (line, numLine2) -> (numLine2 == numLine) ? Action.DELETE : Action.PRINT;
   }

   public static Command findAndDelete(Pattern pattern) {
      Objects.requireNonNull(pattern);
      return (line, numLine) -> (pattern.matcher(line).find()) ? Action.DELETE : Action.PRINT;
   }
}
```

7. On souhaite maintenant introduire une commande substitute(pattern, replacement) qui dans une ligne remplace toutes les occurrences du motif par une chaîne de caractère de remplacement. Malheureusement, notre enum Action n'est pas à même de gérer ce cas, car il faut que la commande puisse renvoyer PRINT mais avec une nouvelle ligne.
   On se propose pour cela de remplacer l'enum Action par une interface et DELETE et PRINT par des records implantant cette interface comme ceci

```java
  private sealed interface Action {
    default String newLine() {
      return "";
    }
    record DeleteAction() implements Action {}
    record PrintAction(String text) implements Action {
      @Override
      public String newLine() {
        return text;
      }
    }
  }
```

8. On peut enfin ajouter la commande substitute(pattern, replacement) telle que le code suivant fonctionne

```java
public class StreamEditor {
   @FunctionalInterface
   public interface Command {
      Action deleteOrPrint(String line, int numLine);
   }

   private sealed interface Action {
      default Optional<String> newLine() {
         return Optional.empty();
      }

      record DeleteAction() implements Action {
      }

      record PrintAction(String text) implements Action {
         @Override
         public Optional<String> newLine() {
            return Optional.of(text);
         }
      }
   }

   private final Command command;

   public StreamEditor() {
      this((line, numLine) -> new Action.PrintAction(line));
   }

   public StreamEditor(Command command) {
      Objects.requireNonNull(command);
      this.command = command;
   }

   public void transform(LineNumberReader lineNumberReader, Writer writer) throws IOException {
      Objects.requireNonNull(lineNumberReader);
      Objects.requireNonNull(writer);
      for (var line = lineNumberReader.readLine(); line != null; line = lineNumberReader.readLine()) {
         switch (command.deleteOrPrint(line, lineNumberReader.getLineNumber())) {
            case Action.PrintAction printAction -> writer.append(printAction.text()).append('\n');
            case Action.DeleteAction ignored -> {
            }
         }
      }
   }

   public static Command substitute(Pattern pattern, String remplacement) {
      Objects.requireNonNull(pattern);
      Objects.requireNonNull(remplacement);
      return (line, numLine) -> new Action.PrintAction(pattern.matcher(line).replaceAll(remplacement));
   }
}
```

9. Optionnellement, créer une DeleteAction avec un new semble bizarre car une DeleteAction est un record qui n'a pas de composant donc on pourrait toujours utiliser la même instance.
   Comment faire tout en gardant le record DeleteAction pour éviter de faire un new à chaque fois que l'on veut avoir une instance de DeleteAction ?
   En fait, il y a une façon plus élégante de faire la même chose que la précédente en transformant DeleteAction en enum (chercher "enum singleton java" sur internet).
   Vérifier que les tests JUnit marqués "Q9" passent.

On peut créer un champ static dans la classe qui sera constant et créer qu'une seule fois.

```java
public class StreamEditor {
   private sealed interface Action {
      enum DeleteAction implements Action {
         INSTANCE
      }

      record PrintAction(String text) implements Action {
      }
   }
   
   public static Command lineDelete(int numLine) {
      if (numLine < 0) {
         throw new IllegalArgumentException("Negative value are forbidden");
      }
      return (line, numLine2) -> (numLine2 == numLine) ? Action.DeleteAction.INSTANCE : new Action.PrintAction(line);
   }

   public static Command findAndDelete(Pattern pattern) {
      Objects.requireNonNull(pattern);
      return (line, numLine) -> (pattern.matcher(line).find()) ? Action.DeleteAction.INSTANCE : new Action.PrintAction(line);
   }
}
```

10. Enfin, on peut vouloir combiner plusieurs commandes en ajoutant une méthode andThen à Command tel que le code suivant fonctionne.

```java
public class StreamEditor {
   @FunctionalInterface
   public interface Command {
      Action deleteOrPrint(String line, int numLine);

      default Command andThen(Command command) {
         Objects.requireNonNull(command);
         return (line, numLine) -> {
            return switch (this.deleteOrPrint(line, numLine)) {
               case Action.PrintAction(String text) -> command.deleteOrPrint(text, numLine);
               case Action.DeleteAction delete -> delete;
            };
         };
      }
   }
}
```

11. En conclusion, dans quel cas, à votre avis, va-t-on utiliser des records pour implanter de différentes façons une interface et dans quel cas va-t-on utiliser des lambdas ?

À mon avis, on va utiliser des records dans le cas où on a besoin de stocker une valeur
et utiliser un lambda lorsqu'on veut plutôt stocker un comportement.