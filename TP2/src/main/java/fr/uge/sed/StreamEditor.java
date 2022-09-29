package fr.uge.sed;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
      if (lineNumberReader.getLineNumber() != lineDeleteCommand.numLine()) {
        writer.append(line).append('\n');
      }
    }
  }

  public static LineDeleteCommand lineDelete(int numLine) {
    return new LineDeleteCommand(numLine);
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
