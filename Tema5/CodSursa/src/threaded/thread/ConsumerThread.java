package threaded.thread;


import polynomial.PolynomialUtils;
import polynomial.entity.ConcurrentPolynomial;
import threaded.ConcurrentQueue;
import threaded.CyclicCountingCondition;

public class ConsumerThread extends Thread {

    private final ConcurrentQueue<String> queue;

    private final ConcurrentPolynomial result;

    private final CyclicCountingCondition finishedConsumingCondition;

    public ConsumerThread(ConcurrentQueue<String> queue, CyclicCountingCondition finishedConsumingCondition, ConcurrentPolynomial result) {
        this.queue = queue;
        this.result = result;
        this.finishedConsumingCondition = finishedConsumingCondition;
    }

    @Override
    public void run() {
        String line = queue.pop();
        while (line != null) {
            result.add(PolynomialUtils.monomialFromString(line));
            line = queue.pop();
        }
        finishedConsumingCondition.signal();
    }
}
