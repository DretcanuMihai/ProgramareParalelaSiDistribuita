import sequential.SequentialSolver;
import threaded.ThreadedSolver;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        long endTime;
        double totalTime;
        int nrProducers = Integer.parseInt(args[0]);
        int nrConsumers = Integer.parseInt(args[1]);
        int queueCapacity = Integer.parseInt(args[2]);
        int nrFiles = Integer.parseInt(args[3]);
//        int nrProducers = 2;
//        int nrConsumers = 3;
//        int queueCapacity = 30;
//        int nrFiles = 10;


        if ((nrProducers > 0) && (nrConsumers > 0)) {
            solveThreaded(nrProducers,nrConsumers,queueCapacity, nrFiles);
        } else {
            solveSequential(nrFiles);
        }

        endTime = System.nanoTime();
        totalTime = (double) (endTime - startTime) / 1E6;
        System.out.println("\n");
        System.out.println(totalTime);
    }

    /**
     * solves the polynomial problem sequentially
     *
     * @param nrFiles - the number of files with polynomials
     */
    private static void solveSequential(int nrFiles) throws IOException {
        SequentialSolver solver = new SequentialSolver(nrFiles);
        solver.solve();
    }

    /**
     * solves the polynomial problem with threads
     *
     * @param nrProducers - the number of producers to use
     * @param nrConsumers - the number of consumers to use
     * @param queueCapacity - the capacity of the queue
     * @param nrFiles   - the number of files with polynomials
     */
    private static void solveThreaded(int nrProducers, int nrConsumers, int queueCapacity, int nrFiles) {
        ThreadedSolver solver = new ThreadedSolver(nrProducers, nrConsumers, queueCapacity, nrFiles);
        solver.solve();
    }
}