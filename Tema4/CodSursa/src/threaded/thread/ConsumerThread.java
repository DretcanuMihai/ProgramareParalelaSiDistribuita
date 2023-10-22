package threaded.thread;


import polynomial.PolynomialUtils;
import polynomial.entity.Polynomial;
import threaded.ConcurrentQueue;
import threaded.CyclicCountingCondition;

public class ConsumerThread extends Thread {

    private final ConcurrentQueue<String> queue;

    private final Polynomial result;

    private final CyclicCountingCondition finishedConsumingCondition;

    public ConsumerThread(ConcurrentQueue<String> queue, CyclicCountingCondition finishedConsumingCondition, Polynomial result) {
        this.queue = queue;
        this.result = result;
        this.finishedConsumingCondition = finishedConsumingCondition;
    }

    @Override
    public void run() {
        String line = queue.pop();
        while (line != null) {
            synchronized (result) {
                result.add(PolynomialUtils.monomialFromString(line));
            }
            line = queue.pop();
        }
        finishedConsumingCondition.signal();
    }
}
