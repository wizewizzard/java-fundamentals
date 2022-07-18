package edu.wz.synchronizers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class ExchangerTest {
    @Test
    public void testExchanger() throws InterruptedException{
        ExecutorService executorService = Executors.newCachedThreadPool();
        String illegalCargo = "illegalCargo";
        String money = "30000";
        Exchanger<String> exchanger = new Exchanger<>();

        Suspect seller = new Suspect(illegalCargo, 1500, exchanger);
        Suspect buyer = new Suspect(money, 1000, exchanger);

        executorService.submit(buyer);
        executorService.submit(seller);

        //Police
        Thread.sleep(2000);
        executorService.shutdownNow();

        assertThat(seller.getCargo()).isEqualTo(money);
        assertThat(buyer.getCargo()).isEqualTo(illegalCargo);
    }

    private static class Suspect implements Runnable{
        @Getter
        private String cargo;
        private final int timeToDrive;
        private final Exchanger<String> exchanger;

        public Suspect(String cargo, int timeToDrive, Exchanger<String> exchanger) {
            this.exchanger = exchanger;
            this.timeToDrive = timeToDrive;
            this.cargo = cargo;
        }

        @Override
        public void run() {
            try {
                log.info("Driving to the spot");
                Thread.sleep(timeToDrive);
                cargo = exchanger.exchange(cargo);
                log.info("Deal is over driving back...");
                Thread.sleep(timeToDrive);
            } catch (InterruptedException e) {
                log.warn("Busted with: {}", cargo);
                throw new RuntimeException(e);
            }
        }
    }
}
