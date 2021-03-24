package de.mvitz.resilience;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static de.mvitz.resilience.Helpers.sleep;

public class TimeLimiterExample {

    public static void main(String[] args) throws Exception {
        final TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .build();
        final TimeLimiterRegistry registry =
                TimeLimiterRegistry.of(config);

        final TimeLimiter timeLimiter =
                registry.timeLimiter("someMethod");

        final Callable<String> method =
                timeLimiter.decorateFutureSupplier(
                        () -> CompletableFuture.supplyAsync(
                                () -> someMethod("Success")));

        try {
            final String result = method.call();
            System.out.println("Result: " + result);
        } catch (TimeoutException e) {
            System.out.println("Timeout: " + e);
        }
    }

    static String someMethod(String result) {
        sleep(4);
        return result;
    }
}
