package threaded;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentQueue<T> {

    private final int capacity;

    private final Object[] objects;

    private int size;

    private int writeIndex;

    private int readIndex;

    private final Lock lock;

    private final Condition canRead;

    private final Condition canWrite;

    public ConcurrentQueue(int capacity) {
        this.capacity = capacity;
        this.objects = new Object[capacity];
        this.size = 0;
        this.writeIndex = 0;
        this.readIndex = 0;
        this.lock = new ReentrantLock();
        this.canRead = lock.newCondition();
        this.canWrite = lock.newCondition();
    }

    /**
     * puts a object in the queue
     *
     * @param object said object
     */
    public void push(T object) {
        lock.lock();

        while (size == capacity) {
            try {
                this.canWrite.await();
            } catch (InterruptedException ignored) {
            }
        }

        objects[writeIndex] = object;
        writeIndex = (writeIndex + 1) % capacity;
        size++;

        canRead.signal();
        lock.unlock();
    }

    /**
     * takes an object from the queue
     *
     * @return said monomial or null if queue writing is finished
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        Object result;

        lock.lock();

        while (size == 0) {
            try {
                canRead.await();
            } catch (InterruptedException ignored) {

            }
        }

        result = objects[readIndex];
        readIndex = (readIndex + 1) % capacity;
        size--;

        canWrite.signal();
        lock.unlock();
        return (T) result;
    }
}
