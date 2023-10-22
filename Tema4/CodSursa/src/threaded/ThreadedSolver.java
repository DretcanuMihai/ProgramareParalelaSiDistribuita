package threaded;


import polynomial.entity.Polynomial;
import polynomial.entity.SequentialPolynomial;
import threaded.thread.ConsumerThread;
import threaded.thread.ProducerThread;

public class ThreadedSolver {

    private final int nrThreads;

    private final int nrFiles;

    public ThreadedSolver(int nrThreads, int nrFiles) {
        this.nrThreads = nrThreads;
        this.nrFiles = nrFiles;
    }

    /**
     * solves the polynomial problem with threads
     */
    public void solve() {
        CyclicCountingCondition finishedConsumingCondition = new CyclicCountingCondition(nrThreads - 1);

        ConcurrentQueue<String> queue = new ConcurrentQueue<>();
        Polynomial result = new SequentialPolynomial();
        try {
            Thread[] threads = new Thread[nrThreads];

            threads[0] = new ProducerThread(nrFiles, queue, finishedConsumingCondition, result);
            threads[0].start();
            for (int i = 1; i < nrThreads; i++) {
                threads[i] = new ConsumerThread(queue, finishedConsumingCondition, result);
                threads[i].start();
            }

            for (int i = 1; i < nrThreads; i++) {
                threads[i].join();
            }

            threads[0].join();
        } catch (Exception ignored) {
        }
    }
}
