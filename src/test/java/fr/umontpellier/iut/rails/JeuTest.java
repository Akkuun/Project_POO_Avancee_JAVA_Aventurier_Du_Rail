package fr.umontpellier.iut.rails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JeuTest {
    private IOJeu jeu;

    @BeforeEach
    void init() {
        jeu = new IOJeu(new String[]{"Guybrush", "Largo", "LeChuck", "Elaine"});


    }

    @Test
    public void pioche_est_bien_mélangé() {
        Jeu jeu = new Jeu(new String[]{"1", "2", "3"});
        boolean toutes_pareilles = true;
        List<CouleurWagon> cartes = jeu.getPileCartesWagon();
        CouleurWagon première_carte = cartes.get(0);
        for (int i = 1; i < 12 && toutes_pareilles; i++) {
            toutes_pareilles = première_carte == cartes.get(i);
        }
        assertFalse(toutes_pareilles);
    }


    @Test
    void test_Fonction_Liste_Melanger() {

        List<CouleurWagon> ListeCarteWagon =jeu.getPileCartesWagon();
        List<CouleurWagon> carteMelange;
        ListeCarteWagon.add(CouleurWagon.BLEU);
        ListeCarteWagon.add(CouleurWagon.ROUGE);
        ListeCarteWagon.add(CouleurWagon.BLEU);
        ListeCarteWagon.add(CouleurWagon.BLANC);
        ListeCarteWagon.add(CouleurWagon.JAUNE);

        System.out.println(ListeCarteWagon);
        carteMelange = jeu.melangerList(ListeCarteWagon);
        System.out.println(carteMelange);
        assertFalse(carteMelange == ListeCarteWagon);

    }

    @Test
    public void test_piocherCarteWagon_si_aucune_carte_renvoi_null(){
        Jeu jeu = new Jeu(new String[]{"1", "2", "3"});
        jeu.piocher_n_Cartes_CarteWagon(jeu.getPileCartesWagon().size());
        assertNull(jeu.piocherCarteWagon());
    }

    @Test
    public void carte_enlevé_de_la_pile_quand_pioché(){
        Jeu jeu = new Jeu(new String[]{"1", "2", "3"});
        int taille_pile = jeu.getPileCartesWagon().size();
        jeu.piocherCarteWagon();
        assertEquals(taille_pile-1, jeu.getPileCartesWagon().size());
    }

    @Test
    public void carte_pioché_est_la_dernière_carte_de_la_pile(){
        Jeu jeu = new Jeu(new String[]{"1", "2", "3"});
        CouleurWagon carte_haut_de_pile = jeu.getPileCartesWagon().get(0);
        CouleurWagon carte_pioche = jeu.piocherCarteWagon();
        assertEquals(carte_haut_de_pile, carte_pioche);
    }


    @Test
    public void retirerCarteWagonVisible_remelange_les_cartes_si_trois_vagons(){
        Jeu jeu = new Jeu(new String[]{"1", "2", "3"});
        jeu.mettreTroisCarteVagonEtUneBlancheDansPileCarteVisible();
        jeu.retirerCarteWagonVisible(CouleurWagon.BLANC);
        int nbCarteWagon=0;
        for (CouleurWagon carte: jeu.getCartesWagonVisibles()) {
            if (carte == CouleurWagon.LOCOMOTIVE){
                nbCarteWagon++;
            }
        }
        assertFalse(nbCarteWagon>=3);
    }


}