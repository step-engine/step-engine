package dk.ngr.step.engine.domain.model;

import dk.ngr.step.engine.domain.error.RecoverStatus;

public class Recover<T> {
  private T workflowId;
  private String applicationId;
  private int eventType;
  private RecoverStatus recoverStatus;
  private int retry;
  private String note;
  private long occuredOn;

  public Recover(
          T workflowId,
          String applicationId,
          int eventType,
          RecoverStatus recoverStatus,
          int retry,
          String note,
          long occuredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.recoverStatus = recoverStatus;
    this.retry = retry;
    this.note = note;
    this.occuredOn = occuredOn;
  }

  public T getWorkflowId() {
    return workflowId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public int getEventType() {
    return eventType;
  }

  public RecoverStatus getRecoverStatus() {
    return recoverStatus;
  }

  public void setRecoverStatus(RecoverStatus recoverStatus) {
    this.recoverStatus = recoverStatus;
  }

  public int getRetry() {
    return retry;
  }

  public void setRetry(int retry) {
    this.retry = retry;
  }

  public String getNote() {
    return note;
  }

  public long getOccuredOn() {
    return occuredOn;
  }
}