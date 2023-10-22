package com.ppd.p1.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessingConditional {

    private int nrProcesses;

    private boolean canProcessNew;

    private final Lock lock;

    private final Condition canProcessCondition;

    private final Condition zeroCondition;

    public ProcessingConditional() {
        this.nrProcesses = 0;
        this.canProcessNew = false;
        this.lock = new ReentrantLock();
        this.canProcessCondition = lock.newCondition();
        this.zeroCondition = lock.newCondition();
    }

    /**
     * notifies that a new process started
     */
    public void notifyStart() {
        lock.lock();
        while (!canProcessNew) {
            try {
                canProcessCondition.await();
            } catch (InterruptedException ignored) {
            }
        }
        nrProcesses++;
        lock.unlock();
    }

    /**
     * notifies that a new process finished
     */
    public void notifyFinish() {
        lock.lock();
        nrProcesses--;
        if(nrProcesses ==0){
            zeroCondition.signal();
        }
        lock.unlock();
    }

    /**
     * blocks new processes from starting
     */
    public void blockNewStarts() {
        lock.lock();
        canProcessNew = false;
        lock.unlock();
    }

    /**
     * unblocks new processes from starting
     */
    public void unblockNewStarts() {
        lock.lock();
        canProcessNew = true;
        canProcessCondition.signalAll();
        lock.unlock();
    }

    /**
     * awaits for the number of active processes to be zero
     */
    public void awaitZero() {
        lock.lock();
        while (nrProcesses != 0) {
            try {
                zeroCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lock.unlock();
    }
}
