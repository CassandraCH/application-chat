package client;
import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * La classe FenetreClient herite de JFrame.
 * La classe FenetreClient permet de creer une fenetre
 * et de placer les panneaux correctement => Gestion de la vue Client
 * Une fenetre possede :
 * - un panneau de connexion
 * - un panneau de discussion
 * - un client
 */
public class FenetreClient extends JFrame {
    /** variable pour la serialisation */
    private static final long serialVersionUID = -1564000638441253841L;

    /** Zone reservee pour l'affichage du formulaire de connexion */
    private PanneauConnexion panneauConnexion;
    /** Zone reservee pour afficher tout ce qui concerne la discussion */
    private PanneauDiscussion panneauDiscussion;
    /** Client de la fenetre */
    private Client client;

    /**
     * Programme prinicpal qui permet d'ouvrir une fenetre cote Client
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        // permet de ne pas bloquer l'interface graphique
        EventQueue.invokeLater(new Runnable() {
            /**
             * Redefinition de la methode run <=> methode qui va s'executer
             * Son but est de creer une fenetre pour le client
             */
            @Override
            public void run() {
                new FenetreClient();
            }
        });
    }
    /**
     * Constructeur pour creer une fenetre.
     * Permet de placer correctement les panneaux de connexion et de discussion dans la fenetre
     */
    public FenetreClient() {
        super("Chat en Ligne");
        this.setLayout(new BorderLayout(10, 10));

        this.panneauConnexion = new PanneauConnexion(this);
        this.panneauDiscussion = new PanneauDiscussion(this);
        this.add(panneauConnexion, BorderLayout.NORTH);
        this.add(panneauDiscussion, BorderLayout.CENTER);
        cacherDiscussion(); // tant que le client ne s'est pas connecte => on cache le panneau de discussion

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //gestion de la fermeture de la fenetre

        // Si le client ferme la fenetre sans s'etre deconnecte => on le deconnecte
        addWindowListener(new WindowAdapter(){
            /**
             * Methode qui est executee en cas de fermeture de la fenetre
             */
            @Override
            public void windowClosed(WindowEvent e) {
                if((client != null) && client.getConnecte()){
                    client.deconnexion();
                }
            }
        });

        this.setSize(new Dimension(540, 700)); // regler la taille
        this.setLocationRelativeTo(null); // centrer la fenetre
        this.setVisible(true); // afficher
    }

    /**
     * Methode qui permet d'afficher le panneau de discussion
     */
    public void afficherDiscussion(){
        panneauDiscussion.setVisible(true);
    }

    /**
     * Methode qui permet de cacher le panneau de discussion
     */
    public void cacherDiscussion(){
        panneauDiscussion.setVisible(false);
    }

    /**
     * Methode qui permet d'effacer la discussion (utilise quand le client se reconnecte)
     */
    public void effacerDiscussion(){
        panneauDiscussion.effacerDiscussion();
    }

    // GETTERS
    /**
     * Getter pour le panneau de discussion
     * @return le panneau de discussion
     */
    public PanneauDiscussion getPanneauDiscussion(){
        return panneauDiscussion;
    }

    /**
     * Getter du client de la fenetre
     * @return le client de la fenetre
     */
    public Client getClient() {
        return client;
    }

    //SETTER
    /**
     * Permet d'affecter un nouveau client a la fenetre
     * @param client le client a affecter a la fenetre
     */
    public void setClient(Client client) {
        this.client = client;
    }
}
