package org.step.engine.scheduler;

public interface Processor {
  void process();
  long retry();
  long waitTimeout();
}
