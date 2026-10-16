package com.example.jpa.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Utilitaire de persistance (bonne pratique du TP : une seule
 * {@link EntityManagerFactory} pour toute l'application, reutilisee pour
 * creer des {@link EntityManager} a duree de vie courte).
 */
public final class JpaUtil {

    private static final String UNITE_DE_PERSISTANCE = "gestionPU";

    private static EntityManagerFactory emf;

    private JpaUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(UNITE_DE_PERSISTANCE);
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
