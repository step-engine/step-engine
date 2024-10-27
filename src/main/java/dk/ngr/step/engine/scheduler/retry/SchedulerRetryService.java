package dk.ngr.step.engine.scheduler.retry;

import dk.ngr.step.engine.domain.error.RecoverStatus;
import dk.ngr.step.engine.domain.model.Recover;
import dk.ngr.step.engine.domain.model.Scheduler;
import dk.ngr.step.engine.repository.RecoverRepository;
import dk.ngr.step.engine.repository.SchedulerRepository;
import dk.ngr.step.engine.scheduler.SchedulerStatus;
import dk.ngr.step.engine.scheduler.SchedulerType;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

/**
 * A service that is used to add entities for retry to the schduler table. The scheduler will start
 * processing the eventually. It is used when in manual mode ie. after analysing the problem try out one retry.
 * If it succeeds then retry all. The service could be exposed through a REST API for example.
 * Note: the actual retry can be seen in dk.ngr.step.engine.scheduler.retry.RetryService.
 */
@RequiredArgsConstructor
public class SchedulerRetryService<T> {
  private final RecoverRepository<T> recoverRepository;
  private final SchedulerRepository<T> schedulerRespository;

  public void retry(T workflowId) {
    scheduleRetry(recoverRepository.findByWorkflowId(workflowId).get());
  }

  public void retryAll() {
    recoverRepository.findByStatus(RecoverStatus.MANUAL).forEach(this::scheduleRetry);
  }

  private void scheduleRetry(Recover<T> recover) {
    long now = System.currentTimeMillis();
    Scheduler<T> scheduler = new Scheduler<T>(
            recover.getWorkflowId(),
            recover.getApplicationId(),
            SchedulerType.RETRY,
            recover.getEventType(),
            SchedulerStatus.TO_BE_PROCESSED,
            now,
            now);
    schedulerRespository.save(scheduler);
  }
}