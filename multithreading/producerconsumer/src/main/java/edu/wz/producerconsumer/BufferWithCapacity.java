package edu.wz.producerconsumer;

import java.util.LinkedList;
import java.util.Queue;

public class BufferWithCapacity <T> implements Buffer<T>{

    private static final int DEFAULT_CAPACITY = 10;
    private final Object lock = new Object();
    private final int capacity;

    private Queue<T> buffer;

    public BufferWithCapacity(){
        this(DEFAULT_CAPACITY);
    }

    public BufferWithCapacity(int capacity){
        if(capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than 0");
        this.capacity = capacity;
        buffer = new LinkedList<>();
    }

    @Override
    public void putValue(T value) {
        synchronized (lock){
            while(buffer.size() == capacity){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            buffer.add(value);
            lock.notifyAll();
        }
    }

    @Override
    public T getValue() {
        synchronized (lock){
            while(buffer.size() == 0){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            lock.notifyAll();
            return buffer.poll();
        }
    }
}
