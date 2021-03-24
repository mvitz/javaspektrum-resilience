package de.mvitz.resilience;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class HystrixExample {

    public static void main(String[] args) {
        final String result = new MyCommand().execute();
        System.out.println("Result: " + result);
    }

    private static class MyCommand extends HystrixCommand<String> {

        private MyCommand() {
            super(Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey("MyGroup"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("MyCommand"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionTimeoutInMilliseconds(3000)));
        }

        @Override
        protected String run() {
            return "Success";
        }

        @Override
        protected String getFallback() {
            return "Fallback";
        }
    }
}
