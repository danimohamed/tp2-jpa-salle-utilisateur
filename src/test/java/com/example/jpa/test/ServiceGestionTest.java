package com.example.jpa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.service.ServiceGestion;
import com.example.jpa.util.JpaUtil;

/**
 * Tests de la persistance JPA/Hibernate sans serveur d'application ni EJB.
 * <p>
 * La base H2 est en memoire et survit a la fermeture de chaque
 * EntityManagerFactory ({@code DB_CLOSE_DELAY=-1}). Les tables sont donc
 * videes avant chaque test afin que les assertions restent deterministes,
 * quel que soit l'ordre d'execution choisi par JUnit.
 */
public class ServiceGestionTest {

    private ServiceGestion service;

    @Before
    public void setUp() {
        service = new ServiceGestion();
        effacerDonnees();
    }

    @After
    public void tearDown() {
        service.fermer();
    }

    @AfterClass
    public static void fermerContextePartage() {
        JpaUtil.shutdown();
    }

    private void effacerDonnees() {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Utilisateur").executeUpdate();
            em.createQuery("DELETE FROM Salle").executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void testCreationEtRechercheSalle() {
        Salle salle = new Salle("Salle Test", 20);
        service.ajouterSalle(salle);

        assertNotNull("L'identifiant doit etre genere par la base", salle.getId());

        List<Salle> salles = service.listerSalles();
        assertFalse(salles.isEmpty());
        assertEquals("Salle Test", salles.get(0).getNom());
        assertEquals(20, salles.get(0).getCapacite());
    }

    @Test
    public void testCreationUtilisateurAvecSalle() {
        Salle salle = new Salle("Salle B", 15);
        service.ajouterSalle(salle);

        Utilisateur utilisateur = new Utilisateur("Alice", "alice@example.com", salle);
        service.ajouterUtilisateur(utilisateur);

        List<Utilisateur> utilisateurs = service.listerUtilisateurs();
        assertFalse(utilisateurs.isEmpty());
        assertEquals("Alice", utilisateurs.get(0).getNom());
        assertEquals("Salle B", utilisateurs.get(0).getSalle().getNom());
    }

    @Test
    public void testRechercheUtilisateursParSalle() {
        Salle salle = new Salle("Salle C", 30);
        service.ajouterSalle(salle);

        Utilisateur u1 = new Utilisateur("Bob", "bob@example.com", salle);
        Utilisateur u2 = new Utilisateur("Carol", "carol@example.com", salle);
        service.ajouterUtilisateur(u1);
        service.ajouterUtilisateur(u2);

        List<Utilisateur> utilisateurs = service.utilisateursParSalle("Salle C");
        assertEquals(2, utilisateurs.size());
    }

    @Test
    public void testMiseAJourEtSuppression() {
        Salle salle = new Salle("Salle D", 10);
        service.ajouterSalle(salle);

        salle.setCapacite(25);
        service.mettreAJourSalle(salle);
        assertEquals(25, service.trouverSalle(salle.getId()).getCapacite());

        service.supprimerSalle(salle);
        assertEquals(0, service.listerSalles().size());
    }

    @Test
    public void testRequetesJpqlAvancees() {
        Salle petite = new Salle("Salle Petite", 8);
        Salle grande = new Salle("Salle Grande", 40);
        service.ajouterSalle(petite);
        service.ajouterSalle(grande);

        List<Salle> grandes = service.sallesAvecCapaciteMinimale(20);
        assertEquals(1, grandes.size());
        assertEquals("Salle Grande", grandes.get(0).getNom());

        service.ajouterUtilisateur(new Utilisateur("Jean Dupont", "jean@example.com", grande));
        service.ajouterUtilisateur(new Utilisateur("Sans Salle", "libre@example.com", null));

        assertEquals(1, service.utilisateursParNom("Jean").size());
        assertEquals(1, service.utilisateursSansSalle().size());
        assertTrue(service.sallesParNom("grande").size() == 1);
    }
}
