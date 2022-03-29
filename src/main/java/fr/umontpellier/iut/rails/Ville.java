package fr.umontpellier.iut.rails;

import java.util.HashMap;

public class Ville {
    /**
     * Nom complet de la ville
     */
    private String nom;
    /**
     * Joueur qui a construit une gare sur la ville (ou `null` si pas de gare)
     */
    private Joueur proprietaire;


    public Ville(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    @Override
    public String toString() {
        return nom;
    }

    public String toLog() {
        return String.format("<span class=\"ville\">%s</span>", nom);
    }

    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        if (proprietaire != null) {
            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }

    public void construireGare(Joueur joueur) {
        //jeu.getVilleFromChoix(choixCarte).setProprietaire(jeu.getJoueurCourant());
        setProprietaire(joueur);
        joueur.setNbGares(joueur.getNbGares()-1);
        joueur.ajouterPoint(-4); //on lui enlève les points de la gare qui n'est plus stocké

    }




}
