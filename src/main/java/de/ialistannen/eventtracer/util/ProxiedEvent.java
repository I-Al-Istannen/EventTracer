package de.ialistannen.eventtracer.util;

import de.ialistannen.eventtracer.audit.AuditableAction;
import java.util.List;

/**
 * An interface indicating an {@link org.bukkit.event.Event} that has audit information.
 */
public interface ProxiedEvent {

  /**
   * Returns all actions that were applies to this poor event.
   *
   * @return all actions that were applied to the event
   */
  List<AuditableAction> getActions();
}
