# Event based step engine
Event based step engine used to execute arbitrary workflow defined by developer.

Simply create:
* workflow class
* event store class
* aggregate class
* steps to execute
* domain events to publish

You decide which db to use. You can either use the provided db-clients or implement the repository interfaces yourself. See package:
```
dk.ngr.step.engine.repository
```
Prvovided db-clients are:
* cassandra-db-client.jar 

## Changelog
| Version  | Description |
| ------------- | ------------- |
| 1.0.2  | cassandra db repository implementation moved into cassandra-db-client. Repository interfaces now using domain model that should be mapped to local entities. | 
| 1.0.1  | unit test added for the step engine. Fixed bug when using the wait annotation. Removed DefaultEventStream, EventStream and EventStreamId.|
| 1.0.0  | first commit. |

## Terminology
The below are term that are or can be used with the step-engine. They are described briefly where more detail is added later on.

* Command - is a request of executing some operation with some input data. A command is named in *present tense* like "OrderCreate".
* Domain Event - a domain event is an expression that some business logic *has been* executed. Thus, a domain event is named in *past tense* (it really has happened).
* Event store - a simple table with domain events. The store is "append only".
* Event sourcing - a resource exposing an API that is able to append and load domain events. Note that the application domain model basically is represented by one column of type String (a list of domain events).
* Aggregate - some model designed by you having some state based on loaded events. For more info, see *Aggregate*.
* Mutating - the process of setting the aggregate during load of events.
* CQRS - command query responsibility segregation i.e. separating write from read. Write is critical and is around 5-10% of all requests. Read is not critical compared to write. See *Notes* for more. 
* Eventual consistency - means that when writing it is a request of writing and the assumption is that it will succeed eventually (asynchronous). See *Notes* for more. 
* Materialized View - simple *nosql* tables that fits a 1:1 relation from microservice to GUI for fast REST API read. See *Notes* for more.
* Caching - caching can be used but materialized views are recommended instead. Anyway it could be done using Redis which is a bit faster than both Apache Cassandra and Mongodb.
* Idempotent - normally it means that a writing endpoint can be called repeatedly without error e.g. data might just be overwritten. In the context of the step-engine the meaning is different since state is kept. See *Notes* for more.
* Message broker - a queue from where it is possible to consume messages from.
* Workflow id - a unique id for a workflow created with time based UUID.
* Application id - e.g. order number or some other unique business id from the command. Note that the workflow id is used whenever possible in the step engine, not the application id.

## Features
* automatic retry with max attempt.
* wait with timeout (eg. waiting for callback).
* manual retry for a particular workflow.
* manual retry for many workflows.
* full transparency in production since domain events are appended to the event store.
* workflows in error can easily be found.
* stack traces are written to database and can be found by workflow id.
* errors are event based too and also published (could make statistics).
* being backward compatible is about making new versions of events and handle them.
* adding new features it is about adding a new component implementing EventListener thu thus existing code is not changed.
* if-then-else logic for context disappear since event handlers are used.
* if-then-else logic for state check is gone since data to be processed is processed once thus state check is not needed again. In case of error, retry is performed by publishing the last (good) saved event to trigger the step that failed.
* all events are stored in the event store thus only one table exist for the whole domain model thus reducing development time. The storing and loading of events is provided through the event store API (step engine).
* event store repository logic is handled by the local event store class implementing the event listener i.e. business logic is separated from event store logic.
* all other repository CRUD calls can be done in handlers also since events are published based on business logic that has happened. This also separates business logic from repository logic.
* unit testing is about listening for events. Since the step code becomes much cleaner i.e. only about business logic, the unit test becomes much simpler (less mock setup).
* the library is light and near to the developer thus having full flexibility. Annotations are used on the step class.

## Next version
* throw exception on optional get().
* refactor SingleCollector.
* remove EventType id from DomainEvent and use the className for deserialization instead.
* remove streamVersion from DomainEvent (versioning the className is fine).
* add partition id feature for scaling (same principle as for Apache Kafka).
* remove the "error" state for severe error ("manual" is fine).
* add StepRecovered to ErrorListener (publish on successful retry).

## Command
A command i.e. a request with some data will trigger the execution of a workflow. The request can be received from a message broker or a REST API call. When receiving the command it should be stored with the status TO_BE_PROCSSED by some thread. This way commands can be received even if the step-engine is not running. Use the command repository:
```
dk.ngr.step.engine.repository.CommandRepository
```
to save the command:
```
  void save(Command command);
```
The execution of the command i.e. start execution of the workflow should be done by another thread. Get the commands with status TO_BE_PROCESSED by using:
```
List<Command> findByStatus(CommandStatus status);
```
Then map the command, or some part of the command, into a domain event named in past tense like for example:
```
CommandReceived
```
Then publish the event like for example:
```
EventPublisher.of().publish(
    new CommandReceived(
        workflowId,
        message.getOrderNumber(),
        message.getMobileNumber()));
```
Then let the workflow (listener) handle it. In other words, make a handler for that event in order to execute the first step.

### Status
After publishing the command status must be set to RUNNING. It can be done either directly or by making a StatusUpdater that implements the EventListener i.e. to handle domain events for which command status should be updated. The reason is that when the last event is published you know that the workflow is processed and thus the command status must be set to PROCESSED.

A StatusUpdater example where CommandReceived is triggering the workflow and where ServiceProviderNotified is the last event published:
```
public class StatusUpdater implements EventListener<DomainEvent> {
  ...
  public void handle(CommandReceived event, List<DomainEvent> events) {
    commandRepository.updateStatus(event.applicationId, Timestamp.now(), CommandStatus.RUNNING);
  }

  public void handle(ServiceProviderNotified event, List<DomainEvent> events) {
    commandRepository.updateStatus(event.applicationId, Timestamp.now(), CommandStatus.PROCESSED);
  }
}
```

### Note
You create the workflow id (UUID). Other command status values are described later e.g. WAIT, TIMEOUT, MANUAL and IGNORED.

## Workflow
The workflow class is the definition of the workflow. Here you specify domain event handlers that should execute next step based on receive of some domain event (trigger).

In other words:

> Publish a domain event in order to trigger the execution of a step.

The workflow will be able to listen for domain events by implementing this interface:
```
public interface EventListener<K extends DomainEvent> {
  void handle(K event, List<DomainEvent> events);
}
```
Implement a handler method for each domain event that should execute *next step*. Subscribe to the EventPublisher so that it can receive domain events. Again, to start your workflow simply publish the event that is handled for the step that you have defined as the first step.

Step classes are executed by the workflow class using the StepExecutor:
```
dk.ngr.step.engine.domain.StepExecutor 
```
In each handler make a step class object with input and then execute it using the StepExecutor. An example of a workflow class can be seen in (test):
```
dk.ngr.step.engine.application.workflow.listener.Workflow 
```

For info on making a step class, see *Step* below.

### Note
Note that all resources that are needed for the steps to execute are injected into the workflow class.

## EventStore
The EventStore class will append (write) all published domain events to the event store.

The purpose of the event store:
* transparency in production - you know exactly what business logic *has been* executed and especially what *has not* been executed. This is important both in successful -and error mode.
* being able to retry - retry is done by loading the events for a particular workflow id and then publish the last saved event. That will trigger the failing step again.
* continue workflow upon callback - all events are loaded for a particular workflow id before publishing defined trigger domain event (that will continue the workflow). Note that the workflow id is a callback id given to some external process (before waiting).

Let the EventStore class implements the *EventListener* interface so that domain events can be received. Upon receive append the event to the store using the EventRepository that is injected. Implement your own EventRepository using the below interface:
```
dk.ngr.step.engine.repository.EventRepository 
```
### Note
Because of the listener pattern, the logic of the EventStore is removed from the business logic which makes the code clean and the unit test much simpler. The Json format is used in the event store when writing and reading domain events.

### Reports, monitor and statistics
* reports - reports can be created from the event store. Make an event listener fetching, preparing and calculating the data that you need and then loop the whole event store with it. You can make as many reports you like. You could also listen for events runtime thus creating reports runtime for dashboard. Then create the report from the dashboard (pdf). Report on error handling can also be made i.e. the number of errors, the type of error and when it occurred. From one project experience, 95% of all errors were related to errant 3rd part services or invalid incoming data.
* monitor - domain events and error events are published and can be notified. Make an event listener that handle the particular event you're interested in. Then send the message using e.g. Kafka queue (recommended). For more, see *Error handling*.
* statistics - all history is saved about what has *happened* i.e. hidden business value can be revealed i.e. questions can be answered in the future. By history is meant domain events named by their business language i.e. created, updated and deleted. The contained data and especially delta's on updates. The timestamp when it occurred. Again, make event listeners that questions about when it occurred, the frequency, the amount, create the probability. It is a good idea to use percentiles instead of averages. Interpret the knowledge into dashboards showing graphs and charts based on these sorts of calculations.

## Aggregate
An aggregate is an object model that has the state of the workflow by mutating the history of events into a model of your choice. The preferred (and most simple) solution is to make a class representing all domain events as private members and then set each variable during mutate (loading).

In the below both *Mutate from memory* and *Mutate from event store* is described. An example for both can be seen here:
```
dk.ngr.step.engine.application.workflow.listener.Aggregate 
```

### Mutate aggregate from memory (normal)
This is used when being in a handler and need data from previous step e.g. you are going to execute step 3 in a workflow handler and need some data from the first domain event. Note that the EventListener receives all previous events also which are mutated into the aggregate. 

### Mutate aggregate from event store (callback)
When waiting for a callback the workflow is stopped until the callback arrives and "wakes up". At some point in time the callback arrives and a domain event is published to continue the workflow (triggering some defined step of your choice). It might be that data is needed from a previous domain event when executing that step. If so mutate the loaded events into the aggregate. Again, most likely future steps are going to need data from previous events i.e. remember to set state in the EventPublisher also (see below).

### Set state in EventPublisher (retry and callback)
After loading domain events they should be set unconditionally in the EventPublisher so that the list of all domain events are published in future handler calls (input parameter). This is done by calling
```
EventPublisher.setState(String applicationId, List<DomainEvent> events) 
```
Again, you should make this call in callback situations as described above. The call is made in the retry implementation also since all events are loaded. This is handled automatically by the step-engine in:
```
dk.ngr.step.engine.scheduler.retry.RetryService 
```

## Step
The step class should do this:
* execute business logic of your choice
* afterwards, publish a domain event in past tense that it *has happened* 

*Write* is critical i.e. you should design the step around write. It can be either an external call. It can fail for many reasons. The external service can be down. The data you provide as input for the call can be invalid. Somebody suddenly bumped the version. If writing data to own db it can be invalid. Specially if data coming from an external resource is bad. Regardless, when it fails, and it will, it should be able to *retry*.

An important rule to remember is that:
> If a step executes with success it will never execute again for the same workflow id. In some step, the same calls, with the same input parameters will only be called again for the same workflow id in case that step is in *retry mode*.

You should also try to have the business language in mind when designing steps. Is there a clean context or some kind of logical separation or semantics? Examples could be
* OrderCreateStep
* OrderReviewStep
* MobilePayRequestStep
* SmsSendStep

After executing the business logic each step should publish a domain event that will trigger the next step. From the examples above the domain events would be named:
* OrderCreated
* OrderReviewed
* MobilePayRequested
* SmsSended

Create the step class and let it implement the *dk.ngr.step.engine.domain.Executor* interface:
```
public interface Executor {
  void execute();
  DomainEvent event();
}
```

Implement the *execute* method i.e. implement the business logic and *then after* publish the domain event.

Annotate each step with either @Retry, @Manual or @Wait. Please see *Error handling* below.

### Note
From experience, 90% of all errors is because a 3rd part service call failed. 

## DomainEvent
A domain event is published to signal that some particular business logic has been executed. Thus, it is published after the business logic implementation. Publishing an event will trigger the execution of the next step. The developer design the domain event i.e. what it should be named, what data it should contain and what step (handler) it should trigger. Again, the naming will most likely reflect the name of the step but in *past tense*.

All domain events extends the abstract DomainEvent:
```
public abstract class DomainEvent {
  public UUID workflowId;
  public String applicationId;
  public long occuredOn;

  public DomainEvent(UUID workflowId, String applicationId, long occuredOn) {
    this.workflowId = workflowId;
    this. applicationId = applicationId;
    this.occuredOn = occuredOn;
  }

  public abstract void accept(EventListener<DomainEvent> eventListener, List<DomainEvent> events);
```

A domain event must implement *accept* allowing the EventListener to visit the object.

### EventListener
To listen for domain events simply implement the *dk.ngr.step.engine.domain.event.EventListener* interface:
```
public interface EventListener<K extends DomainEvent> {
  void handle(K event, List<DomainEvent> events);
}
```

Then implement handler methods with the given domain event as parameter to be handled. Remember to add (subscribe) the listener to the EventPublisher. Note that the MethodInvoker is used by the handler to call a handler for at particular domain event:
```
@Override
public void handle(DomainEvent event, List<DomainEvent> list) {
  MethodInvoker.invoke(this, event, list);
}
```

An important rule to remember is that:
> When published, a domain event is handled by all event listeners that subscribe to the EventPublisher. Note that the handler also receives all previous domain events. Use the list when mutation previous domain events into an aggregate.

Another important rule to remember is that:
> Adding a new EventListener does *not* change the implementation of existing EventListeners. It is just new functionality in another context based on the same data (domain events).

### Note
The domain event *context* is not mixed with the error *context*. Domain events are *good* and they are published on success. On error an *error event* is published and the publishing of domain events stops. It can be seen in the event store for a particular workflow id. You can of course express semantic errors. 

## Error Handling
Here are some of the errors that can occur:
* command can not be parsed.
* command data is invalid (semantic error).
* external system calls fail (eg. 401 Unauthorized, 403 Forbidden, 500 Internal Error etc. etc.).
* external systems return invalid data.
* code bug in your code.

### ErrorListener
All errors are published as events and handled by the ErrorHandler which implements the *dk.ngr.step.engine.domain.error.ErrorListener* interface:
```
public interface ErrorListener {
  default void handle(StepFailedRetry event) {}
  default void handle(StepFailedManual event) {}
  default void handle(StepFailedError event) {}
  default void handle(TimeoutEvent event) {}
}
```
Upon error the ErrorHandler will update these tables for a particular workflow id:
* command - status will be changed to e.g. RETRY.
* recover - on first attempt a record will be added with retry=0.
* scheduler - a record will be added for next retry. 
* stacktrace - a record will be added with the stacktrace.

It is described more in detail in *Tables* below.

#### Note
You can implement your own error handler if wanted for notify or alerting. Please see *Microservice error handler pattern* for more.

### Retry
When a step is throwing a runtime exception you can make it *retry* using the @Retry annotation.

```Retry
@Retry(maxAttempt = 3)
public class SmsStep implements Executor {

  @Override
  public void execute() {

    smsClient.send(..);

    EventPublisher.of().publish(new SmsSended(..));

  }
}
```

This will make the scheduler start retrying maxAttempt times based on the given retry strategy (decided by developer). After maxAttempt the workflow will go into manual mode i.e. it will stop. Using the annotation will let the step executor handle the exception. In case you need to handle a checked exception then catch it and throw a RuntimeException or RetryException. Note that you can override the default retry behaviour by throwing a ManualException (thus not retrying). It could be that you know for sure that the error will not be fixed externally. If the step is annotated with neither Retry nor Manual the workflow will be set to manual status. All workflows in retry mode can be seen in the command -and recover table. Note that the executor will catch any RuntimeException i.e. you will always be able to find a recover entity with a stacktrace for that particular workflow id.

#### Retry strategy
It is up to the developer to implement the retry strategy interface:
```
dk.ngr.step.engine.scheduler.retry.RetryStrategy 
public interface RetryStrategy {
  long get(int i, long now);
}
```
Based on number of attempt the implementation will return the time for retry. It could be linear or exponential.

### Manual
When a step is throwing a runtime exception you could also let the workflow go directly into *manual mode* using the @Manual annotation:

```Manual
@Manual
public class SmsStep implements Executor {

  @Override
  public void execute() {

    smsClient.send(..);

    EventPublisher.of().publish(new SmsSended(..));

  }
}
```

All workflows in manual can be seen in the command -and recover table. Note that the default manual behaviour can be overridden by throwing a RetryException if wanted. Note that only entries in the recover table in retry mode are retried.

### Timeout
The timeout event is handled by the ErrorHandler. It will set the command status to timeout. The application should also listen for that event by implementing the ErrorListener in order to handle the error. For more info, see Scheduler below.

### Daily use
After maxAttempt the workflow will enter manual mode. Normally you should be notified by the error with the workflow id. If so, lookup the workflow in the command, recover and stacktrace table by the workflow id. In case you're not notified by the error, find all workflows in manual mode from the command table. Having the workflow id lookup the stacktrace. Analyse the stacktrace. It might be that you know that some external service is up and running again or that returned data is valid. At that point you would like to do a manual retry. Then verify the command status for that workflow id. If status is PROCESSED then try to recover (retry) all. You can expose this basic functionality through a REST API. For more, see *step-engine REST API* below.

### Microservice error handler pattern
You can have several microservices processing workflows using the step-engine. It would be a time-consuming task to investigate every microservice looking for workflows in manual mode. Thus, make an ErrorHandler implementing the ErrorListener interface and let each microservice publish the error event to an *error topic* named e.g. error-handler-topic. The message could be like:
```
{
  workflowId: string,
  occuredAt: number,
  commandStatus: string,      // command status of workflow for that particular microservice
  message: string,
  url: string                 // link to step-engine REST API i.e. particular microservice with workflow in manual mode.
}
```
#### Error handler microservice
Consume those messages in an error-handler microservice. Upon receive, store the message in a table reflecting the above message (columns).

Then expose a REST API with this function:
* findByCommandStatus - return a list of messages (with link to particular microservice with errant workflow)

#### Error handler webapp
Make an error-handler webapp able to use this simple error-handler REST API. Here you can find all microservice having workflows in manual mode. Selecting one will then use the step-engine REST API of the particular microservice with the workflow in manual mode. The GUI should expose the functions seen in *step-engine REST API*. Instead of having a separate ErrorHandler webapp, an Admin page could be added to the existing webapp, but it is not recommended.

#### Step recovered (next version)
The step-engine knows when a step in retry mode is executed with success. Then after it could publish StepRecovered. The event is added to the ErrorListener and in the (or some) implementation of the ErrorListener a message (above) is sent to the error-handler-topic that the command status is now PROCESSED. That message is then consumed by the error-handler microservice and the status is changed for that particular workflow id.

## Scheduler
### Retry
When a step is failing a retry entry will be added to the scheduler table with the *executeAt* to some point in time. Eventually the entry will be executed by the scheduler. When making a manual retry (using the REST API) it is about adding an entry to the scheduler with executeAt "now". Then after the scheduler will just do the normal retry procedure. It is the same procedure for retrying all remaining workflows in manuel i.e. all entries will be appended to the scheduler in order to execute retry. The retry itself is done by loading the events for a particular workflow id and then publish the last saved event.

### Wait
After executing a step the workflow can be put into *wait mode* using the @Wait annotation on the step class.

```Wait
@Wait(timeoutInMilliseconds = 3600000, eventType = 3)
public class SmsStep implements Executor {

  @Override
  public void execute() {

    smsClient.send(..);

    EventPublisher.of().publish(new SmsSended(..));

  }
}
```

The above will make a wait entry in the scheduler with the given timeout and the event type to wait for. Then after the workflow will be put into wait status. When the logic that is waited for has happened eg. receiving a callback.. the event with the specified eventType should be published. This will trigger the workflow to continue executing the next step (that you have defined). The StatusUpdater (that you implement) should handle the event so that command status can be set to running.. and scheduler status to processed. Eventually your StatusUpdater will update the command to processed when the workflow is done. In case you do not publish the above eventType (in time).. the scheduler will publish a timeout event. This is handled in the ErrorHandler and will put the command into timeout. The scheduler will be set to processed. The application should listen for timeout event by implementing the ErrorListener so that the error can be handled (eg. send alert to dashboard).

## Tables
Db integration is needed to these tables:

* command
* event
* recover
* stacktrace
* scheduler
 
The integration is done by implementing the interfaces in:
```
dk.ngr.step.engine.repository
```

### Command
The below are status's of the command table, and how it should be used.
* TO_BE_PROCESSED - the command has been received but still not processed.
* RUNNING - command is being processed. 
* PROCESSED - the command has been processed.
* WAIT - the workflow is in wait mode (set automatically).
* TIMEOUT - a timeout has occurred (set automatically).
* RETRY - if step is annotated with @Retry and RuntimeException is thrown, status will be set to retry (automatically).
* MANUAL - after maxAttempt step execution will stop and status is set to manual (automatically).
* IGNORE - this status is set by developer. You might receive the same message and want to mark the workflow as ignored. 
* DUPLICATE - can also be used (set by developer). 

#### Note
Besides status the command table also contains the command received.

### Recover
See the description above. The status are:
* PROCESSED
* RETRY
* MANUAL

Besides the status, the recover table also contain the number of retry.

### Stacktrace
The stacktrace table should be seen as a 1:many table to the recover table. It will have a number of stacktraces for a give workflow id. When entering retry mode that fails all the time.. you will see a number of stacktraces (the initial call + maxAttempt). Eventually the workflow will enter manual mode.

### Scheduler
The scheduler can have status of:
* TO_BE_PROCESSED
* PROCESSED

Besides status the table contains info when to execute either retry or timeout. The retry is executed by the scheduler and on timeout a TimeoutEvent is published. Note that TimeoutEvent is handled in the default ErrorHandler, but the developer should also handle it in some error event listener in order to inform (so that someone can take action).

### Unit test
The unit test is using a memdb which implements the interfaces located in:
```
dk.ngr.step.engine.repository
```

### step-engine REST API
Expose the step-engine error handling through REST API. The most basic functions are:
* find command by status
* find command by workflowId
* find stackstraces by workflowId
* find scheduler entries by status
* find scheduler entry by workflowId
* retry workflow in manual state by workflowId
* retry all workflows in manual state

## Backward compatibility
Backward compatibility is handled by versioning the event class itself like e.g. OrderCreateReceived_V2. The new type will be handled respectively in all existing handlers, together with the old version of the domain event.

## Snapshot
This is not implemented.

## Performance
This is about writing i.e. CompletableFutures or native threads are not used in order to keep things simple. Everything is executed synchronous and blocking. The partition feature will be added in next version. Reading from materialized views i.e. 1:1 tables for GUI can use CompletableFutures (non-blocking) if wanted. Note that both https://micronaut.io/ and https://jooby.io/ integrates with https://netty.io/ for fast non-blocking https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html responses.

## Authors
The inspiration of this work came from these authors:
* Greg Young - has made many talks on CQRS and Eventsourcing.. coining CQRS.
* Eric Evans - Domain-Driven Design : Tackling Complexity in the Heart of Software.. coining "Domin Driven Design".
* Vaughn Vernon - "Implementing Domain-Driven Design" and "Domain-Driven Design Distilled".. explains event sourcing with appendix.
* Betrand Meyer - https://en.wikipedia.org/wiki/Bertrand_Meyer.. coining "return void on write" in Object-Oriented Software Construction.. but coins it in the late 80's.
* Robert C. Martin - "Clean Code - A Handbook of Agile Software Craftsmanship" and "Agile Principles, Patterns, and Practices in C#".. describes the principles behind SOLID.
