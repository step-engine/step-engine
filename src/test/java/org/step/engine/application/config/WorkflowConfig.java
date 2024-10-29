package org.step.engine.application.config;

import org.step.engine.application.client.*;
import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.domain.event.MethodInvoker;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.step.StepExecutor;
import org.step.engine.scheduler.retry.RetryStrategy;
import org.step.engine.scheduler.retry.SchedulerRetryService;
import org.step.engine.application.util.ProductValidator;
import org.step.engine.application.workflow.listener.Aggregate;
import org.step.engine.application.workflow.listener.Eventstore;
import org.step.engine.application.workflow.listener.Workflow;
import org.step.engine.domain.error.ErrorHandler;
import org.step.engine.domain.error.ErrorPublisher;
import lombok.RequiredArgsConstructor;
import org.step.engine.repository.*;

import java.util.UUID;

@RequiredArgsConstructor
public class WorkflowConfig {
  private final EventRepository<UUID> eventRepository;
  private final CommandRepository<UUID> commandRepository;
  private final RecoverRepository<UUID> recoverRepository;
  private final StacktraceRepository<UUID> stacktraceRepository;
  private final SchedulerRepository<UUID> schedulerRespository;
  private final CallbackRepository callbackRepository;
  private final ProductValidator productValidator;
  private final ChubClient chubClient;
  private final SmsClient smsClient;
  private final PrometheusClient prometheusClient;
  private final ServiceProviderNotifierClient serviceProviderNotifierClient;

  private SchedulerRetryService<UUID> schedulerRetryService;

  public long retryAfterMilliseconds;
  public int maxAttempt;
  public Workflow workflow;


  public void setupNotCaringAboutRetry() {
    this.retryAfterMilliseconds = 1000000000;
    this.maxAttempt = 1000;
    setup();
  }

  public void setupForRetryInUnitTest() {
    this.retryAfterMilliseconds = 0;
    this.maxAttempt = 3;
    setup();
  }

  private void setup() {

    schedulerRetryService = new SchedulerRetryService<>(recoverRepository, schedulerRespository);

    MethodInvoker methodInvoker = new MethodInvoker();

    workflow = new Workflow(
        new StepExecutor<>(commandRepository, schedulerRespository, EventType.WAIT_EVENT.ordinal()),
        new Aggregate(eventRepository, methodInvoker),
        productValidator,
        chubClient,
        smsClient,
        prometheusClient,
        serviceProviderNotifierClient,
        recoverRepository,
        commandRepository,
        callbackRepository,
        schedulerRespository,
        methodInvoker);

    EventPublisher.of()
        .add(new Eventstore(eventRepository))
        .add(workflow);

    ErrorPublisher.of()
        .add(new ErrorHandler<>(
            recoverRepository,
            stacktraceRepository,
            schedulerRespository,
            commandRepository,
            new RetryStrategy() {
              @Override
              public long get(int i, long now) {
                return now + retryAfterMilliseconds;
              }
            },
            maxAttempt));
  }
}