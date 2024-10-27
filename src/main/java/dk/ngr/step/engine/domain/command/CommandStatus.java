package dk.ngr.step.engine.domain.command;

import dk.ngr.step.engine.common.StringUtil;

public enum CommandStatus {
  TO_BE_PROCESSED(0, "To be processed"),
  RUNNING(1, "Running"),
  PROCESSED(2, "Processed"),
  WAIT(3, "Wait"),
  TIMEOUT(4, "Timeout"),
  RETRY(5, "Retry"),
  MANUAL(6, "Manual"),
  ERROR(7, "Error"),
  IGNORED(8, "Ignored"),
  DUPLICATE(9, "Duplicate");

  private int id;
  private String status;

  CommandStatus(int id, String status) {
    this.id = id;
    this.status = status;
  }

  public static CommandStatus toEnum(String type) {
    if (StringUtil.isEmpty(type))
      throw new IllegalArgumentException("Command status type is null or empty");

    for (CommandStatus val : values()) {
      if (val.name().equalsIgnoreCase(type)) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown command status type: " + type);
  }

  public static CommandStatus toEnum(int value) {
    for (CommandStatus val : values()) {
      if (val.id == value) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown command status value: " + value);
  }

  public int toId() { return id; }

  public String toStatus() { return status; }
}