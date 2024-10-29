package org.step.engine.domain.model;

public class Stacktrace<T> {
  private final T workflowId;
  private final String applicationId;
  private final String stacktrace;
  private final long occurredOn;

  public Stacktrace(
      T workflowId,
      String applicationId,
      String stacktrace,
      long occurredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.stacktrace = stacktrace;
    this.occurredOn = occurredOn;
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

  public long getOccurredOn() {
    return occurredOn;
  }
}