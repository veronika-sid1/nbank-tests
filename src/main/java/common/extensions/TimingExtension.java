package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        String testId = context.getUniqueId();
        String testName = getTestName(context);

        startTimes.put(testId, System.currentTimeMillis());

        System.out.println("Thread " + Thread.currentThread().getName()
                + ": Test started " + testName);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        String testId = context.getUniqueId();
        String testName = getTestName(context);

        Long startTime = startTimes.remove(testId);

        if (startTime == null) {
            System.out.println("Thread " + Thread.currentThread().getName()
                    + ": Test finished " + testName
                    + ", test duration UNKNOWN ms");
            return;
        }

        long testDuration = System.currentTimeMillis() - startTime;

        System.out.println("Thread " + Thread.currentThread().getName()
                + ": Test finished " + testName
                + ", test duration " + testDuration + " ms");
    }

    private String getTestName(ExtensionContext context) {
        return context.getRequiredTestClass().getName()
                + "." + context.getRequiredTestMethod().getName();
    }
}