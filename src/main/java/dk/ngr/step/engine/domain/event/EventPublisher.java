package dk.ngr.step.engine.domain.event;

import dk.ngr.step.engine.domain.error.WorkflowException;
import java.util.*;

public class EventPublisher<T> {
  private static EventPublisher instance;
  private List<EventListener> listeners;
  private Map<T, List<DomainEvent<T>>> states;

  private EventPublisher() { this.initialize(); }

  public static <T> EventPublisher<T> of() {
    if (instance == null) {
      instance = new EventPublisher<T>();
    }

    return instance;
  }

  public void publish(final DomainEvent<T> event) {
    if (hasListeners()) {
      append(event.workflowId, event);
      for (EventListener listener : listeners) {
        event.accept(listener, states.get(event.workflowId));
      }
    }
  }

  public void publish(Collection<DomainEvent<T>> domainEvents) {
    for (DomainEvent<T> domainEvent : domainEvents) {
      publish(domainEvent);
    }
  }

  public EventPublisher<T> add(EventListener eventListener) {
    initialize();
    listeners.add(eventListener);
    return this;
  }

  public void clear() {
    listeners.clear();
    states.clear();
  }

  public void resetStateOfId(T workflowId) {
    if (Objects.nonNull(workflowId))
      states.remove(workflowId);
  }

  public void setState(T workflowId, List<DomainEvent<T>> events) {
    if (states.containsKey(workflowId))
      throw new WorkflowException(String.format("Failed to mutate since states are not removed. wid=%s", workflowId.toString()));
    events.forEach(event -> append(workflowId, event));
  }

  private void append(T workflowId, DomainEvent<T> domainEvent) {
    if (states.containsKey(workflowId)) {
      List<DomainEvent<T>> domainEvents = states.get(workflowId);
      domainEvents.add(domainEvent);
    } else {
      List<DomainEvent<T>> events = new ArrayList<>();
      events.add(domainEvent);
      states.put(workflowId, events);
    }
  }

  private void initialize() {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }

    if (states == null) {
      states = new HashMap<>();
    }
  }

  private boolean hasListeners() {
    return listeners != null && listeners.size() != 0;
  }
}