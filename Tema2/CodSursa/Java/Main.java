import threads.ConvolutionThread;
import utils.MatrixConvolutionSolver;
import utils.MatrixPackage;
import utils.MatrixIOUtils;

import java.util.concurrent.CyclicBarrier;

public class Main {

    private static final String INPUT_FILE_NAME = "date.txt";

    private static final String OUTPUT_FILE_NAME1 = "output1.txt";

    private static final String OUTPUT_FILE_NAME2 = "output2.txt";

    public static void main(String[] args) throws Exception {
        int nrThreads = Integer.parseInt(args[0]);

        MatrixPackage matrixPackage = MatrixIOUtils.readPackage(INPUT_FILE_NAME);
        double[][] inputMatrix = matrixPackage.inputMatrix;
        double[][] kernelMatrix = matrixPackage.kernelMatrix;

        long startTime, endTime;
        double threadedTime = 0, sequentialTime;

        System.out.println("\n");

        //execute the threaded solution
        if (nrThreads > 0) {
            startTime = System.nanoTime();
            solveThreaded(inputMatrix, kernelMatrix, nrThreads);
            endTime = System.nanoTime();

            threadedTime = (double) (endTime - startTime) / 1E6;
            MatrixIOUtils.writeMatrix(OUTPUT_FILE_NAME1, inputMatrix);
        }

        //execute the sequential solution
        matrixPackage = MatrixIOUtils.readPackage(INPUT_FILE_NAME);
        inputMatrix = matrixPackage.inputMatrix;
        kernelMatrix = matrixPackage.kernelMatrix;


        startTime = System.nanoTime();
        solveSequential(inputMatrix, kernelMatrix);
        endTime = System.nanoTime();

        sequentialTime = (double) (endTime - startTime) / 1E6;
        MatrixIOUtils.writeMatrix(OUTPUT_FILE_NAME2, inputMatrix);

        if (nrThreads > 0) {
            //compare the outputs before printing to the screen
            MatrixIOUtils.assertContentsEqual(OUTPUT_FILE_NAME1, OUTPUT_FILE_NAME2);
            System.out.println(threadedTime);
        } else {
            //if nrThreads is not positive, we want the sequential time
            System.out.println(sequentialTime);
        }
    }

    /**
     * solves the convolution problem sequentially, saving the result in the input matrix
     * because we only need the result of
     *
     * @param inputMatrix  - the input matrix
     * @param kernelMatrix - the kernel matrix
     */
    private static void solveSequential(double[][] inputMatrix, double[][] kernelMatrix) {
        int rows = inputMatrix.length;
        int columns = inputMatrix[0].length;
        int totalSize = rows * columns;
        MatrixConvolutionSolver solver = new MatrixConvolutionSolver(inputMatrix, kernelMatrix, 0, totalSize, null);
        solver.solve();
    }

    /**
     * solves the convolution problem with threads, saving the output to the input matrix
     *
     * @param inputMatrix  - the input matrix
     * @param kernelMatrix - the kernel matrix
     * @param nrThreads    - the number of threads to use
     * @throws InterruptedException if the threads fail
     */
    private static void solveThreaded(double[][] inputMatrix, double[][] kernelMatrix,
                                      int nrThreads) throws InterruptedException {
        int rows = inputMatrix.length;
        int columns = inputMatrix[0].length;
        int totalSize = rows * columns;

        int whole = totalSize / nrThreads;
        int rest = totalSize % nrThreads;

        int start = 0;
        int nrElements;

        Thread[] threads = new Thread[nrThreads];
        CyclicBarrier barrier=new CyclicBarrier(nrThreads);

        for (int i = 0; i < nrThreads; i++) {
            nrElements = whole;
            if (rest > 0) {
                nrElements++;
                rest--;
            }
            MatrixConvolutionSolver solver = new MatrixConvolutionSolver(inputMatrix, kernelMatrix, start, nrElements, barrier);
            threads[i] = new ConvolutionThread(solver);
            threads[i].start();
            start += nrElements;
        }
        for (int i = 0; i < nrThreads; i++) {
            threads[i].join();
        }
    }
}