package dk.ngr.step.engine.repository;

import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.model.Command;
import java.util.List;
import java.util.Optional;

public interface CommandRepository<T> {
  void save(Command<T> command);
  Optional<Command<T>> findByApplicationId(String applicationId);
  Optional<Command<T>> findByWorkflowId(T workflowId);
  void updateStatus(T workflowId, CommandStatus commandStatus);
  long countByStatus(CommandStatus status);
  List<Command<T>> findByCommandStatus(CommandStatus commandStatus);
  void deleteAll();
}