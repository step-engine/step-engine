package dk.ngr.step.engine.scheduler;

public interface Worker extends Runnable {
  void stopThread();
  void enable(boolean enable);
  void notifyThread();
  Status status();
  void interval(int interval);
}
