package fr.umontpellier.iut.rails;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JeuTest {

    @Test
    public void pioche_est_bien_mélangé(){
        Jeu jeu = new Jeu(new String[]{"1", "2", "3"});
        boolean toutes_pareilles = true;
        List<CouleurWagon> cartes = jeu.getPileCartesWagon();
        CouleurWagon première_carte = cartes.get(0);
        for (int i=1; i<12 && toutes_pareilles; i++){
            toutes_pareilles = première_carte==cartes.get(i);
        }
        assertFalse(toutes_pareilles);

    }

}