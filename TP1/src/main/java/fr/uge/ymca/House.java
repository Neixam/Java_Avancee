package fr.uge.ymca;

import java.util.*;
import java.util.stream.Collectors;

public final class House {
  private final ArrayList<People> peoples = new ArrayList<>();
  private final HashMap<Kind, Integer> discount = new HashMap<>();

  public void add(People people) {
    peoples.add(Objects.requireNonNull(people));
  }

  private int price(People people) {
    return switch (people) {
      case VillagePeople(String ignored, Kind kind) when discount.containsKey(kind) -> 100 - discount.get(kind);
      case VillagePeople ignored -> 100;
      case Minion ignored -> 1;
    };
  }

  public double averagePrice() {
    return peoples.stream().mapToDouble(this::price).average().orElse(Double.NaN);
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
      case VillagePeople(String ignored, Kind kind) when discount.containsKey(kind) -> discount.get(kind);
      case VillagePeople ignored -> 0;
      case Minion ignored -> 0;
      }, HashMap::new, Collectors.summingInt(this::price)));
  }

  public void removeDiscount(Kind kind) {
    Objects.requireNonNull(kind);
    if (discount.remove(kind) == null) {
      throw new IllegalStateException("No discount for " + kind);
    }
  }

  @Override
  public String toString() {
    if (peoples.isEmpty()) {
      return "Empty House";
    }
    return peoples.stream()
            .map(People::name)
            .sorted()
            .collect(Collectors
                    .joining(", ", "House with ", ""));
  }
}