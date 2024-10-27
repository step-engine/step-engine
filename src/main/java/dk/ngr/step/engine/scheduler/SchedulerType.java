package dk.ngr.step.engine.scheduler;

import dk.ngr.step.engine.common.StringUtil;

public enum SchedulerType {
  RETRY(0, "Retry"),
  WAIT(1, "Wait timeout");

  private int id;
  private String type;

  SchedulerType(int id, String type) {
    this.id = id;
    this.type = type;
  }

  public static SchedulerType toEnum(String type) {
    if (StringUtil.isEmpty(type))
      throw new IllegalArgumentException("Scheduler type is null or empty");

    for (SchedulerType val : values()) {
      if (val.name().equalsIgnoreCase(type)) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown scheduler type: " + type);
  }

  public static SchedulerType toEnum(long value) {
    for (SchedulerType val : values()) {
      if (val.id == value) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown scheduler type value: " + value);
  }

  public int id() { return id; }
  public String type() {
    return type;
  }
}