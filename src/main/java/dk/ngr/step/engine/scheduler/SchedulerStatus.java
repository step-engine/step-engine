package dk.ngr.step.engine.scheduler;

import dk.ngr.step.engine.common.StringUtil;

public enum SchedulerStatus {
  TO_BE_PROCESSED(0, "To be processed"),
  PROCESSED(1, "Processed");

  private int id;
  private String status;

  SchedulerStatus(int id, String status) {
    this.id = id;
    this.status = status;
  }

  public static SchedulerStatus toEnum(String status) {
    if (StringUtil.isEmpty(status))
      throw new IllegalArgumentException("Scheduler status is null or empty");

    for (SchedulerStatus val : values()) {
      if (val.name().equalsIgnoreCase(status)) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown scheduler status: " + status);
  }

  public static SchedulerStatus toEnum(long value) {
    for (SchedulerStatus val : values()) {
      if (val.id == value) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown scheduler status value: " + value);
  }

  public int id() { return id; }

  public String status() { return this.status; }
}