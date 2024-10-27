package dk.ngr.step.engine.application.workflow.listener;

import dk.ngr.step.engine.application.workflow.domain.event.CommandReceived;
import dk.ngr.step.engine.application.workflow.domain.event.FiberValidated;
import dk.ngr.step.engine.domain.event.MethodInvoker;
import dk.ngr.step.engine.domain.event.EventListener;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.event.WaitEvent;
import dk.ngr.step.engine.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

/**
 * Use the Aggreate class to mutate state from either memory or event store.
 * Note that only history of events needed in the Workflow needs to be mutated.
 * Also note that the invoker does not fail because of non-existing event handlers.
 */

@RequiredArgsConstructor
public class Aggregate implements EventListener {
  private final EventRepository<UUID> eventRepository;
  private final MethodInvoker methodInvoker;

  public CommandReceived commandReceived;
  public FiberValidated fiberValidated;
  public WaitEvent<UUID> waitEvent;


  public void mutateFromMemory(List<DomainEvent<UUID>> events) {
    for (DomainEvent<UUID> event : events) {
      event.accept(this, events);
    }
  }

  public void mutateFromEventstore(UUID workflowId) {
    List<DomainEvent<UUID>> events = eventRepository.read(workflowId);
    for (DomainEvent<UUID> event : events) {

        event.accept(this, events);
    }
  }

  public void handle(CommandReceived event, List<DomainEvent<?>> events) {
    this.commandReceived = event;
  }

  public void handle(FiberValidated event, List<DomainEvent<?>> events) {
    this.fiberValidated = event;
  }

  public void handle(WaitEvent<UUID> event, List<DomainEvent<?>> events) {
    this.waitEvent = event;
  }

  @Override
  public <UUID> void handle(DomainEvent<UUID> event, List<? extends DomainEvent<UUID>> events) {
    methodInvoker.invoke(this, event, events);
  }
}