package org.step.engine.domain.event;

import java.util.List;

public class WaitEvent<T> extends DomainEvent<T> {
  private final long timeoutInMilliseconds;

  public WaitEvent(T workflowId, String applicationId, int eventType, long timeoutInMilliseconds, long occurredOn) {
    super(workflowId, applicationId, eventType, occurredOn);
    this.timeoutInMilliseconds = timeoutInMilliseconds;
  }

  public long timeoutInMilliseconds() {
    return timeoutInMilliseconds;
  }

  @Override
  public void accept(EventListener eventListener, List<DomainEvent<T>> events) {
    eventListener.handle(this, events);
  }
}