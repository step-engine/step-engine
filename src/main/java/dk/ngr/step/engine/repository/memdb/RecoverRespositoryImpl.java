package dk.ngr.step.engine.repository.memdb;

import dk.ngr.step.engine.common.SingleCollector;
import dk.ngr.step.engine.domain.error.RecoverStatus;
import dk.ngr.step.engine.domain.model.Recover;
import dk.ngr.step.engine.repository.RecoverRepository;
import java.util.*;
import java.util.stream.Collectors;

public class RecoverRespositoryImpl implements RecoverRepository<UUID> {
  private Map<UUID, Recover<UUID>> map = new HashMap<>();

  @Override
  public void save(Recover<UUID> recover) {
    map.put(recover.getWorkflowId(), recover);
  }

  @Override
  public Optional<Recover<UUID>> findByApplicationId(String aid) {
    return map.entrySet().stream().map(x -> x.getValue()).filter(x -> aid.equals(x.getApplicationId())).collect(SingleCollector.one());
  }

  @Override
  public Optional<Recover<UUID>> findByWorkflowId(UUID workflowId) {
    if (!map.containsKey(workflowId))
      return Optional.empty();

    return Optional.of(map.get(workflowId));
  }

  @Override
  public void updateStatus(UUID workflowId, RecoverStatus recoverStatus) {
    Optional<Recover<UUID>> oRecover = findByWorkflowId(workflowId);
    oRecover.ifPresent(x -> {
      x.setRecoverStatus(recoverStatus);
      save(x);
    });
  }

  @Override
  public long countByStatus(RecoverStatus recoverStatus) {
    return map.entrySet().stream().map(x -> x.getValue()).filter(x -> recoverStatus == x.getRecoverStatus()).collect(Collectors.toList()).size();
  }

  @Override
  public List<Recover<UUID>> findByStatus(RecoverStatus recoverStatus) {
    return map.entrySet().stream().map(x -> x.getValue()).filter(x -> recoverStatus == x.getRecoverStatus()).collect(Collectors.toList());
  }

  @Override
  public void deleteAll() {
    throw new IllegalArgumentException("Not implemented..");
  }
}