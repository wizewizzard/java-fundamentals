package edu.wz.synchronizers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class SemaphoreTest {
    @Test
    public void testSemaphore() throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch businessDone = new CountDownLatch(10);
        Semaphore semaphore = new Semaphore(5);
        List<Driver> drivers = Stream
                .generate(() -> new Driver(semaphore, ThreadLocalRandom.current().nextInt(3000, 6000), businessDone))
                .limit(10)
                .toList();
        drivers.forEach(executorService::submit);

        boolean businessesDone = businessDone.await(30000, TimeUnit.MILLISECONDS);
        assertThat(businessesDone).isTrue();
    }

    private static class Driver implements Runnable{
        private final Semaphore semaphore;
        private final int timeForBusiness;
        private final CountDownLatch businessDone;

        public Driver(Semaphore semaphore, int timeForBusiness, CountDownLatch businessDone) {
            this.semaphore = semaphore;
            this.timeForBusiness = timeForBusiness;
            this.businessDone = businessDone;
        }

        @Override
        public void run() {
            try {
                log.info("Looking for a parking lot");
                semaphore.acquire();
                log.info("Parked, going for a business");
                Thread.sleep(timeForBusiness);
                log.info("Business completed, driving away");
                businessDone.countDown();
                semaphore.release();
            } catch (InterruptedException e) {
                log.error("I was interrupted", e);
            }
        }
    }
}
