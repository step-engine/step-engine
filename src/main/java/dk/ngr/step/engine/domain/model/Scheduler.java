package dk.ngr.step.engine.domain.model;

import dk.ngr.step.engine.scheduler.SchedulerStatus;
import dk.ngr.step.engine.scheduler.SchedulerType;

public class Scheduler<T> {
  private T workflowId;
  private String applicationId;
  private SchedulerType schedulerType;
  private int eventType;
  private SchedulerStatus schedulerStatus;
  private long executeAt;
  private long occuredOn;

  public Scheduler(T workflowId,
                   String applicationId,
                   SchedulerType schedulerType,
                   int eventType,
                   SchedulerStatus schedulerStatus,
                   long executeAt,
                   long occuredOn) {
    this.workflowId = workflowId;
    this.applicationId = applicationId;
    this.schedulerType = schedulerType;
    this.eventType = eventType;
    this.schedulerStatus = schedulerStatus;
    this.executeAt = executeAt;
    this.occuredOn = occuredOn;
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

  public long getOccuredOn() {
    return occuredOn;
  }
}