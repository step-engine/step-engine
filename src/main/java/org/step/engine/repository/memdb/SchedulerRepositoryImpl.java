package org.step.engine.repository.memdb;

import org.step.engine.domain.model.Scheduler;
import org.step.engine.scheduler.SchedulerStatus;
import org.step.engine.repository.SchedulerRepository;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulerRepositoryImpl implements SchedulerRepository<UUID> {
  public List<Scheduler<UUID>> list = new ArrayList<>();

  @Override
  public void save(Scheduler<UUID> scheduler) {
    list.add(scheduler);
  }

  @Override
  public List<Scheduler<UUID>> findByStatus(SchedulerStatus schedulerStatus) {
    return list.stream().filter(x -> x.getSchedulerStatus() == schedulerStatus).collect(Collectors.toList());
  }

  @Override
  public List<Scheduler<UUID>> findByWorkflowId(UUID workflowId) {
    return list.stream().filter(x -> x.getWorkflowId() == workflowId).collect(Collectors.toList());
  }

  @Override
  public void updateStatus(UUID workflowId, SchedulerStatus schedulerStatus) {
    Optional<Scheduler<UUID>> schedule = list.stream().
            filter(x -> x.getWorkflowId() == workflowId)
            .min(Comparator.comparing(Scheduler::getOccuredOn));
    schedule.ifPresent(x -> x.setSchedulerStatus(schedulerStatus));
  }

  @Override
  public void deleteAll() {
    throw new IllegalArgumentException("Not implemented..");
  }
}