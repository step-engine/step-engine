package org.step.engine.application.workflow.domain;

import org.step.engine.common.Mapper;

public class EventTypeMapper implements Mapper<Integer,String> {

  @Override
  public String map(Integer eventType) {
    return EventType.values()[eventType].getName();
  }
}