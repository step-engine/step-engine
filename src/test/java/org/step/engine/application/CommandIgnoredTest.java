package org.step.engine.application;

import org.step.engine.application.config.*;
import org.step.engine.application.kafka.Message;
import org.step.engine.application.service.CommandService;
import org.step.engine.application.workflow.domain.EventType;
import org.step.engine.domain.command.CommandStatus;
import org.step.engine.domain.event.DomainEvent;
import org.step.engine.scheduler.SchedulerStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.UUID;

public class CommandIgnoredTest {
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
    ac.commandIgnored();
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
     * classes and the resources from the step engine to mimic a ignore scenario ie. a command has been received
     * that should be ignored.. it is not an error.. it should just be ignored.
     *
     * Note that the validation error is orchestrated by calling ac.commandIgnored() in the above.
     *
     * Below is a command service able to receive a message command and publish the first event.
     */
    CommandService commandService = new CommandService(rc.commandRepository, rc.stacktraceRepository);

    /**
     * Receiving the order create will start the workflow ie. the FiberValidatorStep will execute but since
     * the product is not validated the CommandIgnored is published.
     */
    commandService.onOrderCreate(Message.builder().orderNumber(Constant.orderNumber).mobileNumber(Constant.mobileNumber).build());

    // Verify that all events have been published ie. check the last event
    List<DomainEvent<UUID>> events = rc.eventRepository.read(Constant.workflowId);
    Assert.assertEquals(EventType.STATUS_UPDATED.ordinal(), events.get(events.size()-1).eventType);

    // Verify that the command (workflow) is ignored
    Assert.assertEquals(CommandStatus.IGNORED.ordinal(), rc.commandRepository.findByWorkflowId(Constant.workflowId).get().getCommandStatus());

    // Verify that the recover table is empty
    Assert.assertFalse(rc.recoverRepository.findByWorkflowId(Constant.workflowId).isPresent());

    // Verify that the stacktrace table is empty
    Assert.assertEquals(0, rc.stacktraceRepository.findByWorkflowId(Constant.workflowId).size());

    // Verify that the scheduler table is empty
    Assert.assertEquals(0, rc.schedulerRespository.findByStatus(SchedulerStatus.PROCESSED).size());
    Assert.assertEquals(0, rc.schedulerRespository.findByStatus(SchedulerStatus.TO_BE_PROCESSED).size());
  }
}

