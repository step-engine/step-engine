package dk.ngr.step.engine.domain.event;

import dk.ngr.step.engine.common.JsonUtil;
import dk.ngr.step.engine.common.StacktraceUtil;
import dk.ngr.step.engine.domain.event.DomainEvent;
import dk.ngr.step.engine.domain.error.WorkflowException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodInvoker {

  public <T> void invoke(Object obj, DomainEvent<T> event, List<? extends DomainEvent<T>> events) {
    try {
      Method method = obj.getClass().getMethod("handle", event.getClass(), List.class);
      method.invoke(obj, event, events);
    } catch (NoSuchMethodException e) {
      // note that the method is not called if not implemented in the class implementing the EventListener..
    } catch (IllegalAccessException | InvocationTargetException e) {
      String msg = String.format("Handler call failed. eventName=%s event=%s rootCause= %s",
              event.getClass().getName(),
              new JsonUtil().toString(event),
              StacktraceUtil.getRootCauseFormattedMessage(e));
      throw new WorkflowException(msg, e);
    }
  }
}
