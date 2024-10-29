package org.step.engine.repository.memdb;

import org.step.engine.domain.model.Stacktrace;
import org.step.engine.repository.StacktraceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StacktraceRepositoryImpl implements StacktraceRepository<UUID> {
  private List<Stacktrace<UUID>> list = new ArrayList<>();

  @Override
  public void save(Stacktrace<UUID> stacktrace) {
    list.add(stacktrace);
  }

  @Override
  public List<Stacktrace<UUID>> findByWorkflowId(UUID workflowId) {
    return list.stream().filter(x -> x.getWorkflowId()==workflowId).collect(Collectors.toList());
  }

  @Override
  public void deleteAll() {
    throw new IllegalArgumentException("Not implemented..");
  }
}