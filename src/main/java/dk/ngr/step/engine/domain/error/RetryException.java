package dk.ngr.step.engine.domain.error;

import dk.ngr.step.engine.domain.event.DomainEvent;

public class RetryException extends RuntimeException {
  public DomainEvent event;
  public int maxAttempt;

  public RetryException(Exception exception, DomainEvent event, int maxAttempt) {
    super(exception); this.event = event; this.maxAttempt = maxAttempt;
  }
  public RetryException(String message, Exception exception, DomainEvent event, int maxAttempt) {
    super(message, exception); this.event = event; this.maxAttempt = maxAttempt;
  }
  public RetryException(String message, DomainEvent event, int maxAttempt) {
    super(message); this.event = event; this.maxAttempt = maxAttempt;
  }
}
