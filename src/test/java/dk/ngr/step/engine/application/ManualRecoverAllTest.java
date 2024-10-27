package dk.ngr.step.engine.application;

import dk.ngr.step.engine.application.config.ApplicationConfig;
import dk.ngr.step.engine.application.config.ClientConfig;
import dk.ngr.step.engine.application.config.RepositoryConfig;
import dk.ngr.step.engine.application.config.WorkflowConfig;
import org.junit.Before;
import org.junit.Test;

public class ManualRecoverAllTest {
  private RepositoryConfig rc;
  private ClientConfig cc;
  private ApplicationConfig ac;
  private WorkflowConfig wc;

  @Before
  public void before() {
    rc = new RepositoryConfig();
    cc = new ClientConfig();
    cc.makeServiceProviderClientErrant();
    ac = new ApplicationConfig();
    ac.success();
    wc = new WorkflowConfig(
        rc.eventRepository,
        rc.commandRepository,
        rc.recoverRepository,
        rc.stacktraceRepository,
        rc.schedulerRespository,
        rc.callbackRepository,
        ac.productValidator,
        cc.chubClient,
        cc.smsClient,
        cc.prometheusClient,
        cc.serviceProviderNotifierClient);
    wc.setupForRetryInUnitTest();
  }

  @Test
  public void test() {

    /**
     * NOTE:
     *
     * It is left as an exercise to make the ManualRecoverAllTest.
     *
     * The point of this test is to recover all (2) workflows in manual mode ie. let the workflow retry maxAttempt
     * times and enter manual mode. Then after a retry (all) is scheduled manually using the SchedulerRetryService.
     *
     * As in ManualRecoverTest the workflow is put into manual mode by using the calls from RetryWithMaxAttemptTest
     * where the serviceProviderNotifierClient.notify(...) call (at first) is throwing an exception in the
     * ServiceProviderNotifierStep. After being in manual mode the serviceProviderNotifierClient mock
     * is changed to return successfully.
     *
     * At first look in ManualRecoverAllTest.
     *
     * What needs to be added:
     * Just send in two commands with different orderNumbers.. that will make the step engine execute 2 workflows
     * Note that the CallbackRepository could return a CallbackInfo object with (workflowId,orderNumber) thus letting
     * it return (workflowId1,orderNumber1) and (workflowId2,orderNumber2) when
     * CallbackRepository.onSmsAcknowledged(String callbackId) is called twice with callbackId1 and callbackId2
     * respectively. That will let the two workflows continue processing until both fails in the
     * ServiceProviderNotifierStep.
     * Then run processor.process() 3 times to put them in manual mode.
     * Then change the mock of the ServiceProviderNotifierClient (see ManualRecoverTest)
     * and run schedulerRetryService.retryAll().
     *
     * PS sorry this is my last day and I have to deliver the machine.
     */
  }
}