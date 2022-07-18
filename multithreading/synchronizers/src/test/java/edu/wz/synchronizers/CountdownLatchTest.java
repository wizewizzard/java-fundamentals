package edu.wz.synchronizers;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

public class CountdownLatchTest {
    @Test
    public void countdownLatch() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        executorService.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
        });
        executorService.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
        });

        Future<String> secretFuture = executorService.submit(() -> {
            try {
                if(countDownLatch.await(2500, TimeUnit.MILLISECONDS)){
                    return "Bu";
                }
                else{
                    throw new RuntimeException("You are late...");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        String secret = secretFuture.get();

        assertThat(secret).isEqualTo("Bu");
    }

    @Test
    public void countdownLatchUnsuccessful() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        executorService.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
        });
        executorService.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            countDownLatch.countDown();
        });

        Future<String> secretFuture = executorService.submit(() -> {
            try {
                if(countDownLatch.await(1500, TimeUnit.MILLISECONDS)){
                    return "Bu";
                }
                else{
                    throw new RuntimeException("You are late...");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Exception exception = catchException( () -> secretFuture.get());

        assertThat(exception)
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("You are late...");
    }
}
