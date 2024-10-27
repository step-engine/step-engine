package dk.ngr.step.engine.domain.error;

import dk.ngr.step.engine.common.StringUtil;

public enum RecoverStatus {
  PROCESSED(0, "Processed"),
  RETRY(1, "Retry"),
  MANUAL(2, "Manual"),
  ERROR(3, "Error");

  private int id;
  private String status;

  RecoverStatus(int id, String status) {
    this.id = id;
    this.status = status;
  }

  public static RecoverStatus toEnum(String status) {
    if (StringUtil.isEmpty(status))
      throw new IllegalArgumentException("RecoverStatus status is null or empty");

    for (RecoverStatus val : values()) {
      if (val.name().equalsIgnoreCase(status)) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown recover status: " + status);
  }

  public static RecoverStatus toEnum(long value) {
    for (RecoverStatus val : values()) {
      if (val.id == value) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown recover value: " + value);
  }

  public long toId() { return id; }

  public String toStatus() { return status; }
}