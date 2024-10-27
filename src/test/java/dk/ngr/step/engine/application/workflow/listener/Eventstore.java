package dk.ngr.step.engine.application.workflow.listener;

import dk.ngr.step.engine.domain.event.MethodInvoker;
import dk.ngr.step.engine.domain.event.EventListener;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.event.StatusUpdated;
import dk.ngr.step.engine.domain.event.WaitEvent;
import dk.ngr.step.engine.repository.EventRepository;
import dk.ngr.step.engine.application.workflow.domain.event.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class Eventstore implements EventListener {
  private final EventRepository<UUID> eventRepository;
  private MethodInvoker methodInvoker;

  public void handle(CommandReceived event, List<DomainEvent<UUID>> events) { append(event); }
  public void handle(FiberValidated event, List<DomainEvent<UUID>> events) { append(event); }
  public void handle(CommandIgnored event, List<DomainEvent<UUID>> events) {
    append(event);
  }
  public void handle(SmsSended event, List<DomainEvent<UUID>> events) {
    append(event);
  }
  public void handle(SmsAcknowledged event, List<DomainEvent<UUID>> events) {
    append(event);
  }
  public void handle(WaitEvent<UUID> event, List<DomainEvent<UUID>> events) {
    append(event);
  }
  public void handle(PrometheusCalled event, List<DomainEvent<UUID>> events) {
    append(event);
  }
  public void handle(ServiceProviderNotified event, List<DomainEvent<UUID>> events) {
    append(event);
  }
  public void handle(StatusUpdated<UUID> event, List<DomainEvent<UUID>> events) {
    append(event);
  }

  private void append(DomainEvent<UUID> event) {
    eventRepository.write(event);
  }

  @Override
  public <UUID> void handle(DomainEvent<UUID> event, List<? extends DomainEvent<UUID>> events) {
    methodInvoker.invoke(this, event, events);
  }
}