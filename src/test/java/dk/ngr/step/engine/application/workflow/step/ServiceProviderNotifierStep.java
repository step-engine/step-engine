package dk.ngr.step.engine.application.workflow.step;

import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.application.workflow.domain.event.PrometheusCalled;
import dk.ngr.step.engine.application.workflow.domain.event.ServiceProviderNotified;
import dk.ngr.step.engine.domain.event.EventPublisher;
import dk.ngr.step.engine.domain.Executor;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.application.client.ServiceProviderNotifierClient;
import dk.ngr.step.engine.domain.annotation.Retry;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * Just a step made to stress the importannce of knowing that other systems have been called.
 *
 */
@RequiredArgsConstructor
@Retry(maxAttempt = 3)
public class ServiceProviderNotifierStep implements Executor {
  private final PrometheusCalled event;
  private final ServiceProviderNotifierClient serviceProviderNotifierClient;

  @Override
  public void execute() {

    serviceProviderNotifierClient.notify(event.applicationId);

    EventPublisher.<UUID>of().publish(
        new ServiceProviderNotified(
            event.workflowId,
            event.applicationId,
            EventType.SERVICE_PROVIDER_NOTIFIED.ordinal()));
  }

  @Override
  public DomainEvent event() {
    return event;
  }
}