package fr.uge.sed;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

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
  private sealed interface Action {
    enum DeleteAction implements Action {
      INSTANCE
    }
    record PrintAction(String text) implements Action {}
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
        case Action.PrintAction(String text) -> writer.append(text).append('\n');
        case Action.DeleteAction ignored -> {}
      }
    }
  }

  public static Command substitute(Pattern pattern, String remplacement) {
    Objects.requireNonNull(pattern);
    Objects.requireNonNull(remplacement);
    return (line, numLine) -> new Action.PrintAction(pattern.matcher(line).replaceAll(remplacement));
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
