package dk.ngr.step.engine.application.workflow.domain.event;

import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.event.EventListener;
import java.util.List;
import java.util.UUID;

public class SmsSended extends DomainEvent<UUID> {
  private String callbackId;

  public SmsSended(UUID workflowId, String applicationId, int eventType, String callbackId) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis());
    this.callbackId = callbackId;
  }

  public String callbackId() {
    return callbackId;
  }

  @Override
  public void accept(EventListener eventListener, List<DomainEvent<UUID>> events) {
    eventListener.handle(this, events);
  }
}