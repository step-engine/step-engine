package dk.ngr.step.engine.scheduler;

import dk.ngr.step.engine.common.StacktraceUtil;
import dk.ngr.step.engine.common.TimestampUtil;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SchedulerWorker implements Worker {
  private static final Logger logger = LogManager.getLogger(SchedulerWorker.class);
  private final Processor processor;
  private boolean enabled;
  private int interval;
  private int waitOnStart;
  private TimestampUtil timestampUtil = new TimestampUtil("yyyy-MM-dd'T'HH:mm:ss");
  private long ONE_SECOND = 1000;
  private Object monitor = new Object();
  private volatile boolean keepAlive = true;
  private volatile boolean running = false;
  private volatile boolean idle = true;
  private volatile long errors = 0;
  private volatile String error = "";
  private volatile String errorTime = "";
  private LocalDateTime start;

  public SchedulerWorker(Processor processor, boolean enabled, int interval, int waitOnStart) {
    this.processor = processor;
    this.enabled = enabled;
    this.interval = interval;
    this.waitOnStart = waitOnStart;
  }

  public void stopThread() {
    this.keepAlive = false;
    notfiyMonitor();
  }

  public void enable(boolean enable) {
    this.enabled = enable;
    notfiyMonitor();
  }

  public void notifyThread() {
    notfiyMonitor();
  }

  @Override
  public Status status() {
    return Status.builder()
            .enabled(enabled)
            .interval(interval)
            .running(running)
            .idle(idle)
            .retry(processor.retry())
            .errors(errors)
            .error(error)
            .errorTime(errorTime)
            .start(timestampUtil.parse(start)).build();
  }

  @Override
  public void interval(int interval) {
    this.interval = interval;
    notfiyMonitor();
  }

  public void run() {
    try {

      initialize();

      synchronized (monitor) {
        monitor.wait(this.waitOnStart * ONE_SECOND);
      }

      while (keepAlive) {
        setIdle(false);
        if (enabled) {
          try {
            processor.process();
          } catch (Exception e) {
            errors++;
            error = StacktraceUtil.getRootCauseFormattedMessage(e);
            errorTime = timestampUtil.getNow();
            logger.error("Scheduler processing failed. stacktrace={}", StacktraceUtil.getRootCauseFormattedMessage(e));
            // TODO: put step on manual instead of logging..
          }
        }

        synchronized (monitor) {
          if (keepAlive) {
            setIdle(true);
            monitor.wait(this.interval * ONE_SECOND);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Worker thread failed", e);
    }

    setRunning(false);
  }

  private void setRunning(boolean running) {
    this.running = running;
  }

  private void initialize() {
    this.start = timestampUtil.now();
    setRunning(true);
  }

  private void setIdle(boolean idle) {
    this.idle = idle;
  }

  private void notfiyMonitor() {
    synchronized (monitor) {
      monitor.notify();
    }
  }
}