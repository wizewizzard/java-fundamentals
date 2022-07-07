package edu.wz.producerconsumer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BufferWithCapacityUsingConditions<T> implements Buffer<T>{

    private static final int DEFAULT_CAPACITY = 10;
    private final Lock lock = new ReentrantLock();
    private final Condition writeCondition;
    private final Condition readCondition;
    private final int capacity;

    private final Queue<T> buffer;

    public BufferWithCapacityUsingConditions(){
        this(DEFAULT_CAPACITY);
    }

    public BufferWithCapacityUsingConditions(int capacity){
        if(capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than 0");
        this.capacity = capacity;
        buffer = new LinkedList<>();
        writeCondition = lock.newCondition();
        readCondition = lock.newCondition();
    }

    @Override
    public void putValue(T value) {
        try{
            lock.lock();
            while(buffer.size() == capacity){
                readCondition.await();
            }
            buffer.add(value);
            writeCondition.signal();
        }
        catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        finally{
            lock.unlock();
        }
    }

    @Override
    public T getValue() {
        try{
            lock.lock();
            while(buffer.size() == 0){
                writeCondition.await();
            }
            T value = buffer.poll();
            readCondition.signal();
            return value;
        }
        catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        finally{
            lock.unlock();
        }
    }
}
