package dk.ngr.step.engine.application.config;

import dk.ngr.step.engine.application.client.CallbackRepository;
import dk.ngr.step.engine.repository.*;
import dk.ngr.step.engine.repository.memdb.*;
import org.mockito.Mockito;

import java.util.UUID;

/**
 * Configure memdb.. and CallbackRepository..
 */
public class RepositoryConfig {
  public EventRepository<UUID> eventRepository = new EventRepositoryImpl();
  public CommandRepository<UUID> commandRepository = new CommandRepositoryImpl();
  public RecoverRepository<UUID> recoverRepository = new RecoverRespositoryImpl();
  public StacktraceRepository<UUID> stacktraceRepository = new StacktraceRepositoryImpl();
  public SchedulerRepository<UUID> schedulerRespository = new SchedulerRepositoryImpl();
  public CallbackRepository callbackRepository = Mockito.mock(CallbackRepository.class);

  public RepositoryConfig() {
    Mockito.when(callbackRepository.findWorkflowIdByCallbackId(Mockito.anyString())).thenReturn(Constant.workflowId);
  }
}