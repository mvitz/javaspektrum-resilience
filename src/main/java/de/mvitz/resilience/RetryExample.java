package de.mvitz.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.util.function.Function;

public class RetryExample {

    private static int tries = 0;

    public static void main(String[] args) {
        final RetryConfig config = RetryConfig.<String>custom()
                .maxAttempts(2)
                .failAfterMaxAttempts(true)
                .retryExceptions(RuntimeException.class)
                .retryOnResult(string -> string.endsWith("4"))
                .build();
        final RetryRegistry registry =
                RetryRegistry.of(config);

        final Retry retry =
                registry.retry("someMethod");

        final Function<String, String> function =
                Retry.decorateFunction(retry, RetryExample::someMethod);

        final String result = function.apply("Succeeded after try: ");
        System.out.println(result);
    }

    static String someMethod(String prefix) {
        tries++;
        switch (tries) {
            case 1:
                throw new IllegalStateException("First try");
            case 2:
                throw new IllegalStateException("Second try");
            case 3:
                throw new RuntimeException("Third try");
            default:
                return prefix + tries;
        }
    }
}
