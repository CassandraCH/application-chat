package serveur;

import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;

/**
 * La classe Serveur herite de JFrame.
 * La classe Serveur permet de gerer le serveur et la fenetre du serveur.
 * Un Serveur possede :
 * - un numero de port
 * - une FenetreServeur
 * - une liste des noms des utilisateurs connectes
 * - une liste des threads
 * - une liste de messages (= discussion des utilisateurs)
 * - un fichier ou sont stockes les utilisateurs et leurs couleurs associees (accessible par les utilisateurs)
 * - un compteur du nombre de connexions en cours
 */
public class Serveur extends JFrame {
    /** variable pour la serialisation */
    private static final long serialVersionUID = 6781075975962305703L;

    /** Numero de port du serveur */
    static final int port = 5050;
    /** Fenetre du serveur */
    private static FenetreServeur fenetre;

    /** Liste des utilisateurs connectes au serveur */
    private static List<String> listeConnectes = new ArrayList<>();

    /** Liste des threads */
    private static CopyOnWriteArrayList<ServeurThread> listeThread = new CopyOnWriteArrayList<>();

    /** Discussion en cours sur le serveur */
    private static List<String> discussion;

    /** Fichier ou est stockee la liste des utilisateurs connectes et leur couleur respective */
    private static File fichier;

    /** Compteur du nombre de connexions */
    private static int nbConnexions;

    /**
     * Constructeur pour creer un Serveur
     *
     * @param port numero de port du serveur
     */
    public Serveur(int port) {
        super("Gestion du serveur");
        initialiser();

        nbConnexions = 0;

        discussion = new ArrayList<>();
        fichier = new File("utilisateurs.txt");

        // Suppression du fichier s'il existe deja
        if (fichier.exists()) {
            fichier.delete();
        }
        // Creation du fichier
        try {
            fichier.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Methode qui permet de lancer le serveur
     */
    public void lancer() {
        try (ServerSocket serveur = new ServerSocket(port)) {
            // Boucle infinie pour ecouter les clients entrants
            while (true) {
                // Ecoute des connexions
                Socket socketClient = serveur.accept();
                fenetre.setStatut("Serveur en route sur le port " + Serveur.port + " (nb de connexions = " + nbConnexions + ")");

                // Creation d'un nouveau objet thread
                ServeurThread connexion = new ServeurThread(socketClient);
                // Ajout du nouveau thread a la liste
                listeThread.add(connexion);
                connexion.start();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Classe interne qui permet de lire les messages entrants (messages envoyes par
     * les clients)
     */
    private static class ServeurThread extends Thread {
        /** Socket du client */
        private final Socket socket;
        /** Output du client */
        private DataOutputStream output;
        /** Utilisateur actuel */
        private Utilisateur utilisateur;

        /**
         * Constructeur pour creer un ServeurThread
         * @param socket socket
         */
        public ServeurThread(Socket socket) {
            this.socket = socket;
        }

        /**
         * Methode qui est executee par le ServeurThread
         */
        @Override
        public void run() {
            try {
                // Recuperation de l'output du client
                output = new DataOutputStream(socket.getOutputStream());
                // Recuperation de l'input du client
                DataInputStream input = new DataInputStream(socket.getInputStream());
                // Recuperation du nom de l'utilisateur
                utilisateur = new Utilisateur(input.readUTF());

                String messageClient = "";
                String messageServeur = "";

                // Ajout de l'utilisateur a la liste (s'il n'y est pas deja)
                if (!listeConnectes.contains(utilisateur.getNom())) {
                    listeConnectes.add(utilisateur.getNom());
                }

                // Ajout de l'utilisateur dans le fichier (sa couleur est generee et ajoutee)
                ajouterUtilisateurDansFichier(utilisateur);

                // Envoi de la liste des personnes connectees au serveur
                envoyerListeBroadcast();

                // Tant que le client ne s'est pas deconnecte => le serveur lit les messages
                do {
                    // Lecture du message envoye par le client
                    messageClient = input.readUTF();
                    System.out.println("Message recu => " + messageClient);

                    // Decoupage du message afin de recuperer la bonne partie ("-" est le delimiteur)
                    StringTokenizer tokenizer = new StringTokenizer(messageClient, "-");
                    // Recuperation de l'option/prefixe
                    String option = tokenizer.nextToken();

                    // Si le prefixe est "chat" => message a envoye a tous les utilisateurs
                    if (option.equals("chat")) {
                        option = tokenizer.nextToken();

                        // Gestion de la deconnexion
                        if (messageClient.contains("Deconnexion")) {
                            messageServeur = utilisateur.getNom() + " s'est deconnecté.";
                            // Envoi du message a tout le monde sauf la personne qui s'est deconnecte
                            envoyerBroadcast(messageServeur, utilisateur.getNom());
                        } else {
                            // Ajout de l'expediteur devant le message
                            messageServeur = utilisateur.getNom() + " : " + option;
                            envoyerBroadcast(messageServeur, null);
                        }
                    }
                    // Si le prefixe est "prive" => message prive
                    else if (option.equals("prive")) {
                        // recuperation du nom du destinatire
                        String destinataire = tokenizer.nextToken();
                        option = tokenizer.nextToken();
                        messageServeur = utilisateur.getNom() + " (" + destinataire + ") : " + option;
                        envoyerMessagePrive(messageServeur, utilisateur.getNom(), destinataire);
                    }

                    System.out.println("Message serveur =>  " + messageServeur);
                    ajouterMessageDansDiscussion(messageServeur);
                } while (!messageClient.contains("Deconnexion"));

                // suppression de l'utilisateur
                supprimerUtilisateur(this);

                // Envoi de la liste des personnes connectees au serveur
                envoyerListeBroadcast();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Methode qui permet a un client d'envoyer un message a toutes les personnes
         * connectees
         * @param message message à envoyer
         */
        public void envoyerMessage(String message) {
            try {
                output.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Erreur lors de l'envoi");
                e.printStackTrace();
            }
        }

        /**
         * Methode qui permet d'envoyer a toutes les personnes connectees au serveur la liste
         * des personnes connectees
         */
        public void envoyerListeBroadcast(){
            // On verifie si la liste des connectes n'est pas vide
            if (!listeThread.isEmpty()) {
                String s = String.join("-", listeConnectes);
                // Ajout du prefixe "init" pour indiquer aux clients qu'ils doivent mettre a jour leur liste
                // de personnes connectee
                envoyerBroadcast("init-" + s, null);
                System.out.println("Liste utilisateurs envoyee : " + s + ",");
            } else {
                System.out.println("Liste utilisateurs non envoyee");
            }
        }

        // GETTER
        /**
         * Getter de l'utilisateur actuel
         * @return l'utilisateur
         */
        public Utilisateur getUtilisateur() {
            return utilisateur;
        }
    } // fin de la classe interne

    /**
     * Methode qui permet d'initialiser l'affichage de la fenetre du serveur
     */
    public void initialiser() {
        fenetre = new FenetreServeur();
        add(fenetre);

        fenetre.setStatut("En attente de connexion...");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // gestion de la fermeture de la fenetre
        setSize(new Dimension(420, 600)); // regler la taille
        setLocationRelativeTo(null); // centrer la fenetre

        // Lorsqu'on ferme la fenetre du serveur => on supprime le fichier .txt
        addWindowListener(new WindowAdapter() {
            /**
             * Methode qui est executee en cas de fermeture de la fenetre
             * @param e evenement
             */
            @Override
            public void windowClosed(WindowEvent e) {
                // Suppression du fichier s'il existe deja
                if (fichier.exists()) {
                    fichier.delete();
                }
            }
        });
        setVisible(true); // afficher
    }

    /**
     * Methode qui permet d'ajouter un nouveau utilisateur qui vient de se connecter
     * dans le fichier => sa couleur est generee et lui est attribue
     * @param u utilisateur a ajouter
     */
    public static void ajouterUtilisateurDansFichier(Utilisateur u) {
        // On verifie que l'utilisateur ne s'est pas deja connecte une fois
        if (utilisateurPresent(u.getNom())) {
            // Generation d'une couleur aleatoire et attribution de cette derniere l'utilisateur
            Color couleur = u.genererCouleur();
            u.setCouleur(couleur);
            // Ajout dde l'utilisateur et de sa couleur dans le fichier
            ecrireDansFichier(u);
        }
        // Changement du statut de l'utilisateur
        u.setConnecte(true);

        // Ajout du nom de l'utilisateur dans la fenetre
        fenetre.ajouterUtilisateurFenetre(u.getNom());
        System.out.println("Nouveau utilisateur ajoute avec succes : " + u.getNom());
        nbConnexions++;
        fenetre.setStatut("Serveur en route sur le port " + Serveur.port + " (nb de connexions = " + nbConnexions + ")");
    }

    /**
     * Methode qui permet de supprimer le thread d'un utilisateur qui s'est deconnecte
     * @param st thread a supprimer de la liste
     */
    public static void supprimerUtilisateur(ServeurThread st) {
        // Suppression du nom de l'utilisateur de la liste
        listeConnectes.remove(st.getUtilisateur().getNom());
        // Changement du statut de l'utilisateur
        st.getUtilisateur().setConnecte(false);
        // Suppression du thread de la liste
        listeThread.remove(st);
        majAffichageConnectes();

        nbConnexions--;
        fenetre.setStatut("Serveur en route sur le port " + Serveur.port + " (nb de connexions = " + nbConnexions + ")");
    }

    /**
     * Methode qui permet de mettre a jour l'affichage de la liste des personnes connectees
     */
    public static void majAffichageConnectes() {
        fenetre.effacerUtilisateursFenetre(); // On efface tout
        // Parcours de la liste des threads
        for (ServeurThread st : listeThread) {
            if (st.getUtilisateur().getConnecte()) {
                // Ajout des noms au fur et a mesure
                fenetre.ajouterUtilisateurFenetre(st.getUtilisateur().getNom());
            }
        }
    }

    /**
     * Methode qui permet d'ecrire dans le fichier .txt les noms des utilisateurs
     * ainsi que leur couleur associee
     * @param u utilisateur a ajouter
     */
    public static void ecrireDansFichier(Utilisateur u) {
        // true => permet de fermet automatiquement le fichier
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichier, true))) {
            String s = u.getNom() + ":" + u.getCouleur();
            writer.write(s);
            writer.newLine(); // Saut de ligne

            System.out.println("\t=> Ajout de l'utilisateur dans le fichier  : "+s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Methode qui permet d'envoyer un message a tous les utilisateurs sauf a
     * l'expediteur. Si l'expediteur == null, tout le monde recoit le message
     * @param msg        message a envoyer
     * @param expediteur expediteur du message
     */
    public static void envoyerBroadcast(String msg, String expediteur) {
        for (ServeurThread utilisateur : listeThread) {
            if ((expediteur==null) || (!utilisateur.getUtilisateur().getNom().equals(expediteur))) {
                utilisateur.envoyerMessage(msg);
            }
        }
    }

    /**
     * Methode qui permet d'envoyer le message uniquement a l'expediteur et au destinataire (message prive)
     * @param msg message a envoyer
     * @param expediteur expediteur du message
     * @param destinataire destinatire du message
     */
    public static void envoyerMessagePrive(String msg, String expediteur, String destinataire) {
        for (ServeurThread utilisateur : listeThread) {
            if (utilisateur.getUtilisateur().getNom().equals(expediteur) || utilisateur.getUtilisateur().getNom()
                    .equals(destinataire)) {
                utilisateur.envoyerMessage(msg);
            }
        }
    }

    /**
     * Methode qui permet de savoir si un utilisateur donne est deja present dans la liste
     * @param nom nom de l'utilisateur pour lequel on veut verifier sa presence ou non
     * @return un booleen
     */
    public static boolean utilisateurPresent(String nom) {
        for(int i = 0; i < listeConnectes.size(); i++){
            if(listeConnectes.get(i).equals(nom)){
                return true;
            }
        }
        return false;
    }

    /**
     * Methode qui permet d'ajouter un message dans la discussion
     * @param msg nouveau message a ajouter
     */
    public static void ajouterMessageDansDiscussion(String msg) {
        discussion.add(msg + "\n");
        fenetre.getDiscussion().append(msg + "\n");
    }
}
