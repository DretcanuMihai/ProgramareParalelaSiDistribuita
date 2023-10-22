package com.ppd.p1.repository;

import com.ppd.p1.model.dao.Identifiable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;

public class GenericCRUDRepository<E extends Identifiable<ID>, ID extends Serializable> {

    protected final SessionFactory sessionFactory;

    private final Class<E> type;

    public GenericCRUDRepository(SessionFactory sessionFactory, Class<E> type) {
        this.sessionFactory = sessionFactory;
        this.type = type;
    }

    /**
     * persists given entity
     * @param entity - said entity - if id is generated, it will reflect on this argument
     * @return the created entity if successful, null otherwise
     */
    public E create(E entity) {
        E toReturn = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.persist(entity);
                transaction.commit();
                toReturn = entity;
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (transaction != null)
                    transaction.rollback();
            }
        }
        return toReturn;
    }

    /**
     * reads an entity by id
     * @param id - said id
     * @return said entity or null if it doesn't exist
     */
    public E read(ID id) {
        E toReturn = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                E aux;
                transaction = session.beginTransaction();
                aux = session.get(type, id);
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

    /**
     * updates given entity
     * @param entity - said entity - modifications will also reflect on this argument
     * @return the updated entity if successful, null otherwise
     */
    public E update(E entity) {
        E toReturn = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.merge(entity);
                transaction.commit();
                toReturn = entity;
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (transaction != null)
                    transaction.rollback();
            }
        }
        return toReturn;
    }

    /**
     * deletes an entity by id
     * @param id - said id
     * @return deleted entity or null if deletion failed
     */
    public E delete(ID id) {
        E toReturn = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = null;
            try {
                E aux;
                transaction = session.beginTransaction();
                aux = session.get(type, id);
                session.remove(aux);
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
