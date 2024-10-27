package dk.ngr.step.engine.domain.event;

import java.util.List;

public abstract class DomainEvent<T> {
  public T workflowId;
  public String applicationId;
  public int eventType;
  public long occuredOn;

  public DomainEvent() {}

  public DomainEvent(
          T workflowId,
          String applicationId,
          int eventType,
          long occuredOn) {
    this.workflowId = workflowId;
    this. applicationId = applicationId;
    this.eventType = eventType;
    this.occuredOn = occuredOn;
  }

  public abstract void accept(EventListener eventListener, List<DomainEvent<T>> events);
}