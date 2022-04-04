package fr.umontpellier.iut.rails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JoueurTest {
    private IOJeu jeu;
    private Joueur joueur1;
    private Joueur joueur2;
    private Joueur joueur3;
    private Joueur joueur4;

    @BeforeEach
    void init() {
        jeu = new IOJeu(new String[]{"Guybrush", "Largo", "LeChuck", "Elaine"});
        List<Joueur> joueurs = jeu.getJoueurs();
        joueur1 = joueurs.get(0);
        joueur2 = joueurs.get(1);
        joueur3 = joueurs.get(2);
        joueur4 = joueurs.get(3);
        joueur1.getCartesWagon().clear();
        joueur2.getCartesWagon().clear();
        joueur3.getCartesWagon().clear();
        joueur4.getCartesWagon().clear();
    }


    @Test
    void testChoisirDestinations() {
        jeu.setInput("Athina - Angora (5)", "Frankfurt - Kobenhavn (5)");
        ArrayList<Destination> destinationsPossibles = new ArrayList<>();
        Destination d1 = new Destination("Athina", "Angora", 5);
        Destination d2 = new Destination("Budapest", "Sofia", 5);
        Destination d3 = new Destination("Frankfurt", "Kobenhavn", 5);
        Destination d4 = new Destination("Rostov", "Erzurum", 5);
        destinationsPossibles.add(d1);
        destinationsPossibles.add(d2);
        destinationsPossibles.add(d3);
        destinationsPossibles.add(d4);

        List<Destination> destinationsDefaussees = joueur1.choisirDestinations(destinationsPossibles, 2);
        assertEquals(2, joueur1.getDestinations().size());
        assertEquals(2, destinationsDefaussees.size());
        assertTrue(destinationsDefaussees.contains(d1));
        assertTrue(destinationsDefaussees.contains(d3));
        assertTrue(joueur1.getDestinations().contains(d2));
        assertTrue(joueur1.getDestinations().contains(d4));
    }


    @Test
    void testJouerTourPrendreCartesWagon() {
        jeu.setInput("GRIS", "ROUGE");

        // On met 5 cartes ROUGE dans les cartes wagon visibles
        List<CouleurWagon> cartesWagonVisibles = jeu.getCartesWagonVisibles();
        cartesWagonVisibles.clear();
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);
        cartesWagonVisibles.add(CouleurWagon.ROUGE);

        // On met VERT, BLEU, LOCOMOTIVE (haut de pile) dans la pile de cartes wagon
        List<CouleurWagon> pileCartesWagon = jeu.getPileCartesWagon();
        pileCartesWagon.add(0, CouleurWagon.BLEU);
        pileCartesWagon.add(0, CouleurWagon.LOCOMOTIVE);
        int nbCartesWagon = pileCartesWagon.size();

        joueur1.jouerTour();
        // le joueur devrait piocher la LOCOMOTIVE, prendre une carte ROUGE
        // puis le jeu devrait remettre une carte visible BLEU

        assertTrue(TestUtils.contientExactement(
                joueur1.getCartesWagon(),
                CouleurWagon.ROUGE,
                CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(
                cartesWagonVisibles,
                CouleurWagon.BLEU,
                CouleurWagon.ROUGE,
                CouleurWagon.ROUGE,
                CouleurWagon.ROUGE,
                CouleurWagon.ROUGE));
        assertEquals(nbCartesWagon - 2, pileCartesWagon.size());
    }




    /** nécessite la méthode setNbWagon **/
    @Test
    public void test_fin_de_partie() {
        joueur2.setNbWagon(3);
        joueur2.getCartesWagon().clear();
        joueur2.getCartesWagon().addAll(new ArrayList<CouleurWagon>(List.of(CouleurWagon.BLEU, CouleurWagon.BLEU)));
        jeu.setInput("", "", "", "", "");//les joueurs prennent toutes les destinations pui j1 passe son tour
        jeu.addInputTest("Bruxelles - Frankfurt", "BLEU", "BLEU");//J2 prends une route et passe donc à 1wagon
        jeu.addInputTest("", "", "");//J3,J4 et J1 passe son tour
        jeu.addInputTest("GRIS", "GRIS");//pour le tout dernier jouerTour(J2 pioche deux cartes)
        jeu.run();
        assertEquals(2, joueur2.getCartesWagon().size());//on verifie qu'il ait bien pu aller jusqu'au bout de son dernier tour
    }

    /**le joueurs doit réussir à placer ses trois gares, se retrouvant donc sans gare et à 0 points
     * Nécessite la méthode getVilleFromChoix() dans Jeu qui trouve la ville avec son nom, sinon commentez les tests qui l'utilisent*/
    @Test
    public void construire_trois_gares(){
        joueur2.getCartesWagon().addAll(new ArrayList<>(List.of(CouleurWagon.JAUNE, CouleurWagon.BLEU, CouleurWagon.BLEU, CouleurWagon.BLEU, CouleurWagon.LOCOMOTIVE, CouleurWagon.ROUGE, CouleurWagon.ROUGE)));
        jeu.setInput("Amsterdam", "BLEU", "Barcelona", "BLEU", "BLEU", "Marseille", "LOCOMOTIVE", "ROUGE", "ROUGE");
        joueur2.jouerTour();
        assertEquals(joueur2, jeu.getVilleFromChoix("Amsterdam").getProprietaire());
        joueur2.jouerTour();
        assertEquals(joueur2, jeu.getVilleFromChoix("Barcelona").getProprietaire());
        joueur2.jouerTour();
        assertEquals(joueur2, jeu.getVilleFromChoix("Marseille").getProprietaire());
        assertTrue(joueur2.getCartesWagon().contains(CouleurWagon.JAUNE)&&joueur2.getCartesWagon().size()==1);
        assertEquals(0, joueur2.getNbGares());
        assertEquals(0, joueur2.getScore());
    }

    @Test
    public void construire_gare_impossible_care_deja_prise(){
        jeu.getCartesWagonVisibles().clear();
        jeu.getCartesWagonVisibles().addAll(List.of(CouleurWagon.JAUNE, CouleurWagon.JAUNE, CouleurWagon.ROUGE, CouleurWagon.ROSE, CouleurWagon.LOCOMOTIVE));
        joueur2.getCartesWagon().addAll(new ArrayList<>(List.of(CouleurWagon.JAUNE)));
        joueur3.getCartesWagon().addAll(new ArrayList<>(List.of(CouleurWagon.JAUNE)));
        jeu.setInput("Amsterdam", "JAUNE", "Amsterdam", "JAUNE", "JAUNE");
        joueur2.jouerTour();//le j2 prends Amsterdam
        joueur3.jouerTour();//le j1 ne peut prendre Amsterdam et pioche donc deux cartes jaunes
        assertEquals(joueur2, jeu.getVilleFromChoix("Amsterdam").getProprietaire());
        assertEquals(2, joueur2.getNbGares());
        assertEquals(3, joueur3.getNbGares());
        assertTrue(joueur2.getCartesWagon().isEmpty());
        assertEquals(new ArrayList<CouleurWagon>(List.of(CouleurWagon.JAUNE, CouleurWagon.JAUNE, CouleurWagon.JAUNE)), joueur3.getCartesWagon());
    }


}




