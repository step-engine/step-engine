package org.step.engine.application.workflow.step;

import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.application.workflow.domain.event.FiberValidated;
import org.step.engine.application.workflow.domain.event.SmsSended;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.step.Executor;
import org.step.engine.domain.annotation.Wait;
import org.step.engine.domain.event.DomainEvent;
import org.step.engine.application.client.CallbackRepository;
import org.step.engine.application.client.SmsClient;
import org.step.engine.domain.annotation.Retry;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
@Retry(maxAttempt = 3)
@Wait(timeoutInMilliseconds = 1, eventType = 3)
public class SmsStep implements Executor {
  private final FiberValidated event;
  private final SmsClient client;
  private final CallbackRepository callbackRepository;
  private final String mobileNumber;

  @Override
  public void execute() {

    String callbackId = client.send(mobileNumber, "Dear customer.. welcome to TDC.");

    callbackRepository.save(callbackId, event.workflowId);

    EventPublisher.<UUID>of().publish(
        new SmsSended(
            event.workflowId,
            event.applicationId,
            EventType.SMS_SENDED.ordinal(),
            callbackId));
  }

  @Override
  public DomainEvent event() {
    return event;
  }
}