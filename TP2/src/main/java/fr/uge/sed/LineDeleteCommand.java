package fr.uge.sed;

public record LineDeleteCommand(int numLine) {
  public LineDeleteCommand {
    if (numLine < 0) {
      throw new IllegalArgumentException("Negative line impossible");
    }
  }
}
