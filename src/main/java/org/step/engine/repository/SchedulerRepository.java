package org.step.engine.repository;

import org.step.engine.domain.model.Scheduler;
import org.step.engine.scheduler.SchedulerStatus;
import java.util.List;

public interface SchedulerRepository<T> {
  void save(Scheduler<T> scheduler);
  List<Scheduler<T>> findByStatus(SchedulerStatus schedulerStatus);
  List<Scheduler<T>> findByWorkflowId(T workflowId);
  void updateStatus(T workflowId, SchedulerStatus schedulerStatus);
  void deleteAll();
}