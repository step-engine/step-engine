package dk.ngr.step.engine.application.workflow.domain.event;

import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.event.EventListener;
import java.util.List;
import java.util.UUID;

public class CommandReceived extends DomainEvent<UUID> {
  private String mobileNumber;

  public CommandReceived(UUID workflowId, String applicationId, int eventType, String mobileNumber) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis());
    this.mobileNumber = mobileNumber;
  }

  public String mobileNumber() {
    return mobileNumber;
  }

  @Override
  public void accept(EventListener eventListener, List<DomainEvent<UUID>> events) {
    eventListener.handle(this, events);
  }
}