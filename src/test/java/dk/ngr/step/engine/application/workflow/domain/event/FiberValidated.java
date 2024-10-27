package dk.ngr.step.engine.application.workflow.domain.event;

import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.event.EventListener;
import java.util.List;
import java.util.UUID;

public class FiberValidated extends DomainEvent<UUID> {
  private String providerType;

  public FiberValidated(UUID workflowId, String applicationId, int eventType, String providerType) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis());
    this.providerType = providerType;
  }

  public String providerType() {
    return providerType;
  }

  @Override
  public void accept(EventListener eventListener, List<DomainEvent<UUID>> events) {
    eventListener.handle(this, events);
  }
}