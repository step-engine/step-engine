package dk.ngr.step.engine.domain.error;

import dk.ngr.step.engine.domain.error.event.ErrorEvent;

import java.util.ArrayList;
import java.util.List;

public class ErrorPublisher {
  private static ErrorPublisher instance;
  private List<ErrorListener> listeners;

  private ErrorPublisher() { this.initialize(); }

  public static ErrorPublisher of() {
    if (instance == null) {
      instance = new ErrorPublisher();
    }

    return instance;
  }

  public void publish(final ErrorEvent event) {
    if (hasListeners()) {
      for (ErrorListener listener : listeners) {
        event.accept(listener);
      }
    }
  }

  public ErrorPublisher add(ErrorListener errorListener) {
    initialize();
    listeners.add(errorListener);
    return instance;
  }

  private void initialize() {
    if (this.listeners == null) {
      this.listeners = new ArrayList<>();
    }
  }

  private boolean hasListeners() {
    return listeners != null && listeners.size() != 0;
  }
}