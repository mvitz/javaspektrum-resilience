package de.mvitz.resilience;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static de.mvitz.resilience.Helpers.duration;
import static de.mvitz.resilience.Helpers.sleep;
import static java.time.LocalTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RateLimiterExample {

    public static void main(String[] args) {
        final RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(2)
                .limitRefreshPeriod(Duration.ofSeconds(5))
                .timeoutDuration(Duration.ofSeconds(6))
                .build();
        final RateLimiterRegistry registry = RateLimiterRegistry.of(config);

        final RateLimiter rateLimiter = registry.rateLimiter("someMethod");

        final Consumer<Integer> rateLimitedMethod =
                RateLimiter.decorateConsumer(rateLimiter, RateLimiterExample::someMethod);

        for (int i = 0; i < 6; i++) {
            final int j = i;

            System.out.println("[" + j + "]: Executing @ " + now());
            new Thread(() -> {
                try {
                    rateLimitedMethod.accept(j);
                } catch (RequestNotPermitted e) {
                    System.out.println("[" + j + "]: Not permitted @ " + now());
                }
            }).start();
        }
    }

    static void someMethod(int j) {
        final LocalTime start = now();
        System.out.println("[" + j + "]: Starting @ " + start);

        sleep(4);

        final LocalTime finish = now();
        System.out.println("[" + j + "]: Finished @ " + finish + " -> took " + duration(start, finish) + "s");
    }
}
