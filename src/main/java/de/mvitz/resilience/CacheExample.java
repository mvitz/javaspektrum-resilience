package de.mvitz.resilience;

import io.github.resilience4j.cache.Cache;

import javax.cache.Caching;
import java.time.LocalTime;
import java.util.function.Function;

import static de.mvitz.resilience.Helpers.sleep;

public class CacheExample {

    public static void main(String[] args) {
        final Cache<String, String> cache = Cache.of(
                Caching.getCache("cache1", String.class, String.class));

        final Function<String, String> cachedSupplier =
                Cache.decorateSupplier(cache, () -> someMethod("Hallo"));

        System.out.println(cachedSupplier.apply("cacheKey1"));

        sleep(2);

        System.out.println(cachedSupplier.apply("cacheKey1"));
        System.out.println(cachedSupplier.apply("cacheKey2"));
    }

    static String someMethod(String input) {
        return input + " @ " + LocalTime.now();
    }
}
