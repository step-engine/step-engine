package dk.ngr.step.engine.application.workflow.step;

import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.application.workflow.domain.ProviderType;
import dk.ngr.step.engine.application.workflow.domain.event.CommandIgnored;
import dk.ngr.step.engine.application.workflow.domain.event.CommandReceived;
import dk.ngr.step.engine.application.workflow.domain.event.FiberValidated;
import dk.ngr.step.engine.domain.event.EventPublisher;
import dk.ngr.step.engine.domain.Executor;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.application.client.ChubClient;
import dk.ngr.step.engine.application.util.ProductValidator;
import dk.ngr.step.engine.domain.annotation.Retry;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Retry(maxAttempt = 3)
public class FiberValidatorStep implements Executor<UUID> {
  private final CommandReceived event;
  private final ProductValidator validator;
  private final ChubClient client;

  @Override
  public void execute() {

    String productNumber = client.getProductNumber(event.applicationId);

    if ( ! validator.validate(productNumber)) {
      EventPublisher.<UUID>of().publish(
          new CommandIgnored(
              event.workflowId,
              event.applicationId,
              EventType.COMMAND_IGNORED.ordinal()));
    } else {
      EventPublisher.<UUID>of().publish(
          new FiberValidated(
              event.workflowId,
              event.applicationId,
              EventType.FIBER_VALIDATED.ordinal(),
              ProviderType.ENIIG_FIBER.toString()));
    }
  }

  @Override
  public DomainEvent event() {
    return event;
  }
}