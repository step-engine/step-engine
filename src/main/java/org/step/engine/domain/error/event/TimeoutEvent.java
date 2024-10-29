package org.step.engine.domain.error.event;

import org.step.engine.domain.error.ErrorListener;

public class TimeoutEvent<T> extends ErrorEvent<T> {

  public TimeoutEvent(T workflowId, String applicationId, int eventType) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis());
    this.eventType = eventType;
  }

  @Override
  public void accept(ErrorListener<T> errorListener) {
    errorListener.handle(this);
  }}
