package sequential;

import polynomial.Monomial;
import polynomial.PolynomialUtils;
import polynomial.entity.SequentialPolynomial;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SequentialSolver {
    private final int numberOfFiles;

    public SequentialSolver(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    /**
     * solves the sequential.polynomial problem sequentially
     *
     * @throws IOException if there are any errors with files i/o
     */
    public void solve() throws IOException {
        SequentialPolynomial result = new SequentialPolynomial();
        for (int i = 0; i < numberOfFiles; i++) {
            BufferedReader reader = new BufferedReader(new FileReader(i + ".txt"));
            String line = reader.readLine();
            while (line != null) {
                String[] monomialString = line.split(" ");
                Monomial monomial = new Monomial(Integer.parseInt(monomialString[0]), Integer.parseInt(monomialString[1]));
                result.add(monomial);
                line = reader.readLine();
            }
            reader.close();
        }
        PolynomialUtils.writePolynomialToFile(result, "out.txt");
    }
}
