package org.step.engine.domain.error.event;

import org.step.engine.domain.error.ErrorListener;

public abstract class ErrorEvent<T> {
  public T workflowId;
  public String applicationId;
  public int eventType;
  public long occurredOn;
  public Throwable throwable;

  public ErrorEvent(T workflowId, String applicationId, int eventType, long occurredOn, Throwable throwable) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.occurredOn = occurredOn;
    this.throwable = throwable;
  }

  public ErrorEvent(T workflowId, String applicationId, int eventType, long occurredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.eventType = eventType;
    this.occurredOn = occurredOn;
  }

  public abstract void accept(ErrorListener<T> eventListener);
}
