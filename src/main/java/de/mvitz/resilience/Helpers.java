package de.mvitz.resilience;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.util.concurrent.TimeUnit.SECONDS;

class Helpers {

    public static void sleep(int seconds) {
        try {
            SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static long duration(LocalTime start, LocalTime finish) {
        return Duration.between(start, finish).getSeconds();
    }
}
