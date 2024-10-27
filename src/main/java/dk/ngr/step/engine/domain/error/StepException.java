package dk.ngr.step.engine.domain.error;

import dk.ngr.step.engine.domain.event.DomainEvent;

public class StepException extends RuntimeException {
  public DomainEvent<? extends Object> event;

  public <T> StepException(String message, DomainEvent<T> event) {
    super(message); this.event = event;
  }

  public <T> StepException(String message, Throwable throwable, DomainEvent<T> event) {
    super(message, throwable); this.event = event;
  }

  public <T> StepException(Throwable throwable, DomainEvent<T> event) {
    super(throwable); this.event = event;
  }
}
