package org.step.engine.domain.error.event;

import org.step.engine.domain.error.ErrorListener;

public abstract class ErrorEvent<T> {
  public T workflowId;
  public String applicationId;
  public int eventType;
  public long occuredOn;
  public Throwable throwable;

  public ErrorEvent(T workflowId, String applicationId, int eventType, long occuredOn, Throwable throwable) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.occuredOn = occuredOn;
    this.throwable = throwable;
  }

  public ErrorEvent(T workflowId, String applicationId, int eventType, long occuredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.occuredOn = occuredOn;
  }

  public abstract void accept(ErrorListener<T> eventListener);
}
