package fr.uge.ymca;

import java.util.Objects;

public record VillagePeople(String name, Kind kind) implements People {
  public VillagePeople {
    Objects.requireNonNull(name);
    Objects.requireNonNull(kind);
  }

  @Override
  public String toString() {
    return name + " (" + kind + ")";
  }

  public static void main(String[] args) {
    var lee = new VillagePeople("Lee", Kind.BIKER);
    System.out.println(lee);  // Lee (BIKER)
  }
}