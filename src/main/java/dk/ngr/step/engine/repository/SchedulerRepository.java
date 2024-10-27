package dk.ngr.step.engine.repository;

import dk.ngr.step.engine.domain.model.Scheduler;
import dk.ngr.step.engine.scheduler.SchedulerStatus;
import java.util.List;

public interface SchedulerRepository<T> {
  void save(Scheduler<T> scheduler);
  List<Scheduler<T>> findByStatus(SchedulerStatus schedulerStatus);
  List<Scheduler<T>> findByWorkflowId(T workflowId);
  void updateStatus(T workflowId, SchedulerStatus schedulerStatus);
  void deleteAll();
}