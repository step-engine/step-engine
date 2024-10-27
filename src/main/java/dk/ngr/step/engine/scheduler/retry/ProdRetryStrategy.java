package dk.ngr.step.engine.scheduler.retry;

public class ProdRetryStrategy implements RetryStrategy {
  private static long ONE_SECOND_IN_MILLIES = 1000;

  @Override
  public long get(int retry, long now) {
    return now + (180*ONE_SECOND_IN_MILLIES);
  }
}
