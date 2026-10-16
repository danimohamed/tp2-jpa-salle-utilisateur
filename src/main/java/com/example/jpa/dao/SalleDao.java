package com.example.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.example.jpa.entities.Salle;

/**
 * DAO specialise pour l'entite {@link Salle}.
 */
public class SalleDao extends GenericDao<Salle> {

    public SalleDao(EntityManager em) {
        super(Salle.class, em);
    }

    /**
     * Salles dont la capacite est au moins egale a la valeur fournie.
     */
    public List<Salle> findByCapaciteMin(int capaciteMin) {
        return em.createQuery("SELECT s FROM Salle s WHERE s.capacite >= :capaciteMin", Salle.class)
                .setParameter("capaciteMin", capaciteMin)
                .getResultList();
    }

    /**
     * Salles dont le nom contient la chaine fournie (recherche insensible a la casse).
     */
    public List<Salle> findByNomContaining(String fragment) {
        return em.createQuery("SELECT s FROM Salle s WHERE LOWER(s.nom) LIKE :pattern", Salle.class)
                .setParameter("pattern", "%" + fragment.toLowerCase() + "%")
                .getResultList();
    }
}
