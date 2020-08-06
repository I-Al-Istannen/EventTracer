package de.ialistannen.eventtracer.audit;

import java.util.List;
import org.bukkit.event.Event;

/**
 * An async audit event.
 */
public class AsyncAuditEvent extends AuditEvent {

  /**
   * Creates a new audit event.
   *
   * @param actions the actions
   * @param sourceEvent the source event
   */
  public AsyncAuditEvent(List<AuditableAction> actions, Event sourceEvent) {
    super(actions, sourceEvent, true);
  }
}
