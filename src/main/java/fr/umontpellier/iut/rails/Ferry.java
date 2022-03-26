package fr.umontpellier.iut.rails;

public class Ferry extends Route {
    /**
     * Nombre de locomotives qu'un joueur doit payer pour capturer le ferry
     */
    private int nbLocomotives;

    public Ferry(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur, int nbLocomotives) {
        super(ville1, ville2, longueur, couleur);
        this.nbLocomotives = nbLocomotives;
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s, %d)]", getVille1(), getVille2(), getLongueur(), getCouleur(),
                nbLocomotives);
    }

    @Override
    public boolean joueur_a_assez_de_cartes_et_de_wagons_pour_construire(Joueur joueur) {
        int nbLocomotivesJoueur = 0;
        for (CouleurWagon carte : joueur.getCartesWagon()) {
            if(carte == CouleurWagon.LOCOMOTIVE){nbLocomotivesJoueur ++;}
        }
        if(nbLocomotivesJoueur<nbLocomotives){joueur.log("vous n'avez pas assez de cartes Locomotives\nVous ne pouvez prendre ce ferry !");}
        return super.joueur_a_assez_de_cartes_et_de_wagons_pour_construire(joueur) && nbLocomotivesJoueur>=nbLocomotives;
    }
}
