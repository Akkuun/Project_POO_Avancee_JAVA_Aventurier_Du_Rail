package fr.umontpellier.iut.rails;

import java.util.ArrayList;

public class Tunnel extends Route {
    boolean aCommenceAtirerLesCartes = false;

    public Tunnel(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {
        super(ville1, ville2, longueur, couleur);
    }

    @Override
    public boolean prendreRoute(Joueur joueur) {
        if (super.prendreRoute(joueur)) {
            joueur.log("truc de base ok");
            aCommenceAtirerLesCartes = true;
            joueur.log("vous tentez de capturer un tunnel\npiochons des cartes et laissons parler la chance");
            int nbCartesSupplementaire = 0;

            int nbCarteAPiocher = joueur.getJeu().getPileCartesWagon().size()+joueur.getJeu().getDefausseCartesWagon().size();
            for (CouleurWagon carte : joueur.getJeu().piocher_n_Cartes_CarteWagon(Math.min(nbCarteAPiocher, 3))) {//après avoir utilisé ses cartes normalement, on pioche trois cartes
                if (!a_ete_paye_tout_en_loco() && carte == getCouleurChoisie() || carte == CouleurWagon.LOCOMOTIVE) {
                    nbCartesSupplementaire++;
                }
                joueur.getJeu().defausserCarteWagon(carte);
            }
            joueur.log("boucle de pioche ok");
            Route verif = new Route(new Ville(""), new Ville(""), nbCartesSupplementaire, getCouleurChoisie());
            if (verif.joueur_a_assez_de_cartes_et_de_wagons_pour_construire(joueur)) {
                while (nbCartesSupplementaire > 0) {
                    ArrayList<String> choixHorsBoutons = new ArrayList<>();
                    if (joueur.getCartesWagon().contains(getCouleurChoisie()))
                        choixHorsBoutons.add(getCouleurChoisie().name());
                    if (joueur.getCartesWagon().contains(CouleurWagon.LOCOMOTIVE)) choixHorsBoutons.add("LOCOMOTIVE");
                    String choix = joueur.choisir("pas de chance il vous faut defausser " + nbCartesSupplementaire + " cartes de plus, payez ou abandonnez !", choixHorsBoutons, new ArrayList<>(), true);

                    if (choix.equals("")) {
                        return false;
                    }
                    joueur.utiliserCarterWagon(CouleurWagon.stringToCouleurWagon(choix));
                    nbCartesSupplementaire--;
                }
            }
            if (nbCartesSupplementaire == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void construireRoute(Joueur joueur) {
        if (prendreRoute(joueur)) {
            joueur.confirmerCaptureRoute(getLongueur());
            joueur.ajouterPoint(getNbPoints());
            setProprietaire(joueur);
        } else {
            joueur.annulerCaptureRoute();
            if (!aCommenceAtirerLesCartes) {
                joueur.jouerTour();
            }
        }
    }

    @Override
    public String toString() {
        return "[" + super.toString() + "]";
    }

}
