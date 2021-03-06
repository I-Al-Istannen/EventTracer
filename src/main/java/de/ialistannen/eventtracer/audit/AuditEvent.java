package de.ialistannen.eventtracer.audit;

import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after a event was processed containing the event history.
 */
public abstract class AuditEvent extends Event {

  private static final HandlerList handlerList = new HandlerList();

  private final List<AuditableAction> actions;
  private final Event sourceEvent;

  /**
   * Creates a new audit event.
   *
   * @param actions the actions
   * @param sourceEvent the source event
   * @param async true if the event is async
   */
  public AuditEvent(List<AuditableAction> actions, Event sourceEvent, boolean async) {
    super(async);
    this.actions = actions;
    this.sourceEvent = sourceEvent;
  }

  public List<AuditableAction> getActions() {
    return actions;
  }

  public Event getSourceEvent() {
    return sourceEvent;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }
}
