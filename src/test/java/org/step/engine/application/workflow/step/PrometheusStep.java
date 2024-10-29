package org.step.engine.application.workflow.step;

import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.application.workflow.domain.event.PrometheusCalled;
import org.step.engine.application.workflow.domain.event.SchedulerWaitProcessed;
import org.step.engine.step.Executor;
import org.step.engine.domain.event.DomainEvent;
import org.step.engine.application.client.PrometheusClient;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.domain.annotation.Retry;
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