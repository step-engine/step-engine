package org.step.engine.domain.error;

import org.step.engine.common.StacktraceUtil;
import org.step.engine.domain.command.CommandStatus;
import org.step.engine.domain.error.event.*;
import org.step.engine.domain.model.Recover;
import org.step.engine.domain.model.Scheduler;
import org.step.engine.domain.model.Stacktrace;
import org.step.engine.repository.CommandRepository;
import org.step.engine.repository.RecoverRepository;
import org.step.engine.repository.SchedulerRepository;
import org.step.engine.repository.StacktraceRepository;
import org.step.engine.scheduler.SchedulerStatus;
import org.step.engine.scheduler.SchedulerType;
import org.step.engine.scheduler.retry.RetryStrategy;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
public class ErrorHandler<T> implements ErrorListener<T> {
  private final RecoverRepository<T> recoverRepository;
  private final StacktraceRepository<T> stacktraceRepository;
  private final SchedulerRepository<T> schedulerRespository;
  private final CommandRepository<T> commandRepository;
  private final RetryStrategy retryStrategy;
  private final int maxAttempt;

  public void handle(StepFailedRetry<T> event) {

    Optional<Recover<T>> optional = recoverRepository.findByWorkflowId(event.workflowId);

    if (optional.isEmpty()) {

      recoverRepository.save(
              new Recover<>(
                      event.workflowId,
                      event.applicationId,
                      event.eventType,
                      RecoverStatus.RETRY,
                      0,
                      "",
                      System.currentTimeMillis()));

      commandRepository.updateStatus(event.workflowId, CommandStatus.RETRY);

      scheduleRetry(event, 0);

    } else {

      Recover<T> recover = optional.get();
      int retry = recover.getRetry() + 1;
      recover.setRetry(retry);
      if (retry < maxAttempt) {
        scheduleRetry(event, retry);
      } else {
        recover.setRecoverStatus(RecoverStatus.MANUAL);
        commandRepository.updateStatus(event.workflowId, CommandStatus.MANUAL);
      }

      recoverRepository.save(recover);
    }

    stacktraceRepository.save(
            new Stacktrace<>(
              event.workflowId,
            event.applicationId,
            StacktraceUtil.getRootCauseFormattedMessage(event.throwable),
            System.currentTimeMillis()));
  }

  public void handle(StepFailedManual<T> event) {
    handle(event, RecoverStatus.MANUAL, CommandStatus.MANUAL);
  }

  public void handle(StepFailedError<T> event) {
    handle(event, RecoverStatus.ERROR, CommandStatus.ERROR);
  }

  public void handle(TimeoutEvent<T> event) {
    commandRepository.updateStatus(event.workflowId, CommandStatus.TIMEOUT);
  }

  private void scheduleRetry(StepFailedRetry<T> event, int retry) {
    Scheduler<T> scheduler = new Scheduler<>(
            event.workflowId,
            event.applicationId,
            SchedulerType.RETRY,
            event.eventType,
            SchedulerStatus.TO_BE_PROCESSED,
            retryStrategy.get(retry, System.currentTimeMillis()),
            System.currentTimeMillis());
    schedulerRespository.save(scheduler);
  }

  private void handle(ErrorEvent<T> event, RecoverStatus recoverStatus, CommandStatus commandStatus) {
    Optional<Recover<T>> optional = recoverRepository.findByApplicationId(event.applicationId);

    if (optional.isEmpty()) {
      recoverRepository.save(
              new Recover<>(
              event.workflowId,
              event.applicationId,
              event.eventType,
              recoverStatus,
              0,
              "",
              System.currentTimeMillis()));
    } else {
      Recover<T> recover = optional.get();
      recoverRepository.updateStatus(recover.getWorkflowId(), recoverStatus);
    }

    stacktraceRepository.save(
            new Stacktrace<>(
            event.workflowId,
            event.applicationId,
            StacktraceUtil.getRootCauseFormattedMessage(event.throwable),
            System.currentTimeMillis()));

    commandRepository.updateStatus(event.workflowId, commandStatus);
  }
}