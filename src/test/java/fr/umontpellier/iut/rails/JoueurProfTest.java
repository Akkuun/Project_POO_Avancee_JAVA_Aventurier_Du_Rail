package fr.umontpellier.iut.rails;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JoueurProfTest {
    private IOJeu jeu;
    private Joueur joueur1;
    private Joueur joueur2;
    private Joueur joueur3;
    private Joueur joueur4;

    /**
     * Renvoie la route du jeu dont le nom est passé en argument
     * 
     * @param nom le nom de la route
     * @return la route du jeu dont le nom est passé en argument (ou null si aucune
     *         route ne correspond)
     */
    public Route getRouteParNom(String nom) {
        for (Route route : jeu.getRoutes()) {
            if (route.getNom().equals(nom)) {
                return route;
            }
        }
        return null;
    }

    /**
     * Renvoie la ville du jeu dont le nom est passé en argument
     * 
     * @param nom le nom de la ville
     * @return la ville du jeu dont le nom est passé en argument (ou null si aucune
     *         ville ne correspond)
     */
    public Ville getVilleParNom(String nom) {
        for (Ville ville : jeu.getVilles()) {
            if (ville.getNom().equals(nom)) {
                return ville;
            }
        }
        return null;
    }

    @BeforeEach
    void init() {
        jeu = new IOJeu(new String[] { "Guybrush", "Largo", "LeChuck", "Elaine" });
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

    @Test
    void testJouerTourPiocherDestinations() {
        Destination d1 = new Destination("Brest", "Marseille", 7);
        Destination d2 = new Destination("London", "Berlin", 7);
        Destination d3 = new Destination("Edinburgh", "Paris", 7);
        Destination d4 = new Destination("Amsterdam", "Pamplona", 7);
        Destination d5 = new Destination("Roma", "Smyrna", 8);

        // le joueur choisir de piocher des destinations, pioche les destinations d1,
        // d2, d3, choisit
        // de défausser d3 et passe (il garde donc d1 et d2)
        jeu.setInput("destinations", d3.getNom(), "");
        List<Destination> pileDestinations = jeu.getPileDestinations();
        pileDestinations.clear();
        pileDestinations.add(d1); // début de la liste : haut de la pile
        pileDestinations.add(d2);
        pileDestinations.add(d3);
        pileDestinations.add(d4);
        pileDestinations.add(d5); // fin de la liste : fond de la pile

        joueur1.jouerTour();
        assertEquals(d4, pileDestinations.get(0));
        assertEquals(d5, pileDestinations.get(1));
        assertEquals(d3, pileDestinations.get(2)); // d3 est remise en fond de pile
        assertEquals(2, joueur1.getDestinations().size());
        assertTrue(joueur1.getDestinations().contains(d1));
        assertTrue(joueur1.getDestinations().contains(d2));
    }

    @Test
    void testJouerTourCapturerRoute() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Brest - Pamplona", // coûte 4 ROSE (ne peut pas capturer)
                "Bruxelles - Frankfurt", // coûte 2 BLEU
                "BLEU", // ok
                "ROUGE", // ne convient pas pour une route de 2 BLEU
                "LOCOMOTIVE" // ok
        );

        joueur2.jouerTour();
        assertEquals(null, getRouteParNom("Brest - Pamplona").getProprietaire());
        assertEquals(joueur2, getRouteParNom("Bruxelles - Frankfurt").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.BLEU, CouleurWagon.ROUGE, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.BLEU,
                CouleurWagon.LOCOMOTIVE));
    }

    @Test
    void testJouerTourCapturerRoutePlusieursCouleursPossibles() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.VERT);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        jeu.setInput(
                "Marseille - Paris", // coûte 4 GRIS
                "VERT", // ne convient pas (pas possible de payer en VERT)
                "BLEU", // ok (paye tout en BLEU)
                "ROUGE", // ne convient pas car déjà payé BLEU
                "LOCOMOTIVE", // ok
                "BLEU", // ok
                "BLEU" // ok
        );

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Marseille - Paris").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.VERT,
                CouleurWagon.ROUGE, CouleurWagon.ROUGE, CouleurWagon.ROUGE,
                CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.BLEU,
                CouleurWagon.BLEU,
                CouleurWagon.BLEU,
                CouleurWagon.LOCOMOTIVE));
    }

    @Test
    void testJouerTourCapturerTunnelOK() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        // cartes qui seront piochées après avoir payé le prix initial du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.BLEU);
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);

        jeu.setInput(
                "Marseille - Zurich", // coûte 2 ROSE (tunnel)
                "ROSE", // ok
                "LOCOMOTIVE", // ok
                "ROSE" // coût supplémentaire du tunnel
        );

        joueur2.jouerTour();
        assertEquals(joueur2, getRouteParNom("Marseille - Zurich").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROUGE, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.ROSE,
                CouleurWagon.ROSE,
                CouleurWagon.LOCOMOTIVE,
                CouleurWagon.BLEU,
                CouleurWagon.ROSE,
                CouleurWagon.JAUNE));
    }

    @Test
    void testJouerTourCapturerTunnelImpossible() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        // cartes qui seront piochées après avoir payé le prix initial du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);
        jeu.getPileCartesWagon().add(0, CouleurWagon.BLEU);
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);

        jeu.setInput(
                "Marseille - Zurich", // coûte 2 ROSE (tunnel)
                "ROSE", // ok
                "LOCOMOTIVE" // ok, mais le joueur ne peut pas payer le coût supplémentaire
        );

        joueur2.jouerTour();
        assertEquals(null, getRouteParNom("Marseille - Zurich").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROSE, CouleurWagon.ROUGE, CouleurWagon.ROUGE, CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.ROSE,
                CouleurWagon.BLEU,
                CouleurWagon.JAUNE));
    }

    @Test
    void testJouerTourCapturerTunnelPasse() {
        List<CouleurWagon> cartesWagon = joueur2.getCartesWagon();
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROSE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.LOCOMOTIVE);

        // cartes qui seront piochées après avoir payé le prix initial du tunnel
        jeu.getPileCartesWagon().add(0, CouleurWagon.BLEU);
        jeu.getPileCartesWagon().add(0, CouleurWagon.JAUNE);
        jeu.getPileCartesWagon().add(0, CouleurWagon.ROSE);

        jeu.setInput(
                "Marseille - Zurich", // coûte 2 ROSE (tunnel)
                "ROSE", // ok
                "LOCOMOTIVE", // ok
                "" // le joueur pourrait payer mais choisit d'abandonner la capture
        );

        joueur2.jouerTour();
        assertEquals(null, getRouteParNom("Marseille - Zurich").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur2.getCartesWagon(),
                CouleurWagon.ROSE, CouleurWagon.ROSE, CouleurWagon.ROUGE, CouleurWagon.ROUGE,
                CouleurWagon.LOCOMOTIVE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.ROSE,
                CouleurWagon.BLEU,
                CouleurWagon.JAUNE));
    }

    @Test
    void jouerTourConstruireUneGare() {
        List<CouleurWagon> cartesWagon = joueur3.getCartesWagon();
        cartesWagon.add(CouleurWagon.VERT);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);

        jeu.setInput("Paris", "ROUGE");
        joueur3.jouerTour();

        assertEquals(joueur3, getVilleParNom("Paris").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur3.getCartesWagon(),
                CouleurWagon.VERT, CouleurWagon.BLEU, CouleurWagon.BLEU, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.ROUGE));
        assertEquals(2, joueur3.getNbGares());
    }

    @Test
    void jouerTourConstruireDeuxGares() {
        List<CouleurWagon> cartesWagon = joueur3.getCartesWagon();
        cartesWagon.add(CouleurWagon.VERT);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.BLEU);
        cartesWagon.add(CouleurWagon.ROUGE);
        cartesWagon.add(CouleurWagon.ROUGE);

        jeu.setInput("Paris", "ROUGE"); // premier tour, constuit une gare pour 1 carte
        joueur3.jouerTour();

        jeu.setInput("Madrid", "ROUGE", "BLEU", "BLEU"); // 2e tour, une gare pour 2 cartes
        joueur3.jouerTour();

        assertEquals(joueur3, getVilleParNom("Madrid").getProprietaire());
        assertTrue(TestUtils.contientExactement(
                joueur3.getCartesWagon(),
                CouleurWagon.VERT, CouleurWagon.ROUGE));
        assertTrue(TestUtils.contientExactement(
                jeu.getDefausseCartesWagon(),
                CouleurWagon.ROUGE, CouleurWagon.BLEU, CouleurWagon.BLEU));
        assertEquals(1, joueur3.getNbGares());
    }

}
