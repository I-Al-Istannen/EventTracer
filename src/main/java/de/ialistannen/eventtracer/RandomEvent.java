package de.ialistannen.eventtracer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RandomEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  public static HandlerList getHandlerList() {
    return handlers;
  }

  private int number;

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
