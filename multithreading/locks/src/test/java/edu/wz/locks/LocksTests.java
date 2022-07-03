package edu.wz.locks;

import edu.wz.locks.util.Counter;
import edu.wz.locks.util.RunnableThatMarksThatItWasBlocked;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.assertj.core.api.Assertions.*;

public class LocksTests {

    /**
     * Simple lock that allows to control a resource shared with multiple threads.
     * @result must be zero
     */
    @Test
    public void reentrantLockAllowsToCaptureSharedResource() throws InterruptedException {

        final Counter counter = new Counter();
        final int iterations = 1500;
        Lock lock = new ReentrantLock();

        Runnable increments = () -> {
            for(int i = 0; i< iterations; i ++){
                lock.lock();
                try{
                    counter.increment();
                }
                finally {
                    lock.unlock();
                }
            }
        };

        Runnable decrements = () -> {
            for(int i = 0; i< iterations; i ++){
                lock.lock();
                try{
                    counter.decrement();
                }
                finally {
                    lock.unlock();
                }
            }
        };

        Thread thread1 = new Thread(increments);
        Thread thread2 = new Thread(increments);
        Thread thread3 = new Thread(decrements);
        Thread thread4 = new Thread(decrements);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        assertThat(counter.getCount()).isEqualTo(0);
    }

    /**
     * Not a stable test. But in 90% it works correctly and demonstrates the idea I was after.
     * Occasionally it fails because there is no guarantees, that second thread will ALWAYS start after the first one.
     * Unless the first thread is joined with main thread before second starts, or second thread is used as a callback
     * in first thread there will be no guarantees.
     * @throws InterruptedException
     */
    @RepeatedTest(10)
    public void reentrantLockTryLockWithTimeoutButItExpires() throws InterruptedException {
        final Counter counter = new Counter();
        Lock lock = new ReentrantLock();

        Runnable r1 = () -> {
            lock.lock();
            try {
                Thread.sleep(500);
                counter.increment();
            } catch (InterruptedException ignored) {
            }
            finally {
                lock.unlock();
            }
        };

        Runnable r2 = () -> {
            try {
                boolean lockAcquired = lock.tryLock(300, TimeUnit.MICROSECONDS);
                if(lockAcquired){
                    try{
                        counter.decrement();
                    }
                    finally {
                        lock.unlock();
                    }
                }
            }
            catch (InterruptedException ignored){
            }
        };

        Thread t1 = new Thread(r1);
        t1.start();
        Thread t2 = new Thread(r2);
        t2.start();

        t1.join();
        t2.join();

        assertThat(counter.getCount()).isEqualTo(1);
    }

    /**
     * Division on read and write locks allows to access shared resource for reading for multiple threads at the same time
     * unless write operation is in progress.
     */
    @Test
    public void reentrantReadWriteLockBlocksAnyAccessToSharedResourceDuringWriteOperation() throws InterruptedException {

        final Counter counter = new Counter();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        Runnable writes = () -> {
            try{
                Thread.sleep(200);
                Lock writeLock = lock.writeLock();
                writeLock.lock();
                try{
                    Thread.sleep(500);
                    counter.increment();
                }
                finally {
                    writeLock.unlock();
                }
            }
            catch (InterruptedException ignored){

            }
        };

        RunnableThatMarksThatItWasBlocked reads = new RunnableThatMarksThatItWasBlocked(lock);

        Thread t1 = new Thread(writes);
        Thread t2 = new Thread(reads);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertThat(counter.getCount()).isEqualTo(1);
        assertThat(reads.wasItBlocked()).isTrue();


    }

}
