package dk.ngr.step.engine.domain.error.event;

import dk.ngr.step.engine.domain.error.ErrorListener;
import java.util.UUID;

public class StepFailedError<T> extends ErrorEvent<T> {

  public StepFailedError(T workflowId, String applicationId, int eventType, Throwable throwable) {
    super(workflowId, applicationId, eventType, System.currentTimeMillis(), throwable);
  }

  @Override
  public void accept(ErrorListener<T> errorListener) { errorListener.handle(this); }
}