package dk.ngr.step.engine.application.config;

import com.fasterxml.uuid.Generators;

import java.util.UUID;

public class Constant {
  public static UUID workflowId = Generators.timeBasedGenerator().generate();
  public static String callbackId = "XYZ";
  public static String orderNumber = "ZYX";
  public static String productNumber = "AA1";
  public static String mobileNumber = "XXXXXXXX";
}
