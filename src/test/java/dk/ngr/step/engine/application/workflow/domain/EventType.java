package dk.ngr.step.engine.application.workflow.domain;

public enum EventType {
  COMMAND_RECEIVED("CommandReceived"),
  FIBER_VALIDATED("FiberValidated"),
  SMS_SENDED("SmsSended"),
  WAIT_EVENT("WaitEvent"),
  SMS_ACKNOWLEDGED("SmsAcknowledged"),
  SCHEDULER_WAIT_PROCESSED("SchedulerWaitProcessed"),
  PROMETHEUS_CALLED("PrometheusCalled"),
  SERVICE_PROVIDER_NOTIFIED("ServiceProviderNotified"),
  STATUS_UPDATED("StatusUpdated"),
  COMMAND_IGNORED("CommandIgnored");

  private String type;

  EventType(String type) {
    this.type = type;
  }

  public String getName() {
    return this.type;
  }
}