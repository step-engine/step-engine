package org.step.engine.domain.error.event;

import org.step.engine.domain.error.ErrorListener;

public class StepFailedManual<T> extends ErrorEvent<T> {

  public StepFailedManual(T workflowId, String applicationId, int eventType, Throwable throwable) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis(), throwable);
  }

  @Override
  public void accept(ErrorListener<T> errorListener) {
    errorListener.handle(this);
  }
}