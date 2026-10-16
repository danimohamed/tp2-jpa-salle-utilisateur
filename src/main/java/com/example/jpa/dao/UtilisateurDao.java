package com.example.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.example.jpa.entities.Utilisateur;

/**
 * DAO specialise pour l'entite {@link Utilisateur}.
 */
public class UtilisateurDao extends GenericDao<Utilisateur> {

    public UtilisateurDao(EntityManager em) {
        super(Utilisateur.class, em);
    }

    /**
     * Utilisateurs rattaches a la salle portant le nom indique (jointure implicite).
     */
    public List<Utilisateur> findBySalleNom(String nomSalle) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.salle.nom = :nomSalle", Utilisateur.class)
                .setParameter("nomSalle", nomSalle)
                .getResultList();
    }

    /**
     * Utilisateurs dont le nom contient la chaine fournie.
     */
    public List<Utilisateur> findByNomContaining(String fragment) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.nom LIKE :pattern", Utilisateur.class)
                .setParameter("pattern", "%" + fragment + "%")
                .getResultList();
    }

    /**
     * Utilisateurs sans salle affectee.
     */
    public List<Utilisateur> findSansSalle() {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.salle IS NULL", Utilisateur.class)
                .getResultList();
    }
}
