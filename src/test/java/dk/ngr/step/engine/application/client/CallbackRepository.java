package dk.ngr.step.engine.application.client;

import java.util.UUID;

public interface CallbackRepository {
  void save(String callbackId, UUID workflowId);
  UUID findWorkflowIdByCallbackId(String callbackId);
}
