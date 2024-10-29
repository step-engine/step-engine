package org.step.engine.domain.model;

import org.step.engine.domain.error.RecoverStatus;

public class Recover<T> {
  private final T workflowId;
  private final String applicationId;
  private final int eventType;
  private RecoverStatus recoverStatus;
  private int retry;
  private final String note;
  private final long occurredOn;

  public Recover(
          T workflowId,
          String applicationId,
          int eventType,
          RecoverStatus recoverStatus,
          int retry,
          String note,
          long occurredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.recoverStatus = recoverStatus;
    this.retry = retry;
    this.note = note;
    this.occurredOn = occurredOn;
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

  public long getOccurredOn() {
    return occurredOn;
  }
}