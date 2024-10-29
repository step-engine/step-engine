package org.step.engine.domain.error;

import org.step.engine.domain.error.event.StepFailedError;
import org.step.engine.domain.error.event.StepFailedManual;
import org.step.engine.domain.error.event.StepFailedRetry;
import org.step.engine.domain.error.event.TimeoutEvent;

public interface ErrorListener<T extends Object> {
  default void handle(StepFailedRetry<T> event) {}
  default void handle(StepFailedManual<T> event) {}
  default void handle(StepFailedError<T> event) {}
  default void handle(TimeoutEvent<T> event) {}
}
