import sequential.SequentialSolver;
import threaded.ThreadedSolver;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        long endTime;
        double totalTime;
        int nrThreads = Integer.parseInt(args[0]);
        int nrFiles = Integer.parseInt(args[1]);
//        int nrThreads = 4;
//        int nrFiles = 10;


        if (nrThreads > 0) {
            solveThreaded(nrThreads, nrFiles);
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
     * @param nrProducers   - the number of producers to use
     * @param nrFiles       - the number of files with polynomials
     */
    private static void solveThreaded(int nrProducers, int nrFiles) {
        ThreadedSolver solver = new ThreadedSolver(nrProducers, nrFiles);
        solver.solve();
    }
}