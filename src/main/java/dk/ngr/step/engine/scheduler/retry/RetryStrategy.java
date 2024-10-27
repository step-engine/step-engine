package dk.ngr.step.engine.scheduler.retry;

public interface RetryStrategy {
  long get(int i, long now);
}
