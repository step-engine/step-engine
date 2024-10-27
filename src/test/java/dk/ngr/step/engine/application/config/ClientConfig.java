package dk.ngr.step.engine.application.config;

import dk.ngr.step.engine.application.client.ChubClient;
import dk.ngr.step.engine.application.client.PrometheusClient;
import dk.ngr.step.engine.application.client.ServiceProviderNotifierClient;
import dk.ngr.step.engine.application.client.SmsClient;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

/**
 * Mock and configure clients.
 *
 */
public class ClientConfig {
  public ChubClient chubClient = mock(ChubClient.class);
  public PrometheusClient prometheusClient = mock(PrometheusClient.class);
  public ServiceProviderNotifierClient serviceProviderNotifierClient = mock(ServiceProviderNotifierClient.class);
  public SmsClient smsClient = mock(SmsClient.class);

  public ClientConfig() {
    when(chubClient.getProductNumber(anyString())).thenReturn(Constant.productNumber);
    when(smsClient.send(anyString(), anyString())).thenReturn(Constant.callbackId);
  }

  public void makeServiceProviderClientErrant() {
    Mockito.doThrow(new RuntimeException("500 Internal Error")).when(serviceProviderNotifierClient).notify(anyString());
  }

  public void makeServiceProviderClientSuccessful() {
    serviceProviderNotifierClient = mock(ServiceProviderNotifierClient.class);
  }
}