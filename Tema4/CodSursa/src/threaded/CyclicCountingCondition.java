package threaded;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicCountingCondition {

    private final int capacity;

    private int count;

    private final Lock lock;

    private final Condition finishedCounting;

    /**
     * creates a condition that holds after "capacity" signals were sent
     * if more than capacity signals are sent, the condition recycles
     * @param capacity said capacity
     */
    public CyclicCountingCondition(int capacity) {
        this.capacity = capacity;
        this.count = capacity;
        this.lock = new ReentrantLock();
        this.finishedCounting = lock.newCondition();
    }

    /**
     * awaits for the condition to hold
     */
    public void await() {
        lock.lock();
        while (count != 0) {
            try {
                finishedCounting.await();
            } catch (InterruptedException ignored) {
            }
        }
        lock.unlock();
    }

    /**
     * signals the condition that progress was made
     */
    public void signal() {
        lock.lock();
        count = (count - 1) % capacity;
        if (count == 0) {
            finishedCounting.signal();
        }
        lock.unlock();
    }

    /**
     * returns the capacity of the condition
     * @return said condition
     */
    public int getCapacity() {
        return capacity;
    }
}
