package dk.ngr.step.engine.application.util;

public class SleepUtil {
  public static void sleep(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {}
  }
}
