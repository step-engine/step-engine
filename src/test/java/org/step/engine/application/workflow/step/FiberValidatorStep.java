package org.step.engine.application.workflow.step;

import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.application.workflow.domain.ProviderType;
import org.step.engine.application.workflow.domain.event.CommandIgnored;
import org.step.engine.application.workflow.domain.event.CommandReceived;
import org.step.engine.application.workflow.domain.event.FiberValidated;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.step.Executor;
import org.step.engine.domain.event.DomainEvent;
import org.step.engine.application.client.ChubClient;
import org.step.engine.application.util.ProductValidator;
import org.step.engine.domain.annotation.Retry;
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