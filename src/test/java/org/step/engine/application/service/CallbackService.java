package org.step.engine.application.service;

import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.application.workflow.domain.event.SmsAcknowledged;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.application.client.CallbackRepository;
import org.step.engine.application.config.Constant;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@RequiredArgsConstructor
public class CallbackService {
  private final CallbackRepository callbackRepository;
  public void onSmsAcknowledged(String callbackId) {
    try {
      UUID workflowId = callbackRepository.findWorkflowIdByCallbackId(callbackId);
      EventPublisher.<UUID>of().publish(
          new SmsAcknowledged(
              workflowId,
              Constant.orderNumber,
              EventType.SMS_ACKNOWLEDGED.ordinal(),
              callbackId));
    } finally {
      // Reset history of events in memory
      EventPublisher.of().resetStateOfId(Constant.orderNumber);
    }
  }
}
