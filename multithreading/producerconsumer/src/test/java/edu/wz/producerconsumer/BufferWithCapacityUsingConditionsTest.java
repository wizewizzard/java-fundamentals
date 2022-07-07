package edu.wz.producerconsumer;

import edu.wz.producerconsumer.util.Producer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;

class BufferWithCapacityUsingConditionsTest {
    private Buffer<Integer> buffer;
    private ExecutorService executorService;
    private static List<Integer> preparedData1;
    private static List<Integer> preparedData2;

    @BeforeAll
    public static void prepareData(){
        preparedData1 = Stream
                .generate(() -> (int) (Math.random() * 100))
                .limit(20)
                .toList();

        preparedData2  = Stream
                .generate(() -> (int) (Math.random() * 100))
                .limit(20)
                .toList();
    }

    @BeforeEach
    public void setUp(){
        executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * Simple setup to test one producer one consumer situation. All values produced must be received by consumer
     */
    @Test
    public void oneConsumerOneProducer(){
        buffer = new BufferWithCapacityUsingConditions<>();
        Runnable p1 = new Producer<>(preparedData1, 100, buffer);

        Callable<List<Integer>> c1 = () -> {
            List<Integer> received = new ArrayList<>();
            while(received.size() < preparedData1.size()){
                received.add(buffer.getValue());
            }
            return received;
        };

        executorService.submit(p1);
        Future<List<Integer>> result = executorService.submit(c1);

        try{
            List<Integer> integerResultList = result.get(5000, TimeUnit.MILLISECONDS);
            assertThat(integerResultList).hasSize(preparedData1.size());
            assertThat(integerResultList).containsExactlyElementsOf(preparedData1);
        }
        catch (Exception e){
            fail("Exception fired: ", e);
        }
    }

    /**
     * All values produced by multiple producers must be consumed in time and be the same, but not in the same order.
     * @throws InterruptedException
     */
    @Test
    public void multipleConsumersMultipleProducers() throws InterruptedException {
        buffer = new BufferWithCapacityUsingConditions<>();
        Runnable p1 = new Producer<>(preparedData1, 100, buffer);
        Runnable p2 = new Producer<>(preparedData2, 100, buffer);

        final List<Integer> resultList = new ArrayList<>(preparedData1.size() + preparedData2.size());
        Runnable c1 = () -> {
            while(resultList.size() < preparedData1.size() + preparedData2.size()){
                Integer value = buffer.getValue();
                synchronized (resultList){
                    resultList.add(value);
                }
            }
        };
        executorService.submit(p1);
        executorService.submit(p2);
        Future<?> consumer1 = executorService.submit(c1);
        Future<?> consumer2 = executorService.submit(c1);
        Future<?> consumer3 = executorService.submit(c1);
        executorService.shutdown();
        if(!executorService.awaitTermination(10000, TimeUnit.MILLISECONDS)){
            consumer1.cancel(true);
            consumer2.cancel(true);
            consumer3.cancel(true);
        }

        assertThat(resultList).containsExactlyInAnyOrderElementsOf(
                Stream.concat(preparedData1.stream(),
                                preparedData2.stream())
                        .collect(Collectors.toList()));
    }

    /**
     * Situation when consumer is slower than producers
     */
    @Test
    public void fastProducersWaitSlowConsumer(){
        buffer = new BufferWithCapacityUsingConditions<>();
        Runnable p1 = new Producer<>(preparedData1, 50, buffer);
        Runnable p2 = new Producer<>(preparedData2, 20, buffer);
        Callable<List<Integer>> c1 = () -> {
            try{
                List<Integer> received = new ArrayList<>();
                while(received.size() < preparedData1.size() + preparedData2.size()){
                    received.add(buffer.getValue());
                    Thread.sleep(100);
                }
                return received;
            }
            catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        };

        executorService.submit(p1);
        executorService.submit(p2);
        Future<List<Integer>> result = executorService.submit(c1);

        try{
            List<Integer> integerResultList = result.get(5000, TimeUnit.MILLISECONDS);
            assertThat(integerResultList).containsExactlyInAnyOrderElementsOf(
                    Stream.concat(preparedData1.stream(),
                                    preparedData2.stream())
                            .collect(Collectors.toList()));
        }
        catch (Exception e){
            fail("Exception fired: ", e);
        }
    }

    /**
     * Test when buffer has ability to hold only one element
     */
    @Test
    public void bufferHasCapacityJustForOneElement(){
        buffer = new BufferWithCapacityUsingConditions<>(1);
        Runnable p1 = new Producer<>(preparedData1, 50, buffer);
        Runnable p2 = new Producer<>(preparedData2, 20, buffer);

        Callable<List<Integer>> c1 = () -> {
            try{
                List<Integer> received = new ArrayList<>();
                while(received.size() < preparedData1.size() + preparedData2.size()){
                    received.add(buffer.getValue());
                    Thread.sleep(100);
                }
                return received;
            }
            catch (InterruptedException e){
                throw new RuntimeException(e);
            }
        };

        executorService.submit(p1);
        executorService.submit(p2);
        Future<List<Integer>> result = executorService.submit(c1);

        try{
            List<Integer> integerResultList = result.get(5000, TimeUnit.MILLISECONDS);
            assertThat(integerResultList).containsExactlyInAnyOrderElementsOf(
                    Stream.concat(preparedData1.stream(),
                                    preparedData2.stream())
                            .collect(Collectors.toList()));
        }
        catch (Exception e){
            fail("Exception fired: ", e);
        }
    }
}