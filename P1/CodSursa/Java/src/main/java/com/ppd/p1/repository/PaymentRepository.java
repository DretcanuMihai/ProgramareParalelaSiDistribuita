package com.ppd.p1.repository;

import com.ppd.p1.model.dao.Payment;
import org.hibernate.SessionFactory;

public class PaymentRepository extends GenericCRUDRepository<Payment, Integer> {
    public PaymentRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Payment.class);
    }
}
