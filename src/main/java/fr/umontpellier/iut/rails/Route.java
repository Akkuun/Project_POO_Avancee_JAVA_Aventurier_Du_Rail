package fr.umontpellier.iut.rails;

import java.util.ArrayList;
import java.util.HashMap;

public class Route {
    /**
     * Première extrémité
     */
    private Ville ville1;
    /**
     * Deuxième extrémité
     */
    private Ville ville2;
    /**
     * Nombre de segments
     */
    private int longueur;
    /**
     * CouleurWagon pour capturer la route (éventuellement GRIS, mais pas LOCOMOTIVE)
     */
    private CouleurWagon couleur;
    /**
     * Joueur qui a capturé la route (`null` si la route est encore à prendre)
     */
    private Joueur proprietaire;
    /**
     * Nom unique de la route. Ce nom est nécessaire pour résoudre l'ambiguïté entre les routes doubles
     * (voir la classe Plateau pour plus de clarté)
     */
    private String nom;
    //permet de savoir pour les classes filles quelle couleur est choisie ! pour les non grises c'est forcément = à couleur
    private CouleurWagon couleurChoisie = null;


    public Route(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        this.ville1 = ville1;
        this.ville2 = ville2;
        this.longueur = longueur;
        this.couleur = couleur;
        nom = ville1.getNom() + " - " + ville2.getNom();
        proprietaire = null;


    }

    public Ville getVille1() {
        return ville1;
    }

    public Ville getVille2() {
        return ville2;
    }

    public int getLongueur() {
        return longueur;
    }

    public CouleurWagon getCouleur() {
        return couleur;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getNom() {
        return nom;
    }

    public CouleurWagon getCouleurChoisie() {
        return couleurChoisie;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String toLog() {
        return String.format("<span class=\"route\">%s - %s</span>", ville1.getNom(), ville2.getNom());
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s)]", ville1, ville2, longueur, couleur);
    }

    /**
     * @return un objet simple représentant les informations de la route
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", getNom());
        if (proprietaire != null) {
            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }

    //gérer le cas des routes grises
    //gérer les cas des tunnels et féry
    //vérifie que le joueur a assez de cartes wagons
    // vérifie que le joueur a assez de wagons
    //on demande de cliquer sur les cartes qu'il veut défausser, il peut toujours passer et ainsi on lui rends les éventuelles
    //cartes défaussés et on le laisse choisir autre chose.
    public boolean prendreRoute(Joueur joueur) {
        joueur.log("Vous avez choisi de construire la route :\n" + getNom());
        int nbCartesQuilFautEncorePoser = longueur;
        String choix = "initialisation";
        String consigne;
        if (couleur != CouleurWagon.GRIS) {
            couleurChoisie = couleur;
        }
        while (nbCartesQuilFautEncorePoser > 0) {
            ArrayList<String> choixHorsBoutons = new ArrayList<>();//remet les choix à zéro à chaque tour
            if (joueur.getCartesWagon().contains(CouleurWagon.LOCOMOTIVE))
                choixHorsBoutons.add("LOCOMOTIVE");// du moment qu'il en à une il peut jouer des loco
            if (couleurChoisie == null) {//pour route grise
                consigne = "choisissez une carte";
                for (CouleurWagon carte : CouleurWagon.getCouleursSimples()) {
                    if (joueur.possede_n_fois_couleur_plus_loco(longueur, carte)) {
                        choixHorsBoutons.add(carte.name());
                    }
                }
            } else { //pour les routes colorées ou les grises une fois qu'on à fait un choix
                consigne = "choisissez une Locomotive ou une carte " + couleurChoisie;
                if (joueur.getCartesWagon().contains(couleurChoisie)) {
                    choixHorsBoutons.add(couleurChoisie.name());
                }
            }

            choix = joueur.choisir(consigne, choixHorsBoutons, new ArrayList<>(), true);

            if (choix.equals("")) {
                break;
            } else {
                if (couleurChoisie == null && !choix.equals("LOCOMOTIVE")) {
                    couleurChoisie = CouleurWagon.stringToCouleurWagon(choix);

                }
                joueur.utiliserCarterWagon(CouleurWagon.stringToCouleurWagon(choix));
                nbCartesQuilFautEncorePoser--;
            }

        }
        if (choix.equals("")) {
            joueur.log("vous avez abandonné la construction\nde la route " + nom + "\nchoisissez une autre action");
            return false;
        }
        return true;
    }


    public boolean joueur_a_assez_de_cartes_et_de_wagons_pour_construire(Joueur joueur) {
        if (joueur.getNbWagons() >= longueur) {
            if (couleur == CouleurWagon.GRIS) {
                for (CouleurWagon couleurCompare : CouleurWagon.getCouleursSimples()) {
                    int nbCarteMemeCouleur = 0; //pour route grise on regarde le nombre de carte de chaque couleur + loco, dès qu'on à assez on return true
                    for (CouleurWagon carte : joueur.getCartesWagon()) {
                        if (carte == couleurCompare || carte == CouleurWagon.LOCOMOTIVE) {
                            nbCarteMemeCouleur++;
                        }
                    }
                    if (nbCarteMemeCouleur >= longueur) {
                        return true;
                    }
                }
            } else { // on compte le nombre de cartes de la couleur requise + loco, on return true si on a assez
                int nbCartesCouleur = 0;
                for (CouleurWagon carte : joueur.getCartesWagon()) {
                    if (carte == couleur || carte == CouleurWagon.LOCOMOTIVE) {
                        nbCartesCouleur++;
                    }
                }
                if (nbCartesCouleur >= longueur) {
                    return true;
                } else {
                    joueur.log("il vous manques des cartes pour\n construire cette route");
                }
            }
        }
        return false;
    }

    public void construireRoute(Joueur joueur) {
        if (prendreRoute(joueur)) {
            joueur.confirmerCaptureRoute(longueur);
            proprietaire = joueur;
            joueur.ajouterPoint(getNbPoints());
        } else {
            joueur.annulerCaptureRoute();
            joueur.jouerTour();
        }
    }

    public int getNbPoints() {
        return switch (longueur) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 7;
            case 6 -> 15;
            case 8 -> 21;
            default -> 0;
        };
    }


    public boolean joueur_n_a_pas_deja_pris_sa_route_jumelle(Joueur joueur) {

        if (nom.charAt(nom.length() - 2) == '1') { //si c'est une route de type xxxx-xxxx(1)
            String nomchange = nom.replace('1', '2'); //on la transforme en xxxxx-xxxxx(2)
            Route routeChange = Route.StringToRoute(nomchange, joueur.getJeu()); // on créer une route qui va servir pour tester la précense de soeur dans la liste
            if (routeChange.getProprietaire() != joueur) {
                return true;
            }
            return false;

        } else if (nom.charAt(nom.length() - 2) == '2') {// si c'est une route de type xxxx - xxxx(2)
            String nomchange = nom.replace('2', '1');
            Route routeChange = Route.StringToRoute(nomchange, joueur.getJeu());
            if (routeChange.getProprietaire() != joueur) {
                return true;
            }
            return false;
        }

        return true; //la route n'appartient pas aux deux cas précédents duaphin
    }

    public static Route StringToRoute(String nomRoute, Jeu jeu) {
        for (Route route : jeu.getRoutes()) {
            if (route.getNom().equals(nomRoute)) {
                return route;
            }
        }
        return null;
    }


    public void setLongueur(int t) {
        longueur = t;
    }

}
