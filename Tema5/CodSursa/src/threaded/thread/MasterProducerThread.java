package threaded.thread;


import polynomial.PolynomialUtils;
import polynomial.entity.ConcurrentPolynomial;
import threaded.ConcurrentQueue;
import threaded.CyclicCountingCondition;

import java.io.IOException;

public class MasterProducerThread extends ProducerThread {
    private final CyclicCountingCondition finishedConsumingCondition;

    private final ConcurrentPolynomial result;

    public MasterProducerThread(int firstFileIndex, int lastFileIndex, ConcurrentQueue<String> queue,
                                CyclicCountingCondition finishedProducingCondition,
                                CyclicCountingCondition finishedConsumingCondition,
                                ConcurrentPolynomial result) {
        super(firstFileIndex, lastFileIndex, queue, finishedProducingCondition);
        this.finishedConsumingCondition = finishedConsumingCondition;
        this.result = result;
    }

    @Override
    public void run() {
        try {
            super.run();
            finishedProducingCondition.await();
            for (int i = 0; i < finishedConsumingCondition.getCapacity(); i++) {
                queue.push(null);
            }
            finishedConsumingCondition.await();
            PolynomialUtils.writePolynomialToFile(result, "out.txt");
        } catch (IOException ignored) {
        }
    }
}
