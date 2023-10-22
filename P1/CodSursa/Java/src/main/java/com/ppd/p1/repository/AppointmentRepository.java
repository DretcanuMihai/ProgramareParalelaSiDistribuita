package com.ppd.p1.repository;

import com.ppd.p1.model.dao.Appointment;
import org.hibernate.SessionFactory;

public class AppointmentRepository extends GenericCRUDRepository<Appointment, Integer> {
    public AppointmentRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Appointment.class);
    }
}
