package edu.wz.completedfuture;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

public class CompletableFutureTest {
    @Test
    public void testSimpleCompletableFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<Double> doubleCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {
            }
            return 36.6;
        });

        Double result = doubleCompletableFuture.get();
        assertThat(result).isEqualTo(36.6);
    }
}
