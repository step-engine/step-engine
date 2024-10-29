package org.step.engine.domain.model;

import org.step.engine.scheduler.SchedulerStatus;
import org.step.engine.scheduler.SchedulerType;

public class Scheduler<T> {
  private final T workflowId;
  private final String applicationId;
  private final SchedulerType schedulerType;
  private final int eventType;
  private SchedulerStatus schedulerStatus;
  private final long executeAt;
  private final long occurredOn;

  public Scheduler(T workflowId,
                   String applicationId,
                   SchedulerType schedulerType,
                   int eventType,
                   SchedulerStatus schedulerStatus,
                   long executeAt,
                   long occurredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.schedulerType = schedulerType;
    this.eventType = eventType;
    this.schedulerStatus = schedulerStatus;
    this.executeAt = executeAt;
    this.occurredOn = occurredOn;
  }

  public T getWorkflowId() {
    return workflowId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public SchedulerType getSchedulerType() {
    return schedulerType;
  }

  public int getEventType() {
    return eventType;
  }

  public SchedulerStatus getSchedulerStatus() {
    return schedulerStatus;
  }

  public void setSchedulerStatus(SchedulerStatus schedulerStatus) {
    this.schedulerStatus = schedulerStatus;
  }

  public long getExecuteAt() {
    return executeAt;
  }

  public long getOccurredOn() {
    return occurredOn;
  }
}