package dk.ngr.step.engine.domain;

import dk.ngr.step.engine.domain.event.DomainEvent;

public interface Executor<T> {
  void execute();
  DomainEvent<T> event();
}
