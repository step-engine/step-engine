package org.step.engine.repository;

import org.step.engine.domain.model.Stacktrace;
import java.util.List;

public interface StacktraceRepository<T> {
  void save(Stacktrace<T> stacktrace);
  List<Stacktrace<T>> findByWorkflowId(T workflowId);
  void deleteAll();
}