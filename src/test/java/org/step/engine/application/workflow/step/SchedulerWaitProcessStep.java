package org.step.engine.application.workflow.step;

import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.application.workflow.domain.event.SchedulerWaitProcessed;
import org.step.engine.application.workflow.domain.event.SmsAcknowledged;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.step.Executor;
import org.step.engine.domain.event.DomainEvent;
import org.step.engine.scheduler.SchedulerStatus;
import org.step.engine.domain.annotation.Retry;
import org.step.engine.repository.SchedulerRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Retry(maxAttempt = 3)
public class SchedulerWaitProcessStep implements Executor<UUID> {
  private final SmsAcknowledged event;
  private final SchedulerRepository<UUID> schedulerRespository;
  private final long occuredOn;

  @Override
  public void execute() {

    schedulerRespository.updateStatus(event.workflowId, SchedulerStatus.PROCESSED);

    EventPublisher.<UUID>of().publish(
        new SchedulerWaitProcessed(
            event.workflowId,
            event.applicationId,
            EventType.SCHEDULER_WAIT_PROCESSED.ordinal()));
  }

  @Override
  public DomainEvent<UUID> event() {
    return event;
  }
}