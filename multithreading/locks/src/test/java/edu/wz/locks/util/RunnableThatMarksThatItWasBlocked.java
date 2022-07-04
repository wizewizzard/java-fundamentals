package edu.wz.locks.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class RunnableThatMarksThatItWasBlocked implements Runnable{
    private boolean wasBlocked = false;

    private int readTimes = 0;

    private final ReadWriteLock readWriteLock;

    public int getReadTimes() {
        return readTimes;
    }

    public boolean wasItBlocked() {
        return wasBlocked;
    }

    public RunnableThatMarksThatItWasBlocked(ReadWriteLock readWriteLock) {
        this.readWriteLock = readWriteLock;
    }

    @Override
    public void run() {
        Lock readLock = readWriteLock.readLock();
        for(int i = 0; i < 10; i ++){
            try{
                Thread.sleep(100);
                boolean lockAcquired = readLock.tryLock();
                if(lockAcquired){
                    try {
                        readTimes++;
                        //blah blah reading...
                    }
                    finally {
                        readLock.unlock();
                    }
                }
                else{
                    this.wasBlocked = true;
                }
            }
            catch (InterruptedException ignored){

            }
        }

    }
}
