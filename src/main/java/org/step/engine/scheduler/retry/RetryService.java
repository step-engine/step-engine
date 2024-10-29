package org.step.engine.scheduler.retry;

import org.step.engine.common.StacktraceUtil;
import org.step.engine.domain.event.EventListener;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.domain.event.DomainEvent;
import org.step.engine.domain.model.Stacktrace;
import org.step.engine.repository.EventRepository;
import org.step.engine.repository.StacktraceRepository;
import org.step.engine.domain.error.WorkflowException;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class RetryService<T> {
  private static final Logger logger = LogManager.getLogger(RetryService.class);
  private final EventListener workflow;
  private final EventRepository<T> eventRepository;
  private final StacktraceRepository<T> stacktraceRepository;

  public void retry(T workflowId) {
    Optional<DomainEvent<T>> domainEvent = Optional.empty();
    try {
      List<DomainEvent<T>> events = eventRepository.read(workflowId);
      domainEvent = Optional.of(events.getLast());
      EventPublisher.<T>of().setState(workflowId, events);
      domainEvent.get().accept(workflow, events);
    } catch (WorkflowException e) {

      // TODO: publish error event instead of below..

      domainEvent.ifPresent(event -> stacktraceRepository.save(
              new Stacktrace<T>(
              event.workflowId,
              event.applicationId,
              StacktraceUtil.getRootCauseFormattedMessage(e),
              System.currentTimeMillis())));

      logger.error("Failed to retry", e);

    } finally {
      EventPublisher.of().resetStateOfId(workflowId);
    }
  }
}