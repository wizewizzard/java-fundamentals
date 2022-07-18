package edu.wz.synchronizers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CyclicBarrierTest {
    @Test
    public void cyclicBarrierTest() throws BrokenBarrierException, InterruptedException, TimeoutException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        DummyWorker dummyWorker1 = new DummyWorker(1000, cyclicBarrier);
        DummyWorker dummyWorker2 = new DummyWorker(2000, cyclicBarrier);


        executorService.submit(dummyWorker1);
        executorService.submit(dummyWorker2);
        executorService.shutdown();

        cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
        cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
        Thread.sleep(100);
        executorService.shutdownNow();

        assertThat(dummyWorker1.getCounter()).isEqualTo(2);
        assertThat(dummyWorker2.getCounter()).isEqualTo(2);

    }

    @Test
    public void recalculateIfBarrierIsBroken() throws BrokenBarrierException, InterruptedException, TimeoutException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        DummyWorker dummyWorker1 = new DummyWorker(1000, cyclicBarrier);
        DummyWorker dummyWorker2 = new DummyWorker(2000, cyclicBarrier);


        executorService.submit(dummyWorker1);
        executorService.submit(dummyWorker2);

        executorService.shutdown();

        cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
        Thread.sleep(1500);
        cyclicBarrier.reset();
        cyclicBarrier.await(3000, TimeUnit.MILLISECONDS);
        Thread.sleep(100);
        executorService.shutdownNow();

        assertThat(dummyWorker1.getCounter()).isEqualTo(2);
        assertThat(dummyWorker2.getCounter()).isEqualTo(2);
    }

    @Slf4j
    static class DummyWorker implements Runnable{
        private final int delay;
        private final CyclicBarrier cyclicBarrier;
        @Getter
        private int counter;

        public DummyWorker(int delay, CyclicBarrier cyclicBarrier) {
            this.delay = delay;
            this.cyclicBarrier = cyclicBarrier;
            this.counter = 0;
        }

        @Override
        public void run() {
            while(!Thread.interrupted()){
                try {
                    log.info("Started iteration");
                    Thread.sleep(delay);
                    log.info("Stopped for a barrier");
                    cyclicBarrier.await();
                    counter++;
                    log.info("Leaped a barrier");
                }
                catch (BrokenBarrierException e){
                    log.info("A barrier was broken. Starting all over again");
                }
                catch (InterruptedException ignored) {
                }
            }
        }
    }
}
