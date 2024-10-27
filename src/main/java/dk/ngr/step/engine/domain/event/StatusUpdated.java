package dk.ngr.step.engine.domain.event;

import java.util.List;

public class StatusUpdated<T> extends DomainEvent<T> {
  public StatusUpdated(T workflowId, String applicationId, int eventType) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis());
  }

  @Override
  public void accept(EventListener eventListener, List<DomainEvent<T>> events) {
    eventListener.handle(this, events);
  }
}
