package org.step.engine.application.service;

import org.step.engine.application.workflow.domain.CommandType;
import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.application.workflow.domain.event.CommandReceived;
import org.step.engine.domain.command.CommandStatus;
import org.step.engine.domain.event.EventPublisher;
import org.step.engine.application.config.Constant;
import org.step.engine.application.kafka.Message;
import org.step.engine.common.JsonUtil;
import org.step.engine.common.StacktraceUtil;
import org.step.engine.domain.model.Command;
import org.step.engine.domain.model.Stacktrace;
import org.step.engine.repository.CommandRepository;
import org.step.engine.repository.StacktraceRepository;
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