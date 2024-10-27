package dk.ngr.step.engine.application.service;

import dk.ngr.step.engine.application.workflow.domain.CommandType;
import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.application.workflow.domain.event.CommandReceived;
import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.event.EventPublisher;
import dk.ngr.step.engine.application.config.Constant;
import dk.ngr.step.engine.application.kafka.Message;
import dk.ngr.step.engine.common.JsonUtil;
import dk.ngr.step.engine.common.StacktraceUtil;
import dk.ngr.step.engine.domain.model.Command;
import dk.ngr.step.engine.domain.model.Stacktrace;
import dk.ngr.step.engine.repository.CommandRepository;
import dk.ngr.step.engine.repository.StacktraceRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CommandService {
  private final CommandRepository<UUID> commandRepository;
  private final StacktraceRepository<UUID> stacktraceRepository;

  public void onOrderCreate(Message message) {
    long currentTimeMillis = System.currentTimeMillis();
    UUID workflowId = Constant.workflowId;
    String command = new JsonUtil().toString(message);

    try {

      Optional<Command<UUID>> oCommand = commandRepository.findByApplicationId(message.getOrderNumber());

      if ( ! oCommand.isPresent() ) {
        commandRepository.save(
                new Command<UUID>(
                        workflowId,
                        message.getOrderNumber(),
                        CommandType.ORDER_CREATE.toId(),
                        CommandStatus.RUNNING,
                        "",
                        command,
                        "",
                        currentTimeMillis));

        EventPublisher.<UUID>of().publish(
            new CommandReceived(
                workflowId,
                message.getOrderNumber(),
                EventType.COMMAND_RECEIVED.ordinal(),
                message.getMobileNumber()));

      } else {
        commandRepository.save(
                new Command<UUID>(
                        workflowId,
                        message.getOrderNumber(),
                        CommandType.ORDER_CREATE.toId(),
                        CommandStatus.DUPLICATE,
                        "",
                        command,
                        "",
                        currentTimeMillis));
      }
    } catch (Exception e) {
      commandRepository.save(
              new Command<UUID>(
                  workflowId,
                  message.getOrderNumber(),
                  CommandType.ORDER_CREATE.toId(),
                  CommandStatus.ERROR,
                  command, "",
                  "",
                  currentTimeMillis));
      stacktraceRepository.save(
              new Stacktrace<>(
                      workflowId,
                      message.getOrderNumber(),
                      StacktraceUtil.getRootCauseFormattedMessage(e),
                      System.currentTimeMillis()));
    } finally {
      EventPublisher.of().resetStateOfId(message.getOrderNumber());
    }
  }
}