package dk.ngr.step.engine.application.config;

import dk.ngr.step.engine.application.client.*;
import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.domain.event.MethodInvoker;
import dk.ngr.step.engine.domain.event.EventPublisher;
import dk.ngr.step.engine.domain.StepExecutor;
import dk.ngr.step.engine.repository.*;
import dk.ngr.step.engine.scheduler.retry.RetryStrategy;
import dk.ngr.step.engine.scheduler.retry.SchedulerRetryService;
import dk.ngr.step.engine.application.util.ProductValidator;
import dk.ngr.step.engine.application.workflow.listener.Aggregate;
import dk.ngr.step.engine.application.workflow.listener.Eventstore;
import dk.ngr.step.engine.application.workflow.listener.Workflow;
import dk.ngr.step.engine.domain.error.ErrorHandler;
import dk.ngr.step.engine.domain.error.ErrorPublisher;
import lombok.RequiredArgsConstructor;

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