package dk.ngr.step.engine.repository;

import dk.ngr.step.engine.domain.event.DomainEvent;
import java.util.List;

public interface EventRepository<T> {
  void write(DomainEvent<T> domainEvent);
  void batch(List<DomainEvent<Long>> events);
  List<DomainEvent<T>> read(T workflowId);
  List<DomainEvent<T>> read(String applicationId);

  void deleteAll();
}
