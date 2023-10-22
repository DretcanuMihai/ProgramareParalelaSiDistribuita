package polynomial.entity;

import polynomial.Monomial;

public interface Polynomial extends Iterable<Monomial>{

    /**
     * adds a monomial to the polynomial (as in actual addition, modifying the coefficients)
     *
     * @param monomial - said monomial
     */
    void add(Monomial monomial);
}
