package org.step.engine.scheduler;

import org.step.engine.domain.model.Scheduler;
import org.step.engine.repository.SchedulerRepository;
import org.step.engine.domain.error.ErrorPublisher;
import org.step.engine.domain.error.event.TimeoutEvent;
import org.step.engine.scheduler.retry.RetryService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchedulerProcessor<T> implements Processor {
  private final SchedulerRepository<T> schedulerRespository;
  private final RetryService<T> retryService;
  private volatile long retry = 0;
  private volatile long waitTimeout = 0;

  @Override
  public void process() {
    schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).forEach(this::handle);
  }

  private void handle(Scheduler<T> scheduler) {
    switch (scheduler.getSchedulerType()) {
      case RETRY:
        retry(scheduler);
        break;
      case WAIT:
        waitTimeout(scheduler);
        break;
      default:
        // TODO: put on manual
    }
  }

  private void retry(Scheduler<T> scheduler) {
    if (scheduler.getExecuteAt() <= System.currentTimeMillis()) {
      retryService.retry(scheduler.getWorkflowId());
      schedulerRespository.updateStatus(scheduler.getWorkflowId(), SchedulerStatus.PROCESSED);
      retry++;
    }
  }

  private void waitTimeout(Scheduler<T> scheduler) {
    if (scheduler.getExecuteAt() <= System.currentTimeMillis()) {
      schedulerRespository.updateStatus(scheduler.getWorkflowId(), SchedulerStatus.PROCESSED);
      ErrorPublisher.of().publish(
              new TimeoutEvent<T>(
                      scheduler.getWorkflowId(),
                      scheduler.getApplicationId(),
                      scheduler.getEventType()));
      waitTimeout++;
    }
  }

  @Override
  public long retry() {
    return retry;
  }

  @Override
  public long waitTimeout() {
    return waitTimeout;
  }
}