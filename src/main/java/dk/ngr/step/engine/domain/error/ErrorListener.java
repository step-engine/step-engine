package dk.ngr.step.engine.domain.error;

import dk.ngr.step.engine.domain.error.event.StepFailedError;
import dk.ngr.step.engine.domain.error.event.StepFailedManual;
import dk.ngr.step.engine.domain.error.event.StepFailedRetry;
import dk.ngr.step.engine.domain.error.event.TimeoutEvent;

public interface ErrorListener<T extends Object> {
  default void handle(StepFailedRetry<T> event) {}
  default void handle(StepFailedManual<T> event) {}
  default void handle(StepFailedError<T> event) {}
  default void handle(TimeoutEvent<T> event) {}
}
