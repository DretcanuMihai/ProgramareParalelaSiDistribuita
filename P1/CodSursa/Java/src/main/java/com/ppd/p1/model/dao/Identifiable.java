package com.ppd.p1.model.dao;

import java.io.Serializable;
import java.util.Objects;

public abstract class Identifiable<ID extends Serializable> implements Serializable {

    public abstract ID getIdentifier();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationTreatmentOffer that = (LocationTreatmentOffer) o;
        return Objects.equals(getIdentifier(), that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier());
    }
}