package org.step.engine.scheduler;

import org.step.engine.scheduler.retry.RetryStrategy;

public class DefaultRetryStrategy implements RetryStrategy {

    @Override
    public long get(int i, long now) {
        if (i == 0) {
            i += 1;
        }

        return now + (i*5);
    }
}
