package edu.wz.executorservices;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class ExecutorServicesTest {

    /**
     * Always shutdown the ES! And the best practice to combine it with awaitTermination and the shutDownNow method if
     * any threads left running. When shutDownNow is used it invokes InterruptedException in the running threads.
     * @throws InterruptedException
     */
    @Test
    public void executorServiceBasicMethods() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable durable = () -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        Collection<Callable<String>> callables = List.of(
                () -> "Hello",
                () -> "World"
        );
        List<Future<String>> futuresList = executorService.invokeAll(callables);
        List<String> strings = futuresList.stream().map(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        executorService.submit(durable);
        executorService.submit(durable);

        executorService.shutdown();
        Exception exc = catchException(() -> executorService.submit(durable));
        boolean termination1 = executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
        boolean termination2 = executorService.awaitTermination(500, TimeUnit.MILLISECONDS);

        assertThat(strings).containsExactlyInAnyOrder("Hello", "World");
        assertThat(exc.getClass()).isEqualTo(RejectedExecutionException.class);
        assertThat(termination1).isFalse();
        assertThat(termination2).isTrue();
    }

    /**
     * Calling a ThreadPoolExecutor allows to customise it starting from treads number to how it treats overflowing the queue.
     * By default the saturation policy is an AbortPolicy that throws a Rejected exception. But one can block the sibmitting thread
     * if he specifies the CallerRunsPolicy. Or even ignore the fact that task was not submitted with DiscardPolicy.
     * @throws InterruptedException
     */
    @Test
    public void customisingExecutorService() throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1),
                new ThreadPoolExecutor.CallerRunsPolicy());

        executorService.execute(() -> waitFor(300));
        executorService.submit(() -> waitFor(300));
        Exception exception1 = catchException( () -> executorService.submit(() -> waitFor(300)));
        Exception exception2 = catchException( () -> executorService.submit(() -> waitFor(300)));
        executorService.shutdown();

        boolean terminated = executorService.awaitTermination(1500, TimeUnit.MILLISECONDS);
        if(!terminated){
            executorService.shutdownNow();
        }

        assertThat(terminated).isTrue();
        assertThat(exception1).isNull();
        assertThat(exception2).isNull();

    }

    /**
     * The ScheduledExecutorService is an executor service as it may be seen from the naming that used for running tasks
     * according to some schedule. Those tasks can be deferred for some amount of time or repeatable, or both.
     * @throws InterruptedException
     */
    @Test
    public void scheduledExecutorService() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
        List<String> list = new CopyOnWriteArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if(counter.get() < 3){
                counter.getAndIncrement();
                list.add("Element " + counter.get());
            }
        }, 0L, 300L, TimeUnit.MILLISECONDS);
        scheduledExecutorService.schedule(() -> list.add("Meeee"), 1000, TimeUnit.MILLISECONDS);
        Thread.sleep(2000);
        scheduledExecutorService.shutdown();
        boolean terminated = scheduledExecutorService.awaitTermination(500, TimeUnit.MILLISECONDS);
        if(!terminated){
            System.out.println("asd");
            scheduledExecutorService.shutdownNow();
        }

        assertThat(list)
                .containsSubsequence("Element 1", "Element 2", "Element 3")
                .contains("Meeee");
    }

    private void waitFor(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
