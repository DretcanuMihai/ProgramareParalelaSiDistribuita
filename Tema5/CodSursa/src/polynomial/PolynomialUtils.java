package polynomial;

import polynomial.entity.Polynomial;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PolynomialUtils {

    /**
     * extracts a monomial from a string (format is "coefficient exponent"
     *
     * @param line - said string
     * @return the monomial
     */
    static public Monomial monomialFromString(String line) {
        String[] monomialString = line.split(" ");
        return new Monomial(Integer.parseInt(monomialString[0]), Integer.parseInt(monomialString[1]));
    }

    /**
     * writes a polynomial to a file
     *
     * @param polynomial said polynomial
     * @param filename   said file
     * @throws IOException if any problem occurs while writing
     */
    static public void writePolynomialToFile(Polynomial polynomial, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        polynomial.forEach(monomial -> {
            try {
                writer.write(monomial.getCoefficient() + " " + monomial.getExponent());
                writer.newLine();
                writer.flush();
            } catch (IOException ignored) {
            }
        });
        writer.close();
    }
}
