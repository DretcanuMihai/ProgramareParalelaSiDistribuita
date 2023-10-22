package com.ppd.p1.repository;

import com.ppd.p1.model.dao.LocationTreatmentOffer;
import org.hibernate.SessionFactory;

public class LocTreatOfferRepository extends GenericCRUDRepository<LocationTreatmentOffer, Integer> {

    public LocTreatOfferRepository(SessionFactory sessionFactory) {
        super(sessionFactory, LocationTreatmentOffer.class);
    }
}
