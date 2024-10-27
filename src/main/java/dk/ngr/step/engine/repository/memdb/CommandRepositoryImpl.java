package dk.ngr.step.engine.repository.memdb;

import dk.ngr.step.engine.common.SingleCollector;
import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.model.Command;
import dk.ngr.step.engine.repository.CommandRepository;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRepositoryImpl implements CommandRepository<UUID> {
  private Map<String, Command<UUID>> map = new HashMap<>();

  @Override
  public void save(Command<UUID> command) {
    map.put(command.getWorkflowId().toString(), command);
  }

  @Override
  public Optional<Command<UUID>> findByApplicationId(String applicationId) {
    return map.entrySet().stream().map(x -> x.getValue()).filter(x -> applicationId.equals(x.getApplicationId())).collect(SingleCollector.one());
  }

  @Override
  public Optional<Command<UUID>> findByWorkflowId(UUID workflowId) {
    String wid = workflowId.toString();
    if (!map.containsKey(wid))
      return Optional.empty();

    return Optional.of(map.get(wid));
  }

  @Override
  public void updateStatus(UUID workflowId, CommandStatus commandStatus) {
    Optional<Command<UUID>> oCommand = findByWorkflowId(workflowId);
    oCommand.ifPresent(x -> {
      x.setCommandStatus(commandStatus);
      save(x);
    });
  }

  @Override
  public long countByStatus(CommandStatus status) {
    return map.entrySet().stream().map(x -> x.getValue()).filter(x -> status.toId() == x.getCommandStatus().toId()).collect(Collectors.toList()).size();
  }

  @Override
  public List<Command<UUID>> findByCommandStatus(CommandStatus status) {
    return map.entrySet().stream().map(x -> x.getValue()).filter(x -> status.toId() == x.getCommandStatus().toId()).collect(Collectors.toList());
  }

  @Override
  public void deleteAll() {
    throw new IllegalArgumentException("Not implemented..");
  }
}