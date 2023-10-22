package com.ppd.p1.repository;

import com.ppd.p1.model.dao.Treatment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.LinkedList;
import java.util.List;

public class TreatmentRepository extends GenericCRUDRepository<Treatment, Integer> {
    public TreatmentRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Treatment.class);
    }

    public List<Treatment> readTreatments() {
        List<Treatment> toReturn = new LinkedList<>();
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                List<Treatment> aux;
                transaction = session.beginTransaction();
                aux = session.createQuery("from Treatment", Treatment.class)
                        .list();
                transaction.commit();
                toReturn = aux;
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (transaction != null)
                    transaction.rollback();
            }
        }
        return toReturn;
    }
}
