package fr.umontpellier.iut.rails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

}