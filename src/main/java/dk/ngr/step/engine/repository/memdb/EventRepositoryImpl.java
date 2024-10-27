package dk.ngr.step.engine.repository.memdb;

import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.repository.EventRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventRepositoryImpl implements EventRepository<UUID> {
  private List<DomainEvent<UUID>> list = new ArrayList<>();

  @Override
  public void write(DomainEvent<UUID> domainEvent) {
    list.add(domainEvent);
  }

  @Override
  public void batch(List<DomainEvent<Long>> events) {
    throw new IllegalArgumentException("Not implemented..");
  }

  @Override
  public List<DomainEvent<UUID>> read(UUID wid) {
    return list.stream().filter(x -> wid.compareTo(x.workflowId) == 0).collect(Collectors.toList());
  }

  @Override
  public List<DomainEvent<UUID>> read(String applicationId) {
    return list.stream().filter(x -> applicationId.equalsIgnoreCase(x.applicationId)).collect(Collectors.toList());
  }

  @Override
  public void deleteAll() {
    throw new IllegalArgumentException("Not implemented..");
  }
}