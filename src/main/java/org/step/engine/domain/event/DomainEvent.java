package org.step.engine.domain.event;

import java.util.List;

public abstract class DomainEvent<T> {
  public T workflowId;
  public String applicationId;
  public int eventType;
  public long occurredOn;

  public DomainEvent() {}

  public DomainEvent(
      T workflowId,
      String applicationId,
      int eventType,
      long occurredOn) {
    this.workflowId = workflowId;
    this. applicationId = applicationId;
    this.eventType = eventType;
    this.occurredOn = occurredOn;
  }

  public abstract void accept(EventListener eventListener, List<DomainEvent<T>> events);
}
