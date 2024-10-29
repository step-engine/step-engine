package org.step.engine.application.workflow.domain.event;

import org.step.engine.domain.event.DomainEvent;
import org.step.engine.domain.event.EventListener;
import java.util.List;
import java.util.UUID;

public class ServiceProviderNotified extends DomainEvent<UUID> {

  public ServiceProviderNotified(UUID workflowId, String applicationId, int eventType) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis());
  }

  @Override
  public void accept(EventListener eventListener, List<DomainEvent<UUID>> events) {
    eventListener.handle(this, events);
  }
}
