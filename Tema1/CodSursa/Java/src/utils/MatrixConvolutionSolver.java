package utils;

public class MatrixConvolutionSolver {

    private final double[][] inputMatrix;

    private final double[][] outputMatrix;

    private final double[][] kernelMatrix;

    private final int start;

    private final int nrElements;

    /**
     * Creates an object that solves the matrix convolution problem for a matrix
     *
     * @param inputMatrix  - the input matrix
     * @param outputMatrix - the matrix in which the results will be saved
     * @param kernelMatrix - the kernel matrix for the convolution
     * @param start        - the index at which the calculations should start (as if the matrix is linearized)
     * @param nrElements   - the number of elements that should be considered for calculation (the calculations
     *                     will be done for the element [start, start+nrElements))
     */
    public MatrixConvolutionSolver(double[][] inputMatrix,
                                   double[][] outputMatrix,
                                   double[][] kernelMatrix,
                                   int start,
                                   int nrElements) {
        this.inputMatrix = inputMatrix;
        this.outputMatrix = outputMatrix;
        this.kernelMatrix = kernelMatrix;
        this.start = start;
        this.nrElements = nrElements;
    }

    /**
     * When this method is called, the convolution problem is solved and the result is saved in the outputMatrix given to
     * the constructor
     */
    public void solve() {

        //helpful metrics for the io matrices
        int ioRows = inputMatrix.length;
        int ioColumns = inputMatrix[0].length;
        int ioMaxRow = ioRows - 1;       //such that this calculation isn't done each time (needed for coordinate
        int ioMaxColumn = ioColumns - 1; //correction

        //helpful metrics for the kernel
        int kernelRows = kernelMatrix.length;
        int kernelColumns = kernelMatrix[0].length;

        //displacements metrics for moving along the kernel
        int rowDisplacement = kernelRows / 2;
        int columnDisplacement = kernelColumns / 2;

        //describe the current element's position
        int currentRow = start / ioColumns;
        int currentColumn = start % ioColumns;

        //variables used inside the while/for blocks
        //decided to put them here such that they are not declared for every iteration
        int remainingElements = nrElements;
        int currentCursorRow;    //the "cursor" describes the current element from the input matrix to be used in
        int currentCursorColumn; //calculations
        int currentKernelRow;
        int currentKernelColumn;
        double accumulator; //accumulates the value to be saved in the output matrix

        while (remainingElements > 0) {
            accumulator = 0;
            for (currentKernelRow = 0; currentKernelRow < kernelRows; currentKernelRow++) {
                currentCursorRow = moveCoordinate(currentRow, currentKernelRow, rowDisplacement, ioMaxRow);
                for (currentKernelColumn = 0; currentKernelColumn < kernelColumns; currentKernelColumn++) {
                    currentCursorColumn = moveCoordinate(currentColumn, currentKernelColumn, columnDisplacement, ioMaxColumn);
                    accumulator += kernelMatrix[currentKernelRow][currentKernelColumn]
                            * inputMatrix[currentCursorRow][currentCursorColumn];
                }
            }
            outputMatrix[currentRow][currentColumn] = accumulator;
            //move to the next element
            currentColumn++;
            //when the Column coordinate exits the bounds, we move to the next row
            if (currentColumn == ioColumns) {
                currentRow++;
                currentColumn = 0;
            }
            remainingElements--;
        }
    }

    /**
     * finds the correspondent for a kernel coordinate in the i/o matrix coordinates given a reference point
     *
     * @param currentCoordinate      - the reference points
     * @param kernelCoordinate       - the kernel coordinate
     * @param coordinateDisplacement - the displacement of the coordinate
     * @param upperBound             - the maximum value for the correspondent coordinate
     * @return the correct coordinate
     */
    private static int moveCoordinate(int currentCoordinate, int kernelCoordinate, int coordinateDisplacement, int upperBound) {
        int resultCoordinate;
        //calculate the position of the coordinate
        resultCoordinate = currentCoordinate + kernelCoordinate - coordinateDisplacement;
        //correct the coordinate if out of bounds
        if (resultCoordinate < 0) {
            resultCoordinate = 0;
        } else if (resultCoordinate > upperBound) {
            resultCoordinate = upperBound;
        }
        return resultCoordinate;
    }
}
