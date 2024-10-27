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
import dk.ngr.step.engine.scheduler.retry.SchedulerRetryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.UUID;

public class ManualRecoverTest {
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
     * The point of this test is to test recover from one workflow in manual mode ie. let the workflow retry
     * maxAttempt times. Then after a retry is scheduled manually using the SchedulerRetryService.
     *
     * The workflow is put into manual mode by using the calls from RetryWithMaxAttemptTest where the
     * serviceProviderNotifierClient.notify(...) call (at first) is throwing an exception in the
     * ServiceProviderNotifierStep. After being in manual mode the serviceProviderNotifierClient mock
     * is changed to return successfully.
     *
     * For more info, see RetryWithMaxAttemptTest.
     */
    CommandService commandService = new CommandService(rc.commandRepository, rc.stacktraceRepository);
    commandService.onOrderCreate(Message.builder().orderNumber(Constant.orderNumber).mobileNumber(Constant.mobileNumber).build());
    CallbackService callbackService = new CallbackService(rc.callbackRepository);
    callbackService.onSmsAcknowledged(Constant.callbackId);
    RetryService<UUID> retryService = new RetryService<>(wc.workflow, rc.eventRepository, rc.stacktraceRepository);
    Processor processor = new SchedulerProcessor<UUID>(rc.schedulerRespository, retryService);

    processor.process();
    SleepUtil.sleep(1);

    processor.process();
    SleepUtil.sleep(1);

    processor.process();
    SleepUtil.sleep(1);

    /**
     * Verify that the workflow is in manual mode
     */
    Assert.assertEquals(CommandStatus.MANUAL, rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());
    Assert.assertEquals(rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRecoverStatus(), RecoverStatus.MANUAL.ordinal());
    Assert.assertEquals(3, rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRetry());
    Assert.assertEquals(4, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());
    Assert.assertEquals(4, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(0, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());

    /**
     * Verify that the last published event was PrometheusCalled
      */
    List<DomainEvent<UUID>> events = rc.eventRepository.read(Constant.workflowId);
    Assert.assertEquals(EventType.PROMETHEUS_CALLED.ordinal(), events.get(events.size()-1).eventType);

    /**
     * Do the following:
     *
     *   let the serviceProviderNotifierClient return with success.
     *   add a scheduled retry.
     *   process the entry.
     *
     * and verify that:
     *
     *   the last published event is StatusUpdated (since the workflow process is done)
     *   command status has changed to PROCESSED
     *   recover status has changed to PROCESSED
     *   retryCount is still 3
     *   stacktraces are still 4 (because the retry succeeded)
     *   that scheduled PROCESSED entries is 5 because of 1 wait, 3 retry and 1 manual recover.
     *   and that scheduler TO_BE_PROCESSED is 0.
     */
    cc.makeServiceProviderClientSuccessful();
    wc.workflow.setServiceProviderNotifierClient(cc.serviceProviderNotifierClient);
    SchedulerRetryService<UUID> schedulerRetryService = new SchedulerRetryService<>(rc.recoverRepository, rc.schedulerRespository);
    schedulerRetryService.retry(Constant.workflowId);
    processor.process();

    events = rc.eventRepository.read(Constant.workflowId);

    Assert.assertEquals(EventType.STATUS_UPDATED.ordinal(), events.get(events.size()-1).eventType);
    Assert.assertEquals(CommandStatus.PROCESSED.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());
    Assert.assertEquals(rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRecoverStatus(), RecoverStatus.PROCESSED.ordinal());
    Assert.assertEquals(3, rc.recoverRepository.findByWorkflowId(Constant.workflowId).get().getRetry());
    Assert.assertEquals(4, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());
    Assert.assertEquals(5, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(0, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());
  }
}