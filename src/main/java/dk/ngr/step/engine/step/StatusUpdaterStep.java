package dk.ngr.step.engine.step;

import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.event.EventPublisher;
import dk.ngr.step.engine.domain.Executor;
import dk.ngr.step.engine.domain.error.RecoverStatus;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.event.StatusUpdated;
import dk.ngr.step.engine.domain.model.Recover;
import dk.ngr.step.engine.domain.annotation.Retry;
import dk.ngr.step.engine.repository.CommandRepository;
import dk.ngr.step.engine.repository.RecoverRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

/**
 * In case the workflow has been in recover there will be a recover entity. The command entity
 * should always be there.. if not.. it is an error.
 *
 * Note
 * If refactoring the step engine schema as proposed in the readme then the update mechanism could
 * be made in a StatusUpdater event listener leaving the StatusUpdaterStep out.
 */
@RequiredArgsConstructor
@Retry(maxAttempt = 3)
public class StatusUpdaterStep<T> implements Executor<T> {
  private final DomainEvent<T> event;
  private final RecoverRepository<T> recoverRepository;
  private final CommandRepository<T> commandRepository;
  private final CommandStatus commandStatus;
  private final int eventType;

  @Override
  public void execute() {
    Optional<Recover<T>> oRecover = recoverRepository.findByWorkflowId(event.workflowId);
    oRecover.ifPresent(recover -> recoverRepository.updateStatus(recover.getWorkflowId(), RecoverStatus.PROCESSED));
    commandRepository.updateStatus(event.workflowId, commandStatus);

    EventPublisher.<T>of().publish(
        new StatusUpdated<T>(
            event.workflowId,
            event.applicationId,
            eventType));
  }

  @Override
  public DomainEvent<T> event() {
    return event;
  }
}