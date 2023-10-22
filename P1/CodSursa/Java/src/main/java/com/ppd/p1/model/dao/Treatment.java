package com.ppd.p1.model.dao;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;


@Entity
@Table(name = "treatments")
public class Treatment extends Identifiable<Integer> {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @Column(name = "price", nullable = false)
    private int price;

    //duration is in minutes
    @Column(name = "duration", nullable = false)
    private int duration;

    public Treatment() {
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
