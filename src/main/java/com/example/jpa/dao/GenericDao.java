package com.example.jpa.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * DAO generique : operations CRUD communes a toutes les entites.
 * <p>
 * Aucun conteneur EJB n'est utilise : les transactions sont gerees
 * manuellement via {@link EntityTransaction} et annulees en cas d'erreur.
 */
public class GenericDao<T> {

    private final Class<T> entityClass;
    protected EntityManager em;

    public GenericDao(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    public void create(T entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Erreur lors de la creation de l'entite : " + e.getMessage());
            throw e;
        }
    }

    public T find(Object id) {
        return em.find(entityClass, id);
    }

    public T update(T entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Erreur lors de la mise a jour de l'entite : " + e.getMessage());
            throw e;
        }
    }

    public void delete(T entity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(entity) ? entity : em.merge(entity));
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Erreur lors de la suppression de l'entite : " + e.getMessage());
            throw e;
        }
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                .getResultList();
    }
}
