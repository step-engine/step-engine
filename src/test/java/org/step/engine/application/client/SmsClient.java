package org.step.engine.application.client;

public interface SmsClient {
  String send(String mobileNumber, String content);
}
