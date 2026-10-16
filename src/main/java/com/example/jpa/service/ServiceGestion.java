package com.example.jpa.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.example.jpa.dao.SalleDao;
import com.example.jpa.dao.UtilisateurDao;
import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;

/**
 * Couche metier : orchestre les DAO et porte la logique applicative.
 * <p>
 * Sans EJB, le service cree lui-meme l'{@link EntityManagerFactory} et
 * l'{@link EntityManager}. La methode {@link #fermer()} libere les ressources.
 */
public class ServiceGestion {

    private final EntityManagerFactory emf;
    private final EntityManager em;
    private final SalleDao salleDao;
    private final UtilisateurDao utilisateurDao;

    public ServiceGestion() {
        emf = Persistence.createEntityManagerFactory("gestionPU");
        em = emf.createEntityManager();
        salleDao = new SalleDao(em);
        utilisateurDao = new UtilisateurDao(em);
    }

    public void fermer() {
        if (em.isOpen()) {
            em.close();
        }
        if (emf.isOpen()) {
            emf.close();
        }
    }

    // ---------------------------------------------------------------- Salles

    public void ajouterSalle(Salle salle) {
        salleDao.create(salle);
    }

    public Salle trouverSalle(Long id) {
        return salleDao.find(id);
    }

    public Salle mettreAJourSalle(Salle salle) {
        return salleDao.update(salle);
    }

    public void supprimerSalle(Salle salle) {
        salleDao.delete(salle);
    }

    public List<Salle> listerSalles() {
        return salleDao.findAll();
    }

    public List<Salle> sallesAvecCapaciteMinimale(int capaciteMin) {
        return salleDao.findByCapaciteMin(capaciteMin);
    }

    public List<Salle> sallesParNom(String fragment) {
        return salleDao.findByNomContaining(fragment);
    }

    // ----------------------------------------------------------- Utilisateurs

    public void ajouterUtilisateur(Utilisateur utilisateur) {
        utilisateurDao.create(utilisateur);
    }

    public Utilisateur trouverUtilisateur(Long id) {
        return utilisateurDao.find(id);
    }

    public Utilisateur mettreAJourUtilisateur(Utilisateur utilisateur) {
        return utilisateurDao.update(utilisateur);
    }

    public void supprimerUtilisateur(Utilisateur utilisateur) {
        utilisateurDao.delete(utilisateur);
    }

    public List<Utilisateur> listerUtilisateurs() {
        return utilisateurDao.findAll();
    }

    public List<Utilisateur> utilisateursParSalle(String nomSalle) {
        return utilisateurDao.findBySalleNom(nomSalle);
    }

    public List<Utilisateur> utilisateursParNom(String fragment) {
        return utilisateurDao.findByNomContaining(fragment);
    }

    public List<Utilisateur> utilisateursSansSalle() {
        return utilisateurDao.findSansSalle();
    }
}
