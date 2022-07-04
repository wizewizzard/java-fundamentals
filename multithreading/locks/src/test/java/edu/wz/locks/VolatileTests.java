package edu.wz.locks;

import lombok.Getter;
import org.junit.jupiter.api.Test;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

public class VolatileTests {

    @Getter
    public static class VolatileCounter{
        private volatile int count;
        public void increment(){
            count++;
        }
        public void decrement(){
            count--;
        }
    }

    @Getter
    public static class AtomicCounter {

        private final AtomicInteger count = new AtomicInteger(0);

        public void increment(){
            count.getAndIncrement();
        }
        public void decrement(){
            count.getAndDecrement();
        }

    }

    /**
     * Volatile != atomic. Volatile only ensures that at every moment of time thread gets the latest possible value
     *  but volatile keyword does not make operations on marked shared resource atomic.
     * @throws InterruptedException
     */
    @Test
    public void makingVariableVolatileDoesNotMeanThatOperationsAgainstItAreAtomic() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(16);
        VolatileCounter volatileCounter = new VolatileCounter();
        final int times = 40000;
        Runnable increments = () -> {
            for(int i =0; i< times; i ++){
                volatileCounter.increment();
            }
        };

        executorService.submit(increments);
        executorService.submit(increments);
        executorService.submit(increments);

        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(2, TimeUnit.SECONDS);
        if(terminated){
            assertThat(volatileCounter.getCount()).isNotEqualTo(times * 3);
        }
        else{
            fail("Threads were not terminated by the expected time");
        }
    }

    /**
     * Atomic variables is the way to do it.
     * @throws InterruptedException
     */
    @Test
    public void atomicVariableRespects() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        AtomicCounter atomicCounter = new AtomicCounter();
        final int times = 10000;
        Runnable increments = () -> {
            for(int i =0; i< times; i ++){
                atomicCounter.increment();
            }
        };

        executorService.submit(increments);
        executorService.submit(increments);
        executorService.submit(increments);

        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(2, TimeUnit.SECONDS);
        if(terminated){
            assertThat(atomicCounter.getCount().get()).isEqualTo(times * 3);
        }
        else{
            fail("Threads were not terminated by the expected time");
        }
    }
}
