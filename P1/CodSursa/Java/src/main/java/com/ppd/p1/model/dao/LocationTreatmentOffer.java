package com.ppd.p1.model.dao;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ppd.p1.commons.Constants.NR_TIME_UNITS;

@Entity
@Table(name = "location_treatment_offers")
public class LocationTreatmentOffer extends Identifiable<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @OneToMany(mappedBy = "offer", fetch = FetchType.EAGER)
    private Set<Appointment> appointments;

    //schedule[k] = number of offers appointed in the k time spot
    @Transient
    private int[] schedule;

    @Transient
    private Lock lock = new ReentrantLock();

    public LocationTreatmentOffer() {
        schedule = new int[NR_TIME_UNITS];
        for (int i = 0; i < NR_TIME_UNITS; i++) {
            schedule[0] = 0;
        }
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<Appointment> appointments) {
        this.appointments = appointments;
    }

    public int[] getSchedule() {
        return schedule;
    }

    public void setSchedule(int[] schedule) {
        this.schedule = schedule;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean canAddAppointment(Appointment appointment) {
        int startTime = appointment.getTime();
        int endTime = startTime + treatment.getDuration();
        if (endTime > NR_TIME_UNITS) {
            return false;
        }
        for (int i = startTime; i < endTime; i++) {
            if (schedule[i] + 1 > capacity) {
                return false;
            }
        }
        return true;
    }

    public void addAppointment(Appointment appointment) {
        int startTime = appointment.getTime();
        int endTime = startTime + treatment.getDuration();
        for (int i = startTime; i < endTime; i++) {
            schedule[i]++;
        }
        appointments.add(appointment);
    }

    public void deleteAppointment(Appointment appointment) {
        int startTime = appointment.getTime();
        int endTime = startTime + treatment.getDuration();
        for (int i = startTime; i < endTime; i++) {
            schedule[i]--;
        }
        appointments.remove(appointment);
    }
}
