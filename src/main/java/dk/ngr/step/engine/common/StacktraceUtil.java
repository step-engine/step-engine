package dk.ngr.step.engine.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class StacktraceUtil {

    /**
     * @param throwable
     * 		throwable
     * @return
     * 		printStackTrace.
     */
    public static String getStackTrace(Throwable throwable) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * @param throwable
     * 		throwable
     * @return
     * 		"java.lang.ArithmeticException: / by zero" and the stacktrace if e.g. doing 1/0.
     */
    public static String getRootStackTrace(Throwable throwable) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable = getRootCause(throwable);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }

    /**
     * Get root cause (throwable) in stack trace chain.
     *
     * @param throwable
     * 		throwable
     * @return
     * 		throwable
     */
    public static Throwable getRootCause(Throwable throwable) {
        while (throwable.getCause() != null)
            throwable = throwable.getCause();
        return throwable;
    }

    /**
     * @param throwable
     * 		throwable
     * @return
     * 		"/ by zero" if e.g. doing 1/0.
     */
    public static String getRootCauseMessage(Throwable throwable) {
        while (throwable.getCause() != null)
            throwable = throwable.getCause();
        return throwable.getMessage();
    }

    /**
     * @param throwable
     * 		throwable
     * @return
     * 		"java.lang.ArithmeticException: / by zero" if e.g. doing 1/0.
     */
    public static String getRootCauseFormattedMessage(Throwable throwable) {
        while (throwable.getCause() != null)
            throwable = throwable.getCause();
        return new StringBuffer().append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).toString();
    }
}
