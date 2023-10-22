package threaded;


import polynomial.entity.ConcurrentPolynomial;
import threaded.thread.ConsumerThread;
import threaded.thread.MasterProducerThread;
import threaded.thread.ProducerThread;

public class ThreadedSolver {

    private final int nrProducers;

    private final int nrConsumers;

    private final int queueCapacity;

    private final int nrFiles;

    public ThreadedSolver(int nrProducers, int nrConsumers, int queueCapacity, int nrFiles) {
        this.nrProducers = nrProducers;
        this.nrConsumers = nrConsumers;
        this.queueCapacity = queueCapacity;
        this.nrFiles = nrFiles;
    }

    /**
     * solves the polynomial problem with threads
     */
    public void solve() {
        CyclicCountingCondition finishedProducingCondition = new CyclicCountingCondition(nrProducers);
        CyclicCountingCondition cyclicCountingCondition = new CyclicCountingCondition(nrConsumers);

        ConcurrentQueue<String> queue = new ConcurrentQueue<>(queueCapacity);
        ConcurrentPolynomial result = new ConcurrentPolynomial();
        try {
            Thread[] producerThreads = new Thread[nrProducers];
            Thread[] consumerThreads = new Thread[nrConsumers];

            int whole = nrFiles / nrProducers;
            int rest = nrFiles % nrProducers;

            int start = 0;
            int end = whole;
            for (int i = 0; i < nrProducers; i++) {
                if (rest > 0) {
                    rest--;
                    end++;
                }
                if (i == 0) {
                    producerThreads[i] = new MasterProducerThread(start, end, queue, finishedProducingCondition,
                            cyclicCountingCondition, result);
                } else {
                    producerThreads[i] = new ProducerThread(start, end, queue, finishedProducingCondition);
                }
                producerThreads[i].start();
                start = end;
                end += whole;
            }
            for (int i = 0; i < nrConsumers; i++) {
                consumerThreads[i] = new ConsumerThread(queue, cyclicCountingCondition, result);
                consumerThreads[i].start();
            }

            for (int i = 1; i < nrProducers; i++) {
                producerThreads[i].join();
            }
            for (int i = 0; i < nrConsumers; i++) {
                consumerThreads[i].join();
            }
            producerThreads[0].join();
        } catch (Exception ignored) {
        }
    }
}
