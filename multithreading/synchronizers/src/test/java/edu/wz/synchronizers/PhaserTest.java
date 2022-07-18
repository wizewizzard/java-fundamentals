package edu.wz.synchronizers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class PhaserTest {
    @Test
    public void testPhaser(){
        int requiredPhases = 7;
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Phaser phaser = new Phaser(){
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                log.info("Moving from phase {} to phase {}", phase, phase + 1);
                return super.onAdvance(phase, registeredParties);
            }
        };
        phaser.register();
        DummyWorkerForPhases dummyWorkerForPhases1 = new DummyWorkerForPhases(1000, phaser, List.of(1, 3, 5));
        DummyWorkerForPhases dummyWorkerForPhases2 = new DummyWorkerForPhases(3000, phaser, List.of(0, 5));
        executorService.submit(dummyWorkerForPhases1);
        executorService.submit(dummyWorkerForPhases2);

        log.info("Waiting for phase #{}", requiredPhases);
        while(phaser.arriveAndAwaitAdvance() < requiredPhases){
            ;
        }
        log.info("Phase #{} is reached!", requiredPhases);
        executorService.shutdownNow();

        assertThat(dummyWorkerForPhases1.getCounter()).isEqualTo(3);
        assertThat(dummyWorkerForPhases2.getCounter()).isEqualTo(2);
    }

    @Slf4j
    static class DummyWorkerForPhases implements Runnable{
        private final List<Integer> phases;
        private final int delay;
        private final Phaser phaser;
        @Getter
        private int counter;

        public DummyWorkerForPhases(int delay, Phaser phaser, List<Integer> phases) {
            phaser.register();
            this.delay = delay;
            this.phaser = phaser;
            this.counter = 0;
            this.phases = phases;
        }

        @Override
        public void run() {
            log.info("Started thread");
            try {
                while(!Thread.interrupted()){
                    if(phases.contains(phaser.getPhase())){
                        doWork();
                    }
                    phaser.arriveAndAwaitAdvance();
                }
            }
            catch (InterruptedException ignored) {
            }

        }

        private void doWork() throws InterruptedException {
            log.info(this + " started doing a work");
            Thread.sleep(delay);
            counter++;
            log.info(this + " work is over");
        }

        @Override
        public String toString() {
            return "DummyWorkerForPhases{" +
                    "phases=" + phases +
                    ", delay=" + delay +
                    '}';
        }
    }
}
