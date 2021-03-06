package fr.umontpellier.iut.rails;

import java.util.*;
import java.util.stream.Collectors;

public class Joueur {

    public int getNbGares() {
        return nbGares;
    }

    public void setNbWagon(int i) {
        nbWagons = i;
    }

    public int getScore() {
        return score;
    }

    /**
     * Les couleurs possibles pour les joueurs (pour l'interface graphique)
     */
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }

    /**
     * Jeu auquel le joueur est rattaché
     */
    private Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;
    /**
     * CouleurWagon du joueur (pour représentation sur le plateau)
     */
    private Couleur couleur;
    /**
     * Nombre de gares que le joueur peut encore poser sur le plateau
     */
    private int nbGares;
    /**
     * Nombre de wagons que le joueur peut encore poser sur le plateau
     */
    private int nbWagons;
    /**
     * Liste des missions à réaliser pendant la partie
     */
    private List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private List<CouleurWagon> cartesWagon;
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées)
     */
    private int score;

    private CouleurWagon couleurChoisi;

    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {
        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons,
                          boolean peutPasser) {
        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {
            return "";
        } else {
            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)
                return choixDistincts.iterator().next();
            else {
                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {
                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))
                        return entree;
                }
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees",
                cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide)teste.
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public ArrayList<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        ArrayList<Destination> destinationsDefaussees = new ArrayList<>(); //création du choix "choix_destination"
        ArrayList<String> boutons_cartes_destinations = new ArrayList<>(); //création d'un bouton pour chaque carte destination

        while (destinationsPossibles.remove(null)) {/*tkt je sais ce que je fais*/}
        int nombreDeDestinationsRestantes = destinationsPossibles.size();

        while (nombreDeDestinationsRestantes > n) {
            boutons_cartes_destinations.clear();
            for (Destination carte : destinationsPossibles) {//créer un bouton pour chaque carte dans destinationPossible
                boutons_cartes_destinations.add(carte.getNom());
            }

            String choix = choisir("Deffaussez les destinations dont vous ne voulez pas, appuyez sur \"passer\" pour garder celles qui sont affichés",
                    new ArrayList<>(),
                    boutons_cartes_destinations,
                    true); //affichage de tout les boutons


            if (!choix.equals("")) {
                Destination choisi = Destination.getDestinationAvecNom(choix, destinationsPossibles);
                destinationsDefaussees.add(choisi);
                destinationsPossibles.remove(choisi);
                nombreDeDestinationsRestantes--;
            } else {
                destinations.addAll(destinationsPossibles);
                return destinationsDefaussees;
            }
        }
        destinations.addAll(destinationsPossibles);
        return destinationsDefaussees;
    }


    /**
     * Exécute un tour de jeu du joueur.
     * <p>
     * Cette méthode attend que le joueur choisisse une des options suivantes :
     * - le nom d'une carte wagon face visible à prendre ;
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */
    public void jouerTour() {
        toLog();
        ArrayList<String> choixBoutons = new ArrayList<>();
        ArrayList<String> choixHorsBoutons = new ArrayList<>();
        if (!jeu.getCartesWagonVisibles().isEmpty()) {
            for (CouleurWagon carte : jeu.getCartesWagonVisibles()) {
                if (carte != null) {
                    choixHorsBoutons.add(carte.name());
                }
            }
        }
        if(!jeu.getPileCartesWagon().isEmpty() || !jeu.getDefausseCartesWagon().isEmpty()) {choixHorsBoutons.add("GRIS");}
        if (peutConstruireGare()) {
            choixHorsBoutons.addAll(jeu.getNomVillesSansProprietaires()); //on lui laisse pas le choix si il ne peut pas construire de gare
        }

        choixHorsBoutons.addAll(nomRoutesPrenables());
        if (!jeu.pileDestinationIsEmpty()) {
            choixHorsBoutons.add("destinations");
            choixBoutons.add("destinations");
        }

        String choix = choisir("cliquez sur une route ou une ville pour la construire. Cliquez sur la pioche, une carte wagon ou destination pour piocher", choixHorsBoutons, choixBoutons, true);
        if (CouleurWagon.getAllCouleursString().contains(choix)) {
            log("pioche...");
            choisirCarteWagon(choix);


        } else if (jeu.getNomRoutesSansProprietaires().contains(choix)) {
            construireRoute(choix);
        } else if (choix.equals("destinations")) {
            log("vous avez choisi de tirer des cartes destinations !");
            ArrayList<Destination> destinationsPossibles = new ArrayList<>();
            destinationsPossibles.add(jeu.piocherDestination());
            destinationsPossibles.add(jeu.piocherDestination());
            destinationsPossibles.add(jeu.piocherDestination());//si plus de cartes, piocher retourne null

            jeu.remettreCarteDestinationDansPile(choisirDestinations(destinationsPossibles, 1));
        } else if (jeu.getNomVillesSansProprietaires().contains(choix)) {
            log("vous avez choisi de construire une gare sur\n"+choix);
            couleurChoisi = null;
            int nbCarteQuiResteADefausser = 4 - nbGares;
            String choix_carte = "init";
            while (nbCarteQuiResteADefausser != 0) {


                //*****************************bloc restreindre choix possible ***********************************/
                ArrayList<String> carteAcceptable = new ArrayList<>();
                if (cartesWagon.contains(CouleurWagon.LOCOMOTIVE)) { //dans tout les cas il peut jouer ses cartes loco
                    carteAcceptable.add(CouleurWagon.LOCOMOTIVE.name());
                }

                if (couleurChoisi == null) {
                    for (CouleurWagon carte : CouleurWagon.getCouleursSimples()) {
                        if (possede_n_fois_couleur_plus_loco(nbCarteQuiResteADefausser, carte))
                            carteAcceptable.add(carte.name());
                    }

                } else {
                    if (cartesWagon.contains(couleurChoisi)) {
                        carteAcceptable.add(couleurChoisi.name());
                    }

                }
                //***************bloc premier choix**********************************/
                choix_carte = choisir("choisir " + String.valueOf(nbCarteQuiResteADefausser) + " carte à defausser", carteAcceptable, new ArrayList<>(), false); //on lui fait choisir sa carte
                if (choix_carte.equals("")) break;
                else {
                    if (couleurChoisi == null && !choix_carte.equals(CouleurWagon.LOCOMOTIVE.name())) {
                        couleurChoisi = CouleurWagon.stringToCouleurWagon(choix_carte);
                    }
                    utiliserCarterWagon(CouleurWagon.stringToCouleurWagon(choix_carte));
                    nbCarteQuiResteADefausser--;
                }
            }
            if (choix_carte.equals("")) {
                annulerCaptureGare();
                jouerTour();
            } else {
                jeu.getVilleFromChoix(choix).construireGare(this);
                confirmerCaptureGare();
            }
        }
    }

    private void annulerCaptureGare() {
        cartesWagon.addAll(cartesWagonPosees);
        cartesWagonPosees.clear();
    }

    public void addCarteWagon(ArrayList<CouleurWagon> cartesAPiocher) {
        cartesWagon.addAll(cartesAPiocher);
    }

    //prérequis : le joueur à cliqué sur une CouleurWagon (pioche ou visible)
    //si le joueur pioche ou tire une carte visible, il doit soit en prendre une autre soit passer son tour.
    //quand la fonction se termine, le joueur à fini son tour.
    public void choisirCarteWagon(String choix) {
        int nbCartesPiochees = 0;
        while (nbCartesPiochees < 2 && !choix.equals("")) {
            if (choix.equals("GRIS")) { //on clique sur la pioche
                CouleurWagon carte = jeu.piocherCarteWagon();
                if (carte != null) {
                    cartesWagon.add(carte);
                    nbCartesPiochees++;
                }
            } else if (choix.equals("LOCOMOTIVE")) {
                if (nbCartesPiochees == 0) {
                    jeu.retirerCarteWagonVisible(CouleurWagon.LOCOMOTIVE);
                    cartesWagon.add(CouleurWagon.LOCOMOTIVE);
                    break;
                } else {
                    log("Vous ne pouvez pas prendre un joker !\n choisissez une autre carte");
                }
            } else { //on clique sur une carte visible hors locomotive
                cartesWagon.add(CouleurWagon.stringToCouleurWagon(choix));
                jeu.retirerCarteWagonVisible(CouleurWagon.stringToCouleurWagon(choix));
                nbCartesPiochees++;
            }
            if (nbCartesPiochees < 2) {
                ArrayList<String> choixHorsBoutons = new ArrayList<>();
                for (CouleurWagon carte : jeu.getCartesWagonVisibles()) {
                    choixHorsBoutons.add(carte.name());
                }
                choixHorsBoutons.add("GRIS");
                choix = choisir("choisissez une autre carte wagon ou piochez", choixHorsBoutons, new ArrayList<>(), true);
            }
        }
    }


    public void construireRoute(String choix) {
        Route routeChoisi = null;
        for (Route route : jeu.getRoutes()) {
            if (route.getNom().equals(choix)) {
                routeChoisi = route;
            }
        }
        routeChoisi.construireRoute(this);
    }

    public boolean utiliserCarterWagon(CouleurWagon couleur) {
        if (cartesWagon.contains(couleur)) {
            cartesWagon.remove(couleur);
            cartesWagonPosees.add(couleur);
            return true;
        }
        return false;
    }

    public void annulerCaptureRoute() {
        cartesWagon.addAll(cartesWagonPosees);
        cartesWagonPosees.clear();
    }

    public void confirmerCaptureRoute(int longueur) {
        for (CouleurWagon carte : cartesWagonPosees) {
            jeu.defausserCarteWagon(carte);
        }
        cartesWagonPosees.clear();
        nbWagons -= longueur;
    }

    public void confirmerCaptureGare() {

        for (CouleurWagon carte : cartesWagonPosees) {
            jeu.defausserCarteWagon(carte);
        }
        cartesWagonPosees.clear();

    }

    public void ajouterPoint(int nbPoints) {
        score += nbPoints;
    }

    public boolean possede_n_fois_couleur_plus_loco(int n, CouleurWagon couleur) {
        if (couleur == CouleurWagon.LOCOMOTIVE) {
            return Collections.frequency(cartesWagon, couleur) >= n;
        } else {
            return Collections.frequency(cartesWagon, couleur) + Collections.frequency(cartesWagon, CouleurWagon.LOCOMOTIVE) >= n;
        }
    }

    public void setNbGares(int nbGares) {
        this.nbGares = nbGares;
    }

    public boolean peutConstruireGare() {
        for (CouleurWagon carte : CouleurWagon.getCouleursSimples()) { //verifier si avec ses cartes il peut construire
            if (Collections.frequency(cartesWagon, carte)
                    + Collections.frequency(cartesWagon, CouleurWagon.LOCOMOTIVE) >= 4 - nbGares) {
                return true;
            }
        }
        return false;
    }


    public ArrayList<String> nomRoutesPrenables(){
        ArrayList<String> resultat = new ArrayList<>();
        for (Route route : jeu.getRoutesSansProprietaires()) {
            if(route.joueur_a_assez_de_cartes_et_de_wagons_pour_construire(this)&& route.joueur_n_a_pas_deja_pris_sa_route_jumelle(this)){
                resultat.add(route.getNom());
            }
        }





        return resultat;
    }


}
