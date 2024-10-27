package dk.ngr.step.engine.application.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
  private String orderNumber;
  private String mobileNumber;
}
