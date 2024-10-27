package dk.ngr.step.engine.scheduler.retry;

public class TestRetryStrategy implements RetryStrategy {
  private static long ONE_SECOND_IN_MILLIES = 1000;

  @Override
  public long get(int retry, long now) {
    return now + (10*ONE_SECOND_IN_MILLIES);
  }
}
