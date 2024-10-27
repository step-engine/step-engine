package dk.ngr.step.engine.application.config;

import dk.ngr.step.engine.application.util.ProductValidator;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationConfig {
  public ProductValidator productValidator = mock(ProductValidator.class);

  public void success() {
    when(productValidator.validate(anyString())).thenReturn(true);
  }

  public void commandIgnored() {
    when(productValidator.validate(anyString())).thenReturn(false);
  }
}