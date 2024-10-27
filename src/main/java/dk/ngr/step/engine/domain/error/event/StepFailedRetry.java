package dk.ngr.step.engine.domain.error.event;

import dk.ngr.step.engine.domain.error.ErrorListener;
import java.util.UUID;

public class StepFailedRetry<T> extends ErrorEvent<T> {
  private int maxAttempt;

  public StepFailedRetry(T workflowId, String applicationId, int eventType, int maxAttempt, Throwable throwable) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis(), throwable);
    this.maxAttempt = maxAttempt;
  }

  public int maxAttempt() { return maxAttempt; }

  @Override
  public void accept(ErrorListener<T> errorListener) {
    errorListener.handle(this);
  }
}