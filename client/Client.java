package client;

import java.awt.Color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * La Classe Client permet de gerer toute les connexions concernant le client
 * Un Client possede :
 * - une fenetre
 * - un nom
 * - un port
 * - une adresse IP
 * - un booleen qui permet de savoir s'il est connecte ou non
 * - une entree
 * - une sortie
 * - un socket
 */
public class Client {
    /** Fenetre du client */
    private FenetreClient fenetre;

    /** Nom du client */
    private String nom;
    /** Numero de port */
    private int port;
    /** Adresse IP */
    private String adIP;
    /** Booleen qui permet de savoir si le client est connecte ou pas */
    private boolean connecte;

    /** Sortie du client */
    private DataOutputStream output;
    /** Entree du client */
    private DataInputStream input;
    /** Socket du client */
    private Socket socket;

    /**
     * Constructeur qui permet de creer un Client
     *
     * @param f    fenetre du client
     * @param nom  nom du client
     * @param ip   adresse IP
     * @param port numero du port pour la connexion avec le serveur
     */
    public Client(FenetreClient f, String nom, String ip, int port) {
        this.nom = nom;
        this.adIP = ip;
        this.port = port;
        this.fenetre = f;
        this.connecte = false;
    }

    /**
     * Methode qui permet au client de se connecter au serveur
     */
    public void connexion() {
        if (connexionServeur(adIP)) {
            connecte = true;
            System.out.println("Connexion OK");

            // nouveau thread pour lire les messages
            new LectureMessageThread().start();
        }
    }

    /**
     * Methode qui renvoie un booleen en fonction de si la connexion avec le serveur
     * s'est bien passee
     *
     * @param hote hote
     * @return un booleen
     */
    public boolean connexionServeur(String hote) {
        try {
            socket = new Socket(hote, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // Envoi du nom du client au serveur
            output.writeUTF(this.nom);

        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(fenetre, "Adresse IP inconnue...", "ERREUR", JOptionPane.ERROR_MESSAGE);
            return false;

        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(fenetre, "Le serveur n'est peut etre pas encore ouvert...", "ERREUR", JOptionPane.ERROR_MESSAGE);
            return false;

        } catch (NoRouteToHostException e) {
            JOptionPane.showMessageDialog(fenetre, "Hote introuvable...", "ERREUR", JOptionPane.ERROR_MESSAGE);
            return false;

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Erreur avec la connexion au serveur");
        }
        return true;
    }

    /**
     * Methode qui permet au client de se deconnecter
     */
    public void deconnexion() {
        // envoi du message de deconnexion
        envoyerMessage("Deconnexion","Tout le monde");
        // mise a jour du statut du client
        connecte = false;
        System.out.println("Deconnexion OK");
    }

    /**
     * Methode qui permet d'envoyer un message
     *
     * @param msg  message a envoyer
     * @param dest option d'envoi (tout le monde ou un utilisateur donne)
     */
    public void envoyerMessage(String msg, String dest) {
        JTextPane jtp = fenetre.getPanneauDiscussion().getDiscussion();

        try {
            String str= "";
            // Cas d'un message prive
            if(!dest.equals("Tout le monde")){
                // Ajout du prefixe "prive-" pour preciser au serveur qu'il s'agit d'un message prive
                str = "prive-"+ dest + "-" + msg + "-";
                System.out.println("Message prive envoye : " + msg);
            }
            // Cas ou le message est envoye a toutes les personnes connectees
            else{
                // Ajout du prefixe "chat-" pour preciser au serveur qu'il s'agit d'un message a
                // envoyer a tous les utilisateurs
                str = "chat-" + msg + "-";
                System.out.println("Message envoye : " + msg);
            }
            // envoi du message
            output.writeUTF(str);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            jtp.setText(jtp.getText().concat("\nOups, nous rencontrons un probleme pour envoyer ton message..."));
        }
    }

    /**
     * Classe interne pour lire les messages entrants
     */
    private class LectureMessageThread extends Thread {
        /**
         * Methode qui est executee dans le Thread
         */
        @Override
        public void run() {
            // Tant que le client est connecte => on peut lire les messages entrants
            while (connecte) {
                String message = "";
                StyledDocument sd = fenetre.getPanneauDiscussion().getDiscussion().getStyledDocument();

                try {
                    if (connecte) {
                        // lecture du message
                        message = input.readUTF();

                        // StringTokenizer permet de decouper la chaine en fonction du delimiteur "-"
                        StringTokenizer sTokenizer = new StringTokenizer(message, "-");
                        // Recuperation de la premiere portion
                        String option = sTokenizer.nextToken();

                        // Si le message contient "init" => le serveur envoie la liste des personnes
                        // connectees
                        // Ce message est envoye uniquement lors d'une nouvelle connexion ou d'une deconnexion d'un utilisateur au serveur
                        if (option.contains("init")) {
                            // On vide la liste des clients connectes
                            fenetre.getPanneauDiscussion().videListeClient();

                            // Lecture de la liste et ajout au fur et a mesure dans la liste
                            while (sTokenizer.hasMoreTokens()) {
                                option = sTokenizer.nextToken();
                                // Ajout des clients a la liste
                                fenetre.getPanneauDiscussion().ajouterClientListe(option);
                            }
                            // System.out.println("Chaine recuperee : " + option);
                            // Mettre a jour l'affichage
                            fenetre.getPanneauDiscussion().majAffichageConnectes();
                        }
                        // Cas d'un message prive
                        else if (option.contains("(")) {
                            int i = message.indexOf("(");
                            String nom = message.substring(0, i-1);// recuperation du nom

                            System.out.println("Message prive recu : " + message);

                            // Ajout du message dans le panneau de discussion
                            ajouterMessage(sd, StyleContext.getDefaultStyleContext(), StyleConstants.Foreground, chercherCouleur(nom), message);
                        }
                        // Cas d'un message broadcast
                        else{
                            String[] tmp = (message.split(" : "));
                            String nom = tmp[0]; // recuperation du nom

                            System.out.println("Message recu : " + message);

                            if (!message.contains("deconnecté")) {
                                // Affection de la bonne couleur au message en fonction du nom de la personne
                                // qui l'a envoye
                                ajouterMessage(sd, StyleContext.getDefaultStyleContext(), StyleConstants.Foreground, chercherCouleur(nom), message);
                            }
                            // Gestion des messages de deconnexion des autres utilisateurs => message en noir et italique
                            else {
                                ajouterMessage(sd, StyleContext.getDefaultStyleContext(), StyleConstants.Italic, true, message);
                            }
                        }

                    } // fin if(connecte)
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            } // fin while
        }// fin run()
    }// fin classe interne

    /**
     * Methode qui permet d'inserer un message avec une couleur ou un style donne
     *
     * @param sd      StyledDocument
     * @param sc      StyleContext
     * @param name    Nom de la constante
     * @param value   valeur
     * @param message message a ajouter
     */
    public void ajouterMessage(StyledDocument sd, StyleContext sc, Object name, Object value, String message) {
        StyledDocument doc = sd;
        StyleContext style = sc;
        // Affectation de la couleur ou du style entre en parametre
        AttributeSet att = style.addAttribute(style.getEmptySet(), name, value);

        AttributeSet aset = null;

        // Si message prive => couleur + italique
        if(message.contains("(")){
            aset = style.addAttributes(att, style.addAttribute(style.getEmptySet(), StyleConstants.Italic, true));
        }
        // Sinon => couleur + gras
        else{
            aset = style.addAttributes(att, style.addAttribute(style.getEmptySet(), StyleConstants.Bold, true));
        }

        try {
            // Ajout du message
            doc.insertString(doc.getLength(), message + "\n", aset);
            // Gestion de l'auto-scrolling
            fenetre.getPanneauDiscussion().getDiscussion().setCaretPosition(fenetre.getPanneauDiscussion().getDiscussion().getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Methode qui permet de chercher la couleur correspondante a une personne
     * @param nom de la personne pour qui on cherche sa couleur
     * @return la couleur (format rgb = un entier)
     */
    public Color chercherCouleur(String nom){
        PanneauDiscussion p = fenetre.getPanneauDiscussion();

        for (Map.Entry<String, Color> entry : p.getCouleurs().entrySet()) {
            if (entry.getKey().equals(nom)) {
                return entry.getValue(); // recuperation de la couleur
            }
        }
        return Color.BLACK;
    }

    // GETTERS
    /**
     * Getter du nom du client
     * @return le nom du client
     */
    public String getNom() {
        return nom;
    }

    /**
     * Methode qui permet de savoir si le client est connecte ou pas
     * @return un booleen
     */
    public boolean getConnecte(){
        return connecte;
    }
}
