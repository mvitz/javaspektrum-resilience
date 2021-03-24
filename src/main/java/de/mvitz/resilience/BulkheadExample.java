package de.mvitz.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.mvitz.resilience.Helpers.duration;
import static de.mvitz.resilience.Helpers.sleep;
import static java.time.LocalTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

public class BulkheadExample {

    public static void main(String[] args) {
        final BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(2)
                .maxWaitDuration(Duration.ofSeconds(4))
                .build();
        final BulkheadRegistry registry = BulkheadRegistry.of(config);

        final Bulkhead bulkhead = registry.bulkhead("bulkhead");

        for (int i = 0; i < 6; i++) {
            final int j = i;

            final Supplier<String> bulkheadedMethod =
                    Bulkhead.decorateSupplier(bulkhead, () -> someMethod(j));

            System.out.println("[" + j + "]: Executing @ " + now());
            new Thread(() -> {
                try {
                    System.out.println(bulkheadedMethod.get());
                } catch (BulkheadFullException e) {
                    System.out.println("[" + j + "]: Not permitted @ " + now());
                }
            }).start();
        }
    }

    static String someMethod(int j) {
        final LocalTime start = now();
        System.out.println("[" + j + "]: Starting @ " + start);

        sleep(3);

        final LocalTime finish = now();
        System.out.println("[" + j + "]: Finished @ " + finish + " -> took " + duration(start, finish) + "s");

        return "SomeMethod #" + j;
    }
}
