package fr.uge.ymca;

public sealed interface People permits Minion, VillagePeople {
  String name();
}