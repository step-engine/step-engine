package dk.ngr.step.engine.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event<T> {
  private T workflowId;
  private String applicationId;
  private int eventType;
  private String eventName;
  private String event;
  private long occuredOn;
}
