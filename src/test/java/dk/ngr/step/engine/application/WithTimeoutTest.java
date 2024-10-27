package dk.ngr.step.engine.application;

import dk.ngr.step.engine.application.kafka.Message;
import dk.ngr.step.engine.application.service.CommandService;
import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.scheduler.SchedulerStatus;
import dk.ngr.step.engine.application.config.*;
import dk.ngr.step.engine.scheduler.Processor;
import dk.ngr.step.engine.scheduler.SchedulerProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.UUID;

public class WithTimeoutTest {
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
     * classes and the resources from the step engine to mimic a scenario with timeout.. since no callback message
     * is received.
     *
     * Note that all mocks in the above are orchestrated to be successful in the above.. but that the SmsAcknowledged
     * is not published (see below).
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
     * Now the callback service is not mimicked ie. the SmsAcknowledged event is not published thus the last event
     * published should be SmsSended.
     */
    List<DomainEvent<UUID>> events = rc.eventRepository.read(Constant.workflowId);
    Assert.assertEquals(EventType.WAIT_EVENT.ordinal(), events.get(events.size()-1).eventType);

    /**
     * Since the SmsAcknowledged event is not published the SchedulerWaitProcessStep will not execute marking the
     * wait entry as processed. Again.. in a normal situation.. when the scheduler thread is running.. if the wait
     * entry is not cleared as processed (in time).. a TimeoutEvent would be published.
     *
     * Notes:
     * The TimeoutEvent is an ErrorEvent.. and is not appended to the event store.
     * The TimeoutEvent has the eventType of the event that was waited for.
     * The wait entry is set to processed and the command status is set to timeout by the ErrorHandler.
     * In order to notify someone about the error make a TimeoutListener implementing the ErrorListener.
     * Whenever the callback message is received the workflow just continue processing because of the SmsAcknowledged
     * event. At that time the scheduler wait entry is just set to processed (again).
     *
     * Again.. the callback message could be received from a queue or REST API.. a queue is definitely recommended in
     * case the service is down.
     *
     *  Since this is a test of timeout the SchedulerProcessor will be called (as the SchedulerWorker do normally)
     *  to see if the wait entry has timed out.. and it has since timeoutInMilliseconds is set to 1 in the SmsStep.
     */
    Processor processor = new SchedulerProcessor(rc.schedulerRespository, null);
    processor.process();

    // Verify that the scheduler wait entry is set to processed
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());

    // Verify that the command (workflow) is in timeout
    Assert.assertEquals(CommandStatus.TIMEOUT.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());

    // Verify that the recover table is empty (since we can't recover ourselves)
    Assert.assertFalse(rc.recoverRepository.findByWorkflowId(Constant.workflowId).isPresent());

    // Verify that the stacktrace table is empty
    Assert.assertEquals(0, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());

    // Verify that the scheduler wait entry is marked as processed
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
  }
}