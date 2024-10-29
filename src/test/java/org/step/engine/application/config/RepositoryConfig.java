package org.step.engine.application.config;

import org.step.engine.application.client.CallbackRepository;
import org.mockito.Mockito;
import org.step.engine.repository.*;
import org.step.engine.repository.memdb.*;

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