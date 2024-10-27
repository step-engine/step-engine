package dk.ngr.step.engine.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Status {
  private boolean enabled;
  private int interval;
  private boolean running;
  private boolean idle;
  private long retry;
  private long errors;
  private String error;
  private String errorTime;
  private String start;
}
