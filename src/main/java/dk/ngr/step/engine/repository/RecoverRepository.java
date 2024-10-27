package dk.ngr.step.engine.repository;

import dk.ngr.step.engine.domain.error.RecoverStatus;
import dk.ngr.step.engine.domain.model.Recover;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecoverRepository<T> {
  void save(Recover<T> recover);
  Optional<Recover<T>> findByApplicationId(String applicationId);
  Optional<Recover<T>> findByWorkflowId(T workflowId);
  void updateStatus(T workflowId, RecoverStatus recoverStatus);
  long countByStatus(RecoverStatus recoverStatus);
  List<Recover<T>> findByStatus(RecoverStatus recoverStatus);
  void deleteAll();
}