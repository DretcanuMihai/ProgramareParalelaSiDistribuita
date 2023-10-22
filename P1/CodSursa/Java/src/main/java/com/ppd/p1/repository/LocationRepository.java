package com.ppd.p1.repository;

import com.ppd.p1.model.dao.Location;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.LinkedList;
import java.util.List;

public class LocationRepository extends GenericCRUDRepository<Location, Integer> {
    public LocationRepository(SessionFactory sessionFactory) {
        super(sessionFactory, Location.class);
    }

    public List<Location> readLocations() {
        List<Location> toReturn = new LinkedList<>();
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                List<Location> aux;
                transaction = session.beginTransaction();
                aux = session.createQuery("from Location", Location.class)
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
