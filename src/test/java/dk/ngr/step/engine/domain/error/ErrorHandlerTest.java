package dk.ngr.step.engine.domain.error;

import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.error.event.StepFailedRetry;
import dk.ngr.step.engine.domain.model.Command;
import dk.ngr.step.engine.domain.model.Recover;
import dk.ngr.step.engine.domain.model.Scheduler;
import dk.ngr.step.engine.domain.model.Stacktrace;
import dk.ngr.step.engine.repository.*;
import dk.ngr.step.engine.repository.memdb.*;
import dk.ngr.step.engine.scheduler.DefaultRetryStrategy;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ErrorHandlerTest {
    CommandRepository<UUID> commandRepository;
    RecoverRepository<UUID> recoverRepository;
    StacktraceRepository<UUID> stacktraceRepository;
    SchedulerRepository<UUID> schedulerRespository;
    StepFailedRetry<UUID> stepFailedRetry;
    int maxAttempt = 3;
    ErrorHandler<UUID> errorHandler;
    UUID workflowId = UUID.randomUUID();
    String applicationId = "1234";
    int commandType = 0;
    int eventType = 0;
    Throwable throwable = new Throwable("some error");

    @Before
    public void before() {
        commandRepository = new CommandRepositoryImpl();
        recoverRepository = new RecoverRespositoryImpl();
        stacktraceRepository = new StacktraceRepositoryImpl();
        schedulerRespository = new SchedulerRepositoryImpl();
        errorHandler = new ErrorHandler<>(
                recoverRepository,
                stacktraceRepository,
                schedulerRespository,
                commandRepository,
                new DefaultRetryStrategy(),
                maxAttempt);

        // Save a command TO_BE_PROCESSED
        Command<UUID> command = new Command<>(
                workflowId,
                applicationId,
                commandType,
                CommandStatus.TO_BE_PROCESSED,
                "someCommandName",
                "{ some command }",
                "note",
                0);
        commandRepository.save(command);

        stepFailedRetry = new StepFailedRetry<>(workflowId, applicationId, eventType, maxAttempt, throwable);
    }


    @Test
    public void testRetryFirstTime() {

        // Simulate StepFailedRetry..
        errorHandler.handle(stepFailedRetry);

        // Verify command
        Optional<Command<UUID>> oCommand = commandRepository.findByWorkflowId(workflowId);
        Command<UUID> cmd = oCommand.orElseThrow(() -> new IllegalArgumentException("Failed to find command"));
        assert cmd.getCommandStatus() == CommandStatus.RETRY;

        // Verify recover
        Optional<Recover<UUID>> oRecover = recoverRepository.findByWorkflowId(workflowId);
        Recover<UUID> recover = oRecover.orElseThrow(() -> new IllegalArgumentException("Failed to find recover"));
        assert recover.getRetry() == 0;

        // Verify 1 stacktrace
        List<Stacktrace<UUID>> stacktraces = stacktraceRepository.findByWorkflowId(workflowId);
        assert stacktraces.size() == 1;

        // Verify 1 scheduler
        List<Scheduler<UUID>> schedulers = schedulerRespository.findByWorkflowId(workflowId);
        assert schedulers.size() == 1;
    }

    @Test
    public void testRetrySecondTime() {

        // Simulate StepFailedRetry the first time..
        errorHandler.handle(stepFailedRetry);

        // Simulate StepFailedRetry the second time..
        errorHandler.handle(stepFailedRetry);

        // Verify command
        Optional<Command<UUID>> oCommand = commandRepository.findByWorkflowId(workflowId);
        Command<UUID> cmd = oCommand.orElseThrow(() -> new IllegalArgumentException("Failed to find command"));
        assert cmd.getCommandStatus() == CommandStatus.RETRY;

        // Verify recover
        Optional<Recover<UUID>> oRecover = recoverRepository.findByWorkflowId(workflowId);
        Recover<UUID> recover = oRecover.orElseThrow(() -> new IllegalArgumentException("Failed to find recover"));
        assert recover.getRetry() == 1;

        // Verify 2 stacktrace
        List<Stacktrace<UUID>> stacktraces = stacktraceRepository.findByWorkflowId(workflowId);
        assert stacktraces.size() == 2;

        // Verify 2 scheduler
        List<Scheduler<UUID>> schedulers = schedulerRespository.findByWorkflowId(workflowId);
        assert schedulers.size() == 2;
    }

    @Test
    public void testRetryMaxAttempt() {

        // Simulate StepFailedRetry maxAttempt
        errorHandler.handle(stepFailedRetry);
        errorHandler.handle(stepFailedRetry);
        errorHandler.handle(stepFailedRetry);
        errorHandler.handle(stepFailedRetry);

        // Verify command
        Optional<Command<UUID>> oCommand = commandRepository.findByWorkflowId(workflowId);
        Command<UUID> cmd = oCommand.orElseThrow(() -> new IllegalArgumentException("Failed to find command"));
        assert cmd.getCommandStatus() == CommandStatus.MANUAL;

        // Verify recover
        Optional<Recover<UUID>> oRecover = recoverRepository.findByWorkflowId(workflowId);
        Recover<UUID> recover = oRecover.orElseThrow(() -> new IllegalArgumentException("Failed to find recover"));
        assert recover.getRetry() == 3;

        // Verify 2 stacktrace
        List<Stacktrace<UUID>> stacktraces = stacktraceRepository.findByWorkflowId(workflowId);
        assert stacktraces.size() == 4;

        // Verify 2 scheduler
        List<Scheduler<UUID>> schedulers = schedulerRespository.findByWorkflowId(workflowId);
        assert schedulers.size() == 3;
    }
}