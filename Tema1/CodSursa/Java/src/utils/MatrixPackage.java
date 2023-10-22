package utils;

/**
 * this class exists only to pass all three needed matrices at the same time (specifically returning
 * the matrices after they are read)
 */
public class MatrixPackage {

    public double[][] inputMatrix;

    public double[][] outputMatrix;

    public double[][] kernelMatrix;

    public MatrixPackage(double[][] inputMatrix, double[][] outputMatrix, double[][] kernelMatrix) {
        this.inputMatrix = inputMatrix;
        this.outputMatrix = outputMatrix;
        this.kernelMatrix = kernelMatrix;
    }
}
