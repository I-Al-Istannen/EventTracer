package de.ialistannen.eventtracer.audit;

import java.util.List;
import org.bukkit.event.Event;

/**
 * A sync audit event...
 */
public class SyncAuditEvent extends AuditEvent {

  /**
   * Creates a new audit event.
   *
   * @param actions the actions
   * @param sourceEvent the source event
   */
  public SyncAuditEvent(List<AuditableAction> actions, Event sourceEvent) {
    super(actions, sourceEvent, false);
  }
}
