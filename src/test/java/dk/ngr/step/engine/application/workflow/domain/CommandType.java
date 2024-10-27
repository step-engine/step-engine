package dk.ngr.step.engine.application.workflow.domain;

import dk.ngr.step.engine.common.StringUtil;

public enum CommandType {
  ORDER_CREATE(0, "ORDER_CREATE");

  private int id;
  private String type;

  CommandType(int id, String type) {
    this.id = id;
    this.type = type;
  }

  public static CommandType toEnum(String type) {
    if (StringUtil.isEmpty(type))
      throw new IllegalArgumentException("Command type is null or empty");

    for (CommandType val : values()) {
      if (val.name().equalsIgnoreCase(type)) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown command type: " + type);
  }

  public static CommandType toEnum(long value) {
    for (CommandType val : values()) {
      if (val.id == value) {
        return val;
      }
    }

    throw new IllegalArgumentException("Unknown command type value: " + value);
  }

  public int toId() { return id; }

  public String toType() { return type; }
}