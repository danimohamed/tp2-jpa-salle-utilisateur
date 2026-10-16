package com.example.jpa;

import java.util.List;

import com.example.jpa.entities.Salle;
import com.example.jpa.entities.Utilisateur;
import com.example.jpa.service.ServiceGestion;

/**
 * Programme de demonstration : illustre l'utilisation du service metier
 * avec une gestion manuelle des transactions (aucun EJB, aucun conteneur).
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        ServiceGestion service = new ServiceGestion();
        try {
            Salle amphi = new Salle("Amphi A", 120);
            Salle td = new Salle("TD 12", 25);
            service.ajouterSalle(amphi);
            service.ajouterSalle(td);

            service.ajouterUtilisateur(new Utilisateur("Alice", "alice@example.com", amphi));
            service.ajouterUtilisateur(new Utilisateur("Bob", "bob@example.com", td));

            System.out.println("--- Salles ---");
            for (Salle salle : service.listerSalles()) {
                System.out.println(salle);
            }

            System.out.println("--- Utilisateurs ---");
            for (Utilisateur utilisateur : service.listerUtilisateurs()) {
                System.out.println(utilisateur);
            }

            System.out.println("--- Salles de capacite >= 100 ---");
            for (Salle salle : service.sallesAvecCapaciteMinimale(100)) {
                System.out.println(salle);
            }

            System.out.println("--- Utilisateurs de la salle 'TD 12' ---");
            List<Utilisateur> occupants = service.utilisateursParSalle("TD 12");
            for (Utilisateur utilisateur : occupants) {
                System.out.println(utilisateur);
            }
        } finally {
            service.fermer();
        }
    }
}
