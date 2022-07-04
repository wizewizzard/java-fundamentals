package edu.wz.producerconsumer.util;

import edu.wz.producerconsumer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Producer<T> implements Runnable{
    private final List<T> preparedData;
    private final int pause;
    private Buffer<T> buffer;

    @Override
    public void run() {
        for (T value : preparedData) {
            buffer.putValue(value);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
