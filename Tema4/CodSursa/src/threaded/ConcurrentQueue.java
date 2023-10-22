package threaded;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentQueue<T> {

    private final Queue<T> objects;

    private final Lock lock;

    private final Condition canRead;

    public ConcurrentQueue() {
        this.objects = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.canRead = lock.newCondition();
    }

    /**
     * puts a object in the queue
     *
     * @param object said object
     */
    public void push(T object) {
        lock.lock();
        objects.add(object);
        canRead.signal();
        lock.unlock();
    }

    /**
     * takes an object from the queue
     *
     * @return said monomial or null if queue writing is finished
     */
    public T pop() {
        T result;

        lock.lock();

        while (objects.size() == 0) {
            try {
                canRead.await();
            } catch (InterruptedException ignored) {

            }
        }

        result = objects.remove();
        lock.unlock();
        return result;
    }
}
