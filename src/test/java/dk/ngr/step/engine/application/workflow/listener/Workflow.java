package dk.ngr.step.engine.application.workflow.listener;

import dk.ngr.step.engine.application.util.ProductValidator;
import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.domain.event.MethodInvoker;
import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.event.EventListener;
import dk.ngr.step.engine.domain.StepExecutor;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.repository.CommandRepository;
import dk.ngr.step.engine.repository.RecoverRepository;
import dk.ngr.step.engine.repository.SchedulerRepository;
import dk.ngr.step.engine.step.StatusUpdaterStep;
import dk.ngr.step.engine.application.client.*;
import dk.ngr.step.engine.application.workflow.domain.event.*;
import dk.ngr.step.engine.application.workflow.step.*;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class Workflow implements EventListener {
  private StepExecutor<UUID> stepExecutor;
  private Aggregate aggregate;
  private ProductValidator productValidator;
  private ChubClient chubClient;
  private SmsClient smsClient;
  private PrometheusClient prometheusClient;
  private ServiceProviderNotifierClient serviceProviderNotifierClient;
  private RecoverRepository<UUID> recoverRepository;
  private CommandRepository<UUID> commandRepository;
  private CallbackRepository callbackRepository;
  private SchedulerRepository<UUID> schedulerRespository;
  private MethodInvoker methodInvoker;


  public void handle(CommandReceived event, List<DomainEvent<UUID>> events) {
    stepExecutor.execute(new FiberValidatorStep(event, productValidator, chubClient));
  }

  /**
   * Note that mobileNumber is fetched from a previous event (CommandReceived) using the aggregate.
   * Data could be put in future events to avoid mutating but that could lead into building up events
   * with redundant data which is unnecessary. Also it will take up space in the event store and minimize
   * load time.
   */
  public void handle(FiberValidated event, List<DomainEvent<UUID>> events) {
    aggregate.mutateFromMemory(events);
    stepExecutor.execute(new SmsStep(event, smsClient, callbackRepository, aggregate.commandReceived.mobileNumber()));
  }

  /**
   * The workflow does not implement this handler since "wait" is used ie. the workflow
   * will only continue processing when SmsAcknowledged has been published by
   * some receiver (either from topic or REST API).
   * <p>
   * Recommended
   * Create a callback micro service that exposes a REST API that 3rd party can call.
   * Put the event on a callback topic that is consumed by this application and then publish
   * the SmsAcknowledged in that listener.
   */
  public void handle(SmsSended event, List<DomainEvent<UUID>> events) {
  }

  /**
   * Note that events must be mutated from the eventstore.. not memory.. since workflow processing stopped
   * after publishing the SmsSended event. The only event in history now is the SmsAcknowledged event because of
   * a callback message.
   */
  public void handle(SmsAcknowledged event, List<DomainEvent<UUID>> events) {
    aggregate.mutateFromEventstore(event.workflowId);
    stepExecutor.execute(new SchedulerWaitProcessStep(event, schedulerRespository, aggregate.waitEvent.occuredOn));
  }

  /**
   * Note that the prometheus call could be handled in a PrometheusListener but it will be a problem for
   * the workflow if this call fails. The exception will be caught in the step which will go on retry even
   * though a previous call in the step worked ie. make it a step.
   * <p>
   * In other words only make calls in event listeners that are Cassandra calls and given the situation can be
   * handled in the db eg. the StatupsUpdater logic can be handled by updating db entities by hand.
   * <p>
   * See note above about mutateFromEventstore vs mutateFromMemory.
   */
  public void handle(SchedulerWaitProcessed event, List<DomainEvent<UUID>> events) {
    aggregate.mutateFromEventstore(event.workflowId);
    stepExecutor.execute(new PrometheusStep(event, prometheusClient, aggregate.fiberValidated.providerType()));
  }

  public void handle(PrometheusCalled event, List<DomainEvent<UUID>> events) {
    stepExecutor.execute(new ServiceProviderNotifierStep(event, serviceProviderNotifierClient));
  }

  /**
   * If refactoring the step engine schema as described in the readme.. the status update
   * functionality can be made as an event listener. For now.. make it as a step.
   */
  public void handle(ServiceProviderNotified event, List<DomainEvent<UUID>> events) {
    stepExecutor.execute(new StatusUpdaterStep(event, recoverRepository, commandRepository, CommandStatus.PROCESSED, EventType.STATUS_UPDATED.ordinal()));
  }

  /**
   * The command and recover status also have to be updated in case of the CommandIgnored (product didn't
   * validate in FiberValidatorStep). Also note the comment above regarding making the StatusUpdater
   * as an EventListener.
   */
  public void handle(CommandIgnored event, List<DomainEvent<UUID>> events) {
    stepExecutor.execute(new StatusUpdaterStep(event, recoverRepository, commandRepository, CommandStatus.IGNORED, EventType.STATUS_UPDATED.ordinal()));
  }

  public void setServiceProviderNotifierClient(ServiceProviderNotifierClient serviceProviderNotifierClient) {
    this.serviceProviderNotifierClient = serviceProviderNotifierClient;
  }

  @Override
  public <UUID> void handle(DomainEvent<UUID> event, List<? extends DomainEvent<UUID>> events) {
    methodInvoker.invoke(this, event, events);
  }
}