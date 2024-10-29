package org.step.engine.step;

import org.step.engine.domain.annotation.Manual;
import org.step.engine.domain.annotation.Wait;
import org.step.engine.domain.command.CommandStatus;
import org.step.engine.domain.error.ErrorPublisher;
import org.step.engine.domain.error.ManualException;
import org.step.engine.domain.error.RetryException;
import org.step.engine.domain.error.StepException;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.domain.event.WaitEvent;
import org.step.engine.scheduler.SchedulerStatus;
import org.step.engine.domain.annotation.Retry;
import org.step.engine.domain.error.event.StepFailedError;
import org.step.engine.domain.error.event.StepFailedManual;
import org.step.engine.domain.error.event.StepFailedRetry;
import org.step.engine.domain.model.Scheduler;
import org.step.engine.repository.CommandRepository;
import org.step.engine.repository.SchedulerRepository;
import org.step.engine.scheduler.SchedulerType;
import lombok.RequiredArgsConstructor;
import java.lang.annotation.Annotation;
import java.util.Optional;

@RequiredArgsConstructor
public class StepExecutor<T> {
  private final CommandRepository<T> commandRepository;
  private final SchedulerRepository<T> schedulerRespository;
  private final int waitEventType;

  public void execute(Executor<T> executor) {

    Optional<Retry> oRetry = annotation(executor, Retry.class);
    Optional<Manual> oManual = annotation(executor, Manual.class);
    Optional<Wait> oWait = annotation(executor, Wait.class);

    try {

      executor.execute();

      if (oWait.isPresent()) {

        commandRepository.updateStatus(executor.event().workflowId, CommandStatus.WAIT);

        long occurredOn = System.currentTimeMillis();
        Scheduler<T> scheduler = new Scheduler<T>(
                executor.event().workflowId,
                executor.event().applicationId,
                SchedulerType.WAIT,
                oWait.get().eventType(),
                SchedulerStatus.TO_BE_PROCESSED,
                System.currentTimeMillis() + oWait.get().timeoutInMilliseconds(),
                occurredOn);
        schedulerRespository.save(scheduler);

        EventPublisher.of().publish(
                new WaitEvent<>(
                        executor.event().workflowId,
                        executor.event().applicationId,
                        waitEventType,
                        oWait.get().timeoutInMilliseconds(),
                        occurredOn));
      }

    } catch (StepException e) {
      ErrorPublisher.of().publish(
              new StepFailedError<>(
                      (T)e.event.workflowId,
                      e.event.applicationId,
                      e.event.eventType,
                      e));
    } catch (ManualException e) {
      ErrorPublisher.of().publish(
              new StepFailedManual<>(
                      (T)e.event.workflowId,
                      e.event.applicationId,
                      e.event.eventType,
                      e));
    } catch (RetryException e) {
      ErrorPublisher.of().publish(
              new StepFailedRetry<>(
                      (T)e.event.workflowId,
                      e.event.applicationId,
                      e.event.eventType,
                      e.maxAttempt,
                      e));
    } catch (Exception e) {
      if (oManual.isPresent()) {
        ErrorPublisher.of().publish(
                new StepFailedManual<>(
                        executor.event().workflowId,
                        executor.event().applicationId,
                        executor.event().eventType, e));
      } else if (oRetry.isPresent()) {
        ErrorPublisher.of().publish(
                new StepFailedRetry<>(
                        executor.event().workflowId,
                        executor.event().applicationId,
                        executor.event().eventType,
                        oRetry.get().maxAttempt(),
                        e));
      } else {

        // No annotations found ie. put workflow on manual..

        ErrorPublisher.of().publish(
                new StepFailedManual<T>(
                        executor.event().workflowId,
                        executor.event().applicationId,
                        executor.event().eventType, e));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<T> annotation(Executor executor, Class<? extends Annotation> annotationClass) {
    Class<?> executorClass = executor.getClass();
    if (executorClass.isAnnotationPresent(annotationClass)) {
      return (Optional<T>) Optional.of(executorClass.getAnnotation(annotationClass));
    }

    return Optional.empty();
  }
}