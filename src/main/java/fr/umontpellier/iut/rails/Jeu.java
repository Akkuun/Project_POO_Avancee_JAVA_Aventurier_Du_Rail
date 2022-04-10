package fr.umontpellier.iut.rails;

import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Jeu implements Runnable {
    /**
     * Liste des joueurs
     */
    private List<Joueur> joueurs;

    /**
     * Le joueur dont c'est le tour
     */
    private Joueur joueurCourant;
    /**
     * Liste des villes représentées sur le plateau de jeu
     */
    private List<Ville> villes;
    /**
     * Liste des routes du plateau de jeu
     */
    private List<Route> routes;
    /**
     * Pile de pioche (face cachée)
     */
    private List<CouleurWagon> pileCartesWagon;
    /**
     * Cartes de la pioche face visible (normalement il y a 5 cartes face visible)
     */
    private List<CouleurWagon> cartesWagonVisibles;
    /**
     * Pile de cartes qui ont été défaussée au cours de la partie
     */
    private List<CouleurWagon> defausseCartesWagon;
    /**
     * Pile des cartes "Destination" (uniquement les destinations "courtes", les
     * destinations "longues" sont distribuées au début de la partie et ne peuvent
     * plus être piochées après)
     */
    private List<Destination> pileDestinations;
    /**
     * File d'attente des instructions recues par le serveur
     */
    private BlockingQueue<String> inputQueue;
    /**
     * Messages d'information du jeu
     */
    private List<String> log;

    public Jeu(String[] nomJoueurs) {
        /*
         * ATTENTION : Cette méthode est à réécrire.
         *
         * Le code indiqué ici est un squelette minimum pour que le jeu se lance et que
         * l'interface graphique fonctionne.
         * Vous devez modifier ce code pour que les différents éléments du jeu soient
         * correctement initialisés.
         */

        // initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        // création des cartes

        pileCartesWagon = initialiser_nouvelle_pioche();
        cartesWagonVisibles = piocher_n_Cartes_CarteWagon(5);

        defausseCartesWagon = new ArrayList<>();
        pileDestinations = Destination.makeDestinationsEurope();
        Collections.shuffle(pileDestinations);


        // création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nom : nomJoueurs) {
            Joueur joueur = new Joueur(nom, this, couleurs.remove(0));

            joueurs.add(joueur);
        }
        joueurCourant = joueurs.get(0);

        //faire piocher les 4 cartes à tout les joueurs

        for (Joueur joueur : joueurs) {
            joueur.addCarteWagon(piocher_n_Cartes_CarteWagon(4));
        }

        // création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();
    }

    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    /**
     * Exécute la partie
     */
    public void run() {
        /*
         * ATTENTION : Cette méthode est à réécrire.
         *
         * Cette méthode doit :
         * - faire choisir à chaque joueur les destinations initiales qu'il souhaite
         * garder : on pioche 3 destinations "courtes" et 1 destination "longue", puis
         * le
         * joueur peut choisir des destinations à défausser ou passer s'il ne veut plus
         * en défausser. Il doit en garder au moins 2.
         * - exécuter la boucle principale du jeu qui fait jouer le tour de chaque
         * joueur à tour de rôle jusqu'à ce qu'un des joueurs n'ait plus que 2 wagons ou
         * moins
         * - exécuter encore un dernier tour de jeu pour chaque joueur après
         */

        /**
         * Le code proposé ici n'est qu'un exemple d'utilisation des méthodes pour
         * interagir avec l'utilisateur, il n'a rien à voir avec le code de la partie et
         * doit donc être entièrement réécrit.


         /********************* TOUR 0 || Distribution des cartes destination ************************************/
        ArrayList<Destination> destinationsLongues = Destination.makeDestinationsLonguesEurope();
        Collections.shuffle(destinationsLongues);

        ArrayList<Destination> destinationsPossibles= new ArrayList<>();
        for (Joueur joueur:joueurs) {
            joueurCourant=joueur;
            destinationsPossibles.clear();
            destinationsPossibles.add(piocherDestination());
            destinationsPossibles.add(piocherDestination());
            destinationsPossibles.add(piocherDestination());
            destinationsPossibles.add(destinationsLongues.remove(0)); //ajoute aussi une destination longue et on supprime une carte destination longue de la pioche
            joueurCourant.choisirDestinations(destinationsPossibles,2);
        }
        boolean jeuFini = false;
        boolean dernierTour = false;
        while (!jeuFini) {
            for (Joueur joueur : joueurs) {
                if (!jeuFini) {
                    if (dernierTour){log("dernier tour !");}
                    joueurCourant = joueur;
                    if (joueurCourant.getNbWagons() <= 2) {
                        jeuFini = true;
                    }
                    log("<strong>"+joueurCourant.getNom()+"</strong>");
                    joueurCourant.jouerTour();
                    if (joueurCourant.getNbWagons() <= 2) {
                        dernierTour = true;
                    }
                }
            }
        }
        log.clear();
        log("<h2>fin du jeu !</h2>");
        prompt("Fin de partie", new ArrayList<>(), false);
    }

    /**
     * Ajoute une carte dans la pile de défausse.
     * Dans le cas peu probable, où il y a moins de 5 cartes wagon face visibles
     * (parce que la pioche
     * et la défausse sont vides), alors il faut immédiatement rendre cette carte
     * face visible.
     *
     * @param c carte à défausser
     */
    public void defausserCarteWagon(CouleurWagon c) {
        defausseCartesWagon.add(c);
        if (cartesWagonVisibles.size() < 5) {
            cartesWagonVisibles.add(c);
            defausseCartesWagon.remove(defausseCartesWagon.size() - 1);
        }

    }


    /**
     * Pioche une carte de la pile de pioche
     * Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
     * puis mélangées avant de piocher une carte
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CouleurWagon piocherCarteWagon() {

        if (pileCartesWagon.isEmpty()) {
            if (!defausseCartesWagon.isEmpty()) {
                remelangerDefausse();
            } else {
                return null;
            }
        }
        return pileCartesWagon.remove(0);
    }

    public void remelangerDefausse() {
        pileCartesWagon.addAll(defausseCartesWagon);
        defausseCartesWagon.clear();

        pileCartesWagon = melangerList(pileCartesWagon);
    }


    /**
     * Retire une carte wagon de la pile des cartes wagon visibles.
     * Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
     * (remise à 5, éventuellement remélangée si 3 locomotives visibles)
     */
    public void retirerCarteWagonVisible(CouleurWagon c) {
        if (cartesWagonVisibles.remove(c)) {
            cartesWagonVisibles.add(piocherCarteWagon());
            while (cartesWagonVisibles.remove(null)){}

            for (int i=0; i<10 && !moinsDeTroisCartesLocomotiveVisibles(); i++) {
                for (CouleurWagon carte : cartesWagonVisibles) {
                    defausserCarteWagon(carte);
                }
                cartesWagonVisibles.clear();
                cartesWagonVisibles = piocher_n_Cartes_CarteWagon(5);
            }
        }
    }

    public boolean moinsDeTroisCartesLocomotiveVisibles() {
        int nbLocomotives = 0;
        for (CouleurWagon carte : cartesWagonVisibles) {
            if (carte == CouleurWagon.LOCOMOTIVE) {
                nbLocomotives++;
            }
        }
        return nbLocomotives < 3;
    }

    /**
     * Pioche et renvoie la destination du dessus de la pile de destinations.
     *
     * @return la destination qui a été piochée (ou `null` si aucune destination
     * disponible)
     */
    public Destination piocherDestination() {
        return pileDestinations.isEmpty() ? null : pileDestinations.remove(0);
    }

    public boolean pileDestinationIsEmpty(){
        return pileDestinations.isEmpty();
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     * file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<%n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<%n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<String, Object>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes",
                        villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes",
                        routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }


    public List<CouleurWagon> melangerList(List<CouleurWagon> listDeBase) {
        Random random = new Random();
        List<CouleurWagon> listMelange = new ArrayList<>(); //création de la pile de carte qui va être retourne
        System.out.println(listDeBase);//affichag de la pile de carte de base
        int nombreDeCarteDansPileDeBase = listDeBase.size();
        for (int i = 0; i < nombreDeCarteDansPileDeBase; i++) { //pour toutes les carte de la pile de base faire
            int randomNumber = random.nextInt(listDeBase.size() - 1 + 1);
            //choix d'une carte alétoire du paquet des carte de wagon
            CouleurWagon carteCouleurWagonChoisiAleatoirement = listDeBase.get(randomNumber);
            listMelange.add(carteCouleurWagonChoisiAleatoirement);
            listDeBase.remove(randomNumber);
        }

        return listMelange;
    }


    public List<CouleurWagon> initialiser_nouvelle_pioche() {
        List<CouleurWagon> pile = new ArrayList<>();
        List<CouleurWagon> couleurs = CouleurWagon.getCouleursSimples();
        for (CouleurWagon couleur : couleurs) {
            for (int i = 0; i < 12; i++) {
                pile.add(couleur);
            }
        }
        for (int i = 0; i < 14; i++) {
            pile.add(CouleurWagon.LOCOMOTIVE);
        }
        pile = melangerList(pile);
        return pile;
    }


    public ArrayList<CouleurWagon> piocher_n_Cartes_CarteWagon(int nombre_de_cartes) {
        ArrayList<CouleurWagon> cartes = new ArrayList<>();
        for (int i = 0; i < nombre_de_cartes; i++) {
            cartes.add(piocherCarteWagon());
        }
        return cartes;
    }


    //pour test retirerCarteWagonVisible
    public void mettreTroisCarteVagonEtUneBlancheDansPileCarteVisible() {
        cartesWagonVisibles.clear();
        cartesWagonVisibles.add(CouleurWagon.LOCOMOTIVE);
        cartesWagonVisibles.add(CouleurWagon.LOCOMOTIVE);
        cartesWagonVisibles.add(CouleurWagon.LOCOMOTIVE);
        cartesWagonVisibles.add(CouleurWagon.BLANC);
        cartesWagonVisibles.add(CouleurWagon.BLEU);

    }

    public ArrayList<String> getNomVillesSansProprietaires() {
        ArrayList<String> resultat = new ArrayList<>();
        for (Ville ville : villes) {
            if (ville.getProprietaire() == null) {
                resultat.add(ville.getNom());
            }
        }
        return resultat;
    }

    public ArrayList<Route> getRoutesSansProprietaires() {
        ArrayList<Route> resultat = new ArrayList<>();
        for (Route route : routes) {
            if (route.getProprietaire() == null) {
                resultat.add(route);
            }
        }
        return resultat;
    }

    public ArrayList<String> getNomRoutesSansProprietaires() {
        ArrayList<String> resultat = new ArrayList<>();
        for (Route route : getRoutesSansProprietaires()) {
            resultat.add(route.getNom());
        }
        return resultat;
    }


        public ArrayList<String> routeToString(List<Route> routes) {
        ArrayList<String> resultat = new ArrayList<>();
        for (Route route : routes) {
            resultat.add(route.getNom());
        }
        return resultat;
    }

    public ArrayList<String> vileToString(List<Ville> villes) {
        ArrayList<String> resultat = new ArrayList<>();
        for (Ville ville : villes) {
            resultat.add(ville.getNom());
        }
        return resultat;
    }

    public void remettreCarteDestinationDansPile(ArrayList<Destination> destinations){
        pileDestinations.addAll(destinations);
    }


    public List<CouleurWagon> getDefausseCartesWagon() {
      return   defausseCartesWagon;
    }

    public List<Destination> getPileDestinations() {
        return pileDestinations;
    }

    public Ville getVilleFromChoix(String choix){
        Ville ville= new Ville("");
        for (int i=0;i<villes.size();i++){
            if (Objects.equals(villes.get(i).getNom(), choix)) ville=villes.get(i);
        }
        return ville;
    }

}
