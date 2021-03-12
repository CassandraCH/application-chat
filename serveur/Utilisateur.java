package serveur;
import java.awt.Color;

import java.util.Random;

/**
 * La classe Utilisateur permet d'assigner a chaque utilisateur une couleur
 * Un Utilisateur est caracterise par :
 * - un nom
 * - une couleur
 * - un statut (connecte ou non => booleen)
 */
public class Utilisateur {
    /** Nom de l'utilisateur */
    private String nom;
    /** Couleur de l'utilisateur */
    private Color couleur;
    /** Statut de l'utilisateur (connecte ou non) => booleen */
    private boolean connecte;

    /**
     * Constructeur qui permet de creer un utilisateur
     * @param nom nom de l'utilisateur
     */
    public Utilisateur(String nom){
        this.nom = nom;
        this.connecte = true;
        this.couleur = null;
    }

    /**
     * Methode qui permet de generer une couleur aleatoirement
     * @return la couleur generee
     */
    public Color genererCouleur(){
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);

        return (new Color(r,g,b));
    }

    // GETTERS
    /**
     * Getter du nom de l'utilisateur
     * @return le nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * Getter de la couleur de l'utilisateur
     * @return la couleur
     */
    public int getCouleur() {
        return couleur.getRGB();
    }

    /**
     * Methode qui permet de savoir si l'utilisateur est toujours connecte ou pas
     * @return un booleen
     */
    public boolean getConnecte() {
        return connecte;
    }

    // SETTERS
    /**
     * Methode qui permet de modifier le statut de l'utilisateur
     * @param connecte valeur du statut
     */
    public void setConnecte(boolean connecte) {
        this.connecte = connecte;
    }

    /**
     * Methode qui permet d'attribuer une couleur a un utilisateur
     * @param couleur couleur a assigner a l'utilisateur
     */
    public void setCouleur(Color couleur) {
        this.couleur = couleur;
    }
}
