package org.step.engine.step;

import org.step.engine.domain.event.DomainEvent;

public interface Executor<T> {
  void execute();
  DomainEvent<T> event();
}
