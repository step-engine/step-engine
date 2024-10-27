package dk.ngr.step.engine.application;

import dk.ngr.step.engine.application.kafka.Message;
import dk.ngr.step.engine.application.service.CallbackService;
import dk.ngr.step.engine.application.service.CommandService;
import dk.ngr.step.engine.application.util.SleepUtil;
import dk.ngr.step.engine.application.workflow.domain.EventType;
import dk.ngr.step.engine.domain.command.CommandStatus;
import dk.ngr.step.engine.domain.error.RecoverStatus;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.scheduler.SchedulerStatus;
import dk.ngr.step.engine.application.config.*;
import dk.ngr.step.engine.scheduler.Processor;
import dk.ngr.step.engine.scheduler.SchedulerProcessor;
import dk.ngr.step.engine.scheduler.retry.RetryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.UUID;

public class RetryWithMaxAttemptTest {
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
     * Before going further please see description of the below statements in WorkflowWithoutTimeoutTest..
     *
     * The point of this test is to let the ServiceProviderNotifierStep retry maxAttempt times by throwing an
     * exception in the serviceProviderNotifierClient.notify(...) call since the external service is down.
     *
     * Note that the exception is orchestrated by calling cc.serviceProviderError() in the above.
     * The time for executing of retry will happen if time is equal or has passed executeAt ie. setting
     * retryAfterMilliseconds = 0 will make that happen by calling wc.setupForRetryInUnitTest().
     */
    CommandService commandService = new CommandService(rc.commandRepository, rc.stacktraceRepository);
    commandService.onOrderCreate(Message.builder().orderNumber(Constant.orderNumber).mobileNumber(Constant.mobileNumber).build());
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());
    CallbackService callbackService = new CallbackService(rc.callbackRepository);
    callbackService.onSmsAcknowledged(Constant.callbackId);

    /**
     * The workflow is now in retry mode.
     *
     * Verify that the last event published is the PrometheusCalled event ie. the event that triggers the
     * ServiceProviderNotifierStep (that just failed).
     */
    List<DomainEvent<UUID>> events = rc.eventRepository.read(Constant.workflowId);
    Assert.assertEquals(EventType.PROMETHEUS_CALLED.ordinal(), events.get(events.size()-1).eventType);

    /**
     * Verify
     *
     *   that command status == RETRY
     *   that the recover status == RETRY
     *   that retryCount == 0  (since the ErrorHandler has just handled the first exception)
     *   an entry in the stacktrace table
     *   a scheduled wait (already processed)
     *   a scheduled retry to be processed
     */
    Assert.assertEquals(CommandStatus.RETRY.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());
    Assert.assertEquals(rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRecoverStatus(), RecoverStatus.RETRY.ordinal());
    Assert.assertEquals(0, rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRetry());
    Assert.assertEquals(1, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());

    // Prepare for retry
    RetryService<UUID> retryService = new RetryService<>(wc.workflow, rc.eventRepository, rc.stacktraceRepository);
    Processor processor = new SchedulerProcessor(rc.schedulerRespository, retryService);
    SleepUtil.sleep(1);

    /**
     * Do first retry, reset and verify:
     *
     *   that command status == RETRY
     *   that the recover status == RETRY
     *   that retryCount == 1  (since one retry have been tried)
     *   2 entries in the stacktrace table
     *   a scheduled wait (processed) + a scheduled retry (processed)
     *   a scheduled retry to be processed
     */
    processor.process();
    SleepUtil.sleep(1);
    Assert.assertEquals(CommandStatus.RETRY.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());
    Assert.assertEquals(rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRecoverStatus(), RecoverStatus.RETRY.ordinal());
    Assert.assertEquals(1, rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRetry());
    Assert.assertEquals(2, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());
    Assert.assertEquals(2, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());

    /**
     * Do second retry, reset and verify:
     *
     *   that command status == retry
     *   that the recover status == retry
     *   that retryCount == 2  (since 2 retries have been tried)
     *   3 entries in the stacktrace table
     *   a scheduled wait (processed) + 2 scheduled retry (processed)
     *   a scheduled retry to be processed
     */
    processor.process();
    SleepUtil.sleep(1);
    Assert.assertEquals(CommandStatus.RETRY.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());
    Assert.assertEquals(rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRecoverStatus(), RecoverStatus.RETRY.ordinal());
    Assert.assertEquals(2, rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRetry());
    Assert.assertEquals(3, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());
    Assert.assertEquals(3, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(1, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());

    /**
     * Do third retry, reset and verify:
     *
     *   that command status == manual (since maxAttempt have been tried)
     *   that the recover status == manual (since maxAttempt have been tried)
     *   that retryCount == 3  (since 3 retries have been tried)
     *   4 entries in the stacktrace table
     *   a scheduled wait (processed) + 3 scheduled retry (processed)
     *   no scheduled retry to be processed (since it gave up and is on manual)
     */
    processor.process();
    SleepUtil.sleep(1);
    Assert.assertEquals(CommandStatus.MANUAL.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());
    Assert.assertEquals(rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRecoverStatus(), RecoverStatus.MANUAL.ordinal());
    Assert.assertEquals(3, rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRetry());
    Assert.assertEquals(4, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());
    Assert.assertEquals(4, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(0, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());
  }
}