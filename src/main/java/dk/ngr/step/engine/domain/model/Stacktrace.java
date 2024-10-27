package dk.ngr.step.engine.domain.model;

public class Stacktrace<T> {
  private T workflowId;
  private String applicationId;
  private String stacktrace;
  private long occuredOn;

  public Stacktrace(
          T workflowId,
          String applicationId,
          String stacktrace,
          long occuredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.stacktrace = stacktrace;
    this.occuredOn = occuredOn;
  }

  public T getWorkflowId() {
    return workflowId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public String getStacktrace() {
    return stacktrace;
  }

  public long getOccuredOn() {
    return occuredOn;
  }
}