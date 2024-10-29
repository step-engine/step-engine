package org.step.engine.domain.error;

import org.step.engine.domain.event.DomainEvent;

public class ManualException extends RuntimeException {
  public DomainEvent event;

  public ManualException(String message, Exception exception, DomainEvent event) {
    super(message, exception); this.event = event;
  }

  public ManualException(String message, DomainEvent event) {
    super(message); this.event = event;
  }
}