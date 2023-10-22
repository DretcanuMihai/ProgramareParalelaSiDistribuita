package utils;

import java.io.*;
import java.util.Scanner;

public class MatrixIOUtils {

    /**
     * reads the current matrix from the scanner
     *
     * @param scanner - the scanner from which the matrix is read
     * @return said matrix
     */
    private static double[][] readMatrix(Scanner scanner) {
        int rows = scanner.nextInt();
        int columns = scanner.nextInt();

        double[][] matrix = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = scanner.nextDouble();
            }
        }

        return matrix;
    }

    /**
     * reads the necessary data for solving the problem from a given file
     *
     * @param filename said file's filename
     * @return a package containing the needed information (input, output and kernel matrix - the output matrix is just
     * declared, such that enough space is allocated for it)
     * @throws FileNotFoundException if the file can't be found
     */
    public static MatrixPackage readPackage(String filename) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filename));

        double[][] inputMatrix = readMatrix(fileScanner);

        int rows = inputMatrix.length;
        int columns = inputMatrix[0].length;
        double[][] outputMatrix = new double[rows][columns];

        double[][] kernelMatrix = readMatrix(fileScanner);

        fileScanner.close();

        return new MatrixPackage(inputMatrix, outputMatrix, kernelMatrix);
    }

    /**
     * writes a given matrix to a file
     *
     * @param filename the file's name
     * @param matrix   the matrix to be written
     * @throws IOException - if any error occurs when writing the file
     */
    public static void writeMatrix(String filename, double[][] matrix) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(matrix.length + " " + matrix[0].length);
            writer.newLine();
            for (double[] lines : matrix) {
                for (double element : lines) {
                    writer.write(element + " ");
                }
                writer.newLine();
            }
            writer.flush();
        }
    }

    /**
     * asserts that two files contain the same matrices
     *
     * @param filename1 - first file's name
     * @param filename2 - second file's name
     * @throws FileNotFoundException if the file couldn't be found
     */
    public static void assertContentsEqual(String filename1, String filename2) throws FileNotFoundException {
        Scanner input1 = new Scanner(new File(filename1));
        Scanner input2 = new Scanner(new File(filename2));

        double[][] matrix1 = readMatrix(input1);
        double[][] matrix2 = readMatrix(input2);

        assertTrue (matrix1.length == matrix2.length);
        assertTrue (matrix1[0].length == matrix2[0].length);
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                assertTrue (matrix1[i][j] == matrix2[i][j]);
            }
        }

        input1.close();
        input2.close();
    }

    /**
     * assertion function - it throws an exception if value is false
     * added it because I keep forgetting to put the assertion flag
     * @param value said value
     */
    private static void assertTrue(boolean value){
        if(!value){
            throw new RuntimeException("ASSERTION FAILED");
        }
    }
}
