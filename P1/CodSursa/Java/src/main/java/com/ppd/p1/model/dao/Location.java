package com.ppd.p1.model.dao;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "locations")
public class Location extends Identifiable<Integer> {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @OneToMany(mappedBy = "location", fetch = FetchType.EAGER)
    private Set<Payment> payments;

    public Location() {
    }

    @Override
    public Integer getIdentifier() {
        return id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<Payment> getPayments() {
        return payments;
    }

    public void setPayments(Set<Payment> payments) {
        this.payments = payments;
    }
}
