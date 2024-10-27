package dk.ngr.step.engine.domain.event;

import java.util.List;

public interface EventListener {
  <T> void handle(DomainEvent<T> event, List<? extends DomainEvent<T>> events);
}

