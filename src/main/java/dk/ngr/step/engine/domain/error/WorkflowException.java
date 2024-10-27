package dk.ngr.step.engine.domain.error;

public class WorkflowException extends RuntimeException {
  public WorkflowException(String message) {
    super(message);
  }

  public WorkflowException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
