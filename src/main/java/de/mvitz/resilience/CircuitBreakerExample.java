package de.mvitz.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.CheckedFunction1;

import java.time.Duration;

import static de.mvitz.resilience.Helpers.sleep;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

public class CircuitBreakerExample {

    public static void main(String[] args) {
        final CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindow(4, 2, COUNT_BASED)
                .failureRateThreshold(75)
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .permittedNumberOfCallsInHalfOpenState(2)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .build();
        final CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        final CircuitBreaker circuitBreaker = registry.circuitBreaker("breaker1");

        final CheckedFunction1<String, String> function =
                CircuitBreaker.decorateCheckedFunction(
                        circuitBreaker, CircuitBreakerExample::someMethod);

        execute(function, "Hallo");
        // Error -> Window [Error] -> CLOSED
        printStats(circuitBreaker, 1);

        execute(function, "Michael");
        // Success -> Window [Success, Error] -> CLOSED with 50% errors
        printStats(circuitBreaker, 2);

        execute(function, "Hallo");
        // Error -> Window [Error, Success, Error] -> CLOSED with 66% errors
        printStats(circuitBreaker, 3);

        execute(function, "Hallo");
        // Error -> Window [Error, Error, Success, Error] -> OPEN with 75% errors
        printStats(circuitBreaker, 4);

        execute(function, "Michael");
        // Not permitted -> Window [Error, Error, Success, Error] -> OPEN with 75% errors
        printStats(circuitBreaker, 5);

        sleep(2);
        // Window [] -> HALF_OPEN
        printStats(circuitBreaker, 5);

        execute(function, "Michael");
        // Success -> Window [Success] -> HALF_OPEN
        printStats(circuitBreaker, 6);

        execute(function, "Michael");
        // Success -> Window [Success, Success] -> CLOSED
        printStats(circuitBreaker, 7);

        execute(function, "Michael");
        // Success -> Window [Success] -> CLOSED
        printStats(circuitBreaker, 8);

        execute(function, "Michael");
        // Success -> Window [Success, Success] -> CLOSED with 0% errors
        printStats(circuitBreaker, 9);

        execute(function, "Hallo");
        // Error -> Window [Error, Success, Success] -> CLOSED with 33% errors
        printStats(circuitBreaker, 10);

        execute(function, "Hallo");
        // Error -> Window [Error, Error, Success, Success] -> CLOSED with 50% errors
        printStats(circuitBreaker, 11);

        execute(function, "Hallo");
        // Error -> Window [Error, Error, Error, Success] -> OPEN with 75% errors
        printStats(circuitBreaker, 12);
    }

    static String someMethod(String input) throws Exception {
        if ("Hallo".equals(input)) {
            throw new Exception("'Hallo' is not working");
        }
        return "'" + input + "' is working";
    }

    static void execute(CheckedFunction1<String, String> function, String input) {
        try {
            final String result = function.apply(input);
            System.out.println(result);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    static void printStats(CircuitBreaker circuitBreaker, int numberOfCalls) {
        System.out.println("State after call " + numberOfCalls + ": "
                + circuitBreaker.getState()
                + " with failure rate of " + circuitBreaker.getMetrics().getFailureRate()
                + " within " + circuitBreaker.getMetrics().getNumberOfBufferedCalls() + " calls");
    }
}
