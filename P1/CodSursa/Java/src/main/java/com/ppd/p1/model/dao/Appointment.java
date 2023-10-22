package com.ppd.p1.model.dao;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Entity
@Table(name = "appointments")
public class Appointment extends Identifiable<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_id", nullable = false)
    private LocationTreatmentOffer offer;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnp", nullable = false)
    private String cnp;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @Column(name = "date")
    private Date date;

    @Column(name = "time", nullable = false)
    private int time;

    @Column(name = "paid", nullable = false)
    private boolean paid;

    @Transient
    private boolean canceled;

    @Transient
    private Lock lock = new ReentrantLock();

    public Appointment() {
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

    public LocationTreatmentOffer getOffer() {
        return offer;
    }

    public void setOffer(LocationTreatmentOffer offer) {
        this.offer = offer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnp() {
        return cnp;
    }

    public void setCnp(String cnp) {
        this.cnp = cnp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
