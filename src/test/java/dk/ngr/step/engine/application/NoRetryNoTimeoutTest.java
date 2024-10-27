package dk.ngr.step.engine.application;

import dk.ngr.step.engine.application.kafka.Message;
import dk.ngr.step.engine.application.service.CallbackService;
import dk.ngr.step.engine.application.service.CommandService;
import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.scheduler.SchedulerStatus;
import dk.ngr.step.engine.application.config.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.UUID;

public class NoRetryNoTimeoutTest {
  private RepositoryConfig rc;
  private ClientConfig cc;
  private ApplicationConfig ac;
  private WorkflowConfig wc;

  @Before
  public void before() {
    rc = new RepositoryConfig();
    cc = new ClientConfig();
    cc.makeServiceProviderClientSuccessful();
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
    wc.setupNotCaringAboutRetry();
  }

  @Test
  public void test() {

    /**
     * Note that the SchedulerWorker thread is not started ie. the test is just using and testing the application
     * classes and the resources from the step engine to mimic a scenario without errors ie. no client call errors
     * and a callback message that is received ie. a full workflow processed successfully.
     *
     * Note that all mocks in the above are orchestrated to be successful.. and that the SmsAcknowledged
     * event is published (see below).
     *
     * Below is a command service able to receive a message command and publish the first event.
     */
    CommandService commandService = new CommandService(rc.commandRepository, rc.stacktraceRepository);

    /**
     * Receiving the order create will start the workflow ie. the FiberValidatorStep and then the SmsStep will be
     * executed by the step engine. Since the workflow does not handle the SmsSended event the process will stop..
     * but will be waiting since the SmsStep is annotated with @Wait ie. a wait entry should be located in
     * the scheduler table. After executing the SmsStep the step executor will find the @Wait annotation and
     * append the wait entry using schedulerRepository api.
     */
    commandService.onOrderCreate(Message.builder().orderNumber(Constant.orderNumber).mobileNumber(Constant.mobileNumber).build());
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());

    /**
     * Upon receiving some callback message the callback service will publish the SMS_ACKNOWLEDGED event which will
     * make the SchedulerWaitProcessStep execute marking the wait entry as processed. In a normal situation.. when
     * the scheduler thread is running.. if the wait entry is not cleared as processed (in time).. a TimeoutEvent would
     * be published. Since this is a test.. without timeout.. a SmsAcknowledged is published (callback message) in order
     * to mark the wait entry as processed.. even though time has elapsed since timeoutInMilliseconds is set to 1
     * millisecond (ignored since no thread is running). The timeoutInMilliseconds is used the WorkflowWithTimeoutTest.
     *
     * The callback message could be received from a queue or REST API.. a queue is definitely recommended in case
     * the service is down.
     */
    CallbackService callbackService = new CallbackService(rc.callbackRepository);
    callbackService.onSmsAcknowledged(Constant.callbackId);

    // Verify that all events have been published ie. check the last event
    List<DomainEvent<UUID>> events = rc.eventRepository.read(Constant.workflowId);
    Assert.assertEquals(EventType.STATUS_UPDATED.ordinal(), events.get(events.size()-1).eventType);

    // Verify that the command (workflow) is processed
    Assert.assertEquals(CommandStatus.PROCESSED.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());

    // Verify that the recover table is empty
    Assert.assertFalse(rc.recoverRepository.findByWorkflowId(Constant.workflowId).isPresent());

    // Verify that the stacktrace table is empty
    Assert.assertEquals(0, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());

    // Verify that the scheduler wait entry is marked as processed
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
  }
}