package edu.wz.completedfuture;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

public class CompletableFutureTest {

    @Test
    public void testSimpleCompletableFuture() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CompletableFuture<Double> doubleCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return 36.6;
        }, executorService);

        Double result = doubleCompletableFuture.get();
        assertThat(result).isEqualTo(36.6);
    }

    @Test
    public void testCompletableFutureChaining() throws ExecutionException, InterruptedException {
        CompletableFuture<String> requestRes = CompletableFuture
                .completedFuture("response body bla bla %payload%")
                .thenApply(s -> {
                    Pattern p = Pattern.compile("%(.*)%");
                    Matcher m = p.matcher(s);
                    if(m.find()){
                        return m.group(1);
                    }
                    else{
                        throw new RuntimeException("No matches were found");
                    }
                });

        assertThat(requestRes.get()).isEqualTo("payload");
    }

    /**
     * Likewise JS promises
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testCompletableFutureChainingExceptionHandling() throws ExecutionException, InterruptedException {
        CompletableFuture<String> requestRes = CompletableFuture
                .completedFuture("response body without pattern")
                .thenApply(s -> {
                    Matcher m = Pattern.compile("%(.*)%").matcher(s);
                    if(m.find())
                        return m.group(1);
                    else
                        throw new RuntimeException("No matches were found");
                })
                .handle((res, exc) -> {
                    if (exc == null) {
                        return res;
                    } else {
                        return exc.getMessage();
                    }
                });

        assertThat(requestRes.get()).contains("No matches were found");
    }

    /**
     * AllOf race condition waits until all the futures given are completed. If any of the futures ends with exception
     * get method called on the allOfFuture will throw ExecutionException, but rest of the futures will be completed
     * anyway. So one is able to retrieve data from them.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void allOfCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("Part 1");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            return "Part 2";
        });
        CompletableFuture<String> future3 = CompletableFuture.completedFuture("Part 3");
        CompletableFuture<String> future4 = CompletableFuture.failedFuture(new RuntimeException("Error occurred"));
        CompletableFuture<String> future5 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            return "Part 5";
        });

        CompletableFuture<Void> futuresPack1 = CompletableFuture.allOf(future1, future2, future3);
        CompletableFuture<Void> futuresPack2 = CompletableFuture.allOf(future5, future4);

        futuresPack1.get();
        Throwable exc = catchThrowable(() -> futuresPack2.get());

        assertThat(future1.join()).isEqualTo("Part 1");
        assertThat(future2.join()).isEqualTo("Part 2");
        assertThat(future3.join()).isEqualTo("Part 3");
        assertThat(future5.isDone()).isTrue();
        assertThat(exc).hasMessageContaining("Error occurred");
    }

    /**
     * AnyOf race condition waits the first of the given futures completes and interrupts the rest.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void anyOfCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            return "Part 1";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            return "Part 2";
        });
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            return "Part 2";
        });
        CompletableFuture<String> future4 = CompletableFuture.failedFuture(new RuntimeException("Error occurred"));
        CompletableFuture<String> future5 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Completed 5");
            return "Part 5";
        });


        CompletableFuture<Object> anyFuture1 = CompletableFuture.anyOf(future1, future2);
        CompletableFuture<Object> anyFuture2 = CompletableFuture.anyOf(future3, future4, future5);
        anyFuture1.get();
        Throwable exc = catchThrowable(() -> anyFuture2.get());

        assertThat(anyFuture1.join()).isEqualTo("Part 1");
        assertThat(future2.isDone()).isFalse();
        assertThat(future5.isDone()).isFalse();
        assertThat(exc).hasMessageContaining("Error occurred");

    }
}
