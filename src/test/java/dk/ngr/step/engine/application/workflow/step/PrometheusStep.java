package dk.ngr.step.engine.application.workflow.step;

import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.application.workflow.domain.event.PrometheusCalled;
import dk.ngr.step.engine.application.workflow.domain.event.SchedulerWaitProcessed;
import dk.ngr.step.engine.domain.Executor;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.application.client.PrometheusClient;
import dk.ngr.step.engine.domain.event.EventPublisher;
import dk.ngr.step.engine.domain.annotation.Retry;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Retry(maxAttempt = 3)
public class PrometheusStep implements Executor {
  private final SchedulerWaitProcessed event;
  private final PrometheusClient prometheusClient;
  private final String providerType;

  @Override
  public void execute() {

    prometheusClient.smsSended(providerType);

    EventPublisher.<UUID>of().publish(
        new PrometheusCalled(
            event.workflowId,
            event.applicationId,
            EventType.PROMETHEUS_CALLED.ordinal()));
  }

  @Override
  public DomainEvent event() {
    return event;
  }
}