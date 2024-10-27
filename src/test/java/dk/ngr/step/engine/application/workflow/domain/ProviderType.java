package dk.ngr.step.engine.application.workflow.domain;

public enum ProviderType {
  ENIIG_FIBER("EniigFiber");

  private String type;

  private ProviderType(String type) {
    this.type = type;
  }
}
