package serveur;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;

/**
 * La classe FenetreServeur herite de JPanel La classe FenetreServeur gere l'affichage sur la fenetre du serveur.
 * La FenetreServeur est constituee de :
 * - une zone pour afficher les utilisateurs
 * - une zone pour afficher la discussion
 * - un statut
 */
public class FenetreServeur extends JPanel {
    /** variable pour la serialisation */
    private static final long serialVersionUID = 6516970438105632210L;

    /** Zone reservee pour afficher les utilisateurs connectes au serveur */
    private JTextArea utilisateurs;
    /** Zone reservee pour afficher la discussion en cours */
    private JTextArea discussion;
    /** Zone pour le statut du serveur */
    private JLabel statut;

    /**
     * Programme princiapl qui permet d'ouvrir une fenetre cote Serveur
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        new Serveur(Serveur.port).lancer();
    }

    /**
     * Constructeur pour creer une FenetreServeur
     */
    public FenetreServeur(){
        this.statut = new JLabel();
        this.utilisateurs = new JTextArea(10,35);
        this.discussion = new JTextArea(17,35);
        this.discussion.setLineWrap(true);

        // Ajout des barres de scrolling
        JScrollPane scrollDis = new JScrollPane(discussion);
        scrollDis.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollPane scrollUtil = new JScrollPane(utilisateurs);
        scrollUtil.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // les zones pour afficher les utilisateurs et la discussion ne sont pas editables
        this.utilisateurs.setEditable(false);
        this.discussion.setEditable(false);

        // Gestion du panneau avec les utilisateurs du serveur
        JPanel panelUtil = new JPanel(new BorderLayout(5,5));
        panelUtil.add(new JLabel("Utilisateurs actifs"), BorderLayout.NORTH);
        panelUtil.add(scrollUtil, BorderLayout.CENTER);

        // Gestion du panneau de discussion
        JPanel panelDisc = new JPanel(new BorderLayout(5, 5));
        panelDisc.add(new JLabel("Discussion"), BorderLayout.NORTH);
        panelDisc.add(scrollDis, BorderLayout.CENTER);

        // Ajout et positionnement des elements au panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.add(statut, BorderLayout.NORTH);
        panel.add(panelUtil,BorderLayout.CENTER);
        panel.add(panelDisc, BorderLayout.PAGE_END);

        add(panel); // Ajout du panel principale a la fenetre
    }

    /**
     * Methode qui permet de supprimer la liste des utilisateurs
     */
    public void effacerUtilisateursFenetre(){
        utilisateurs.setText(""); //effacer tout le contenu
    }

    /**
     * Methode qui permet d'ajouter un utilisateur dans la liste des connectes
     * @param nom nom a ajouter a la liste
     */
    public void ajouterUtilisateurFenetre(String nom){
        utilisateurs.append(nom+"\n");
    }

    // GETTER
    /**
     * Getter de la discussion
     * @return la zone ou il y a la discussion
     */
    public JTextArea getDiscussion() {
        return discussion;
    }

    // SETTER
    /**
     * Methode qui permet de regler le statut du serveur
     * @param msg le statut
     */
    public void setStatut(String msg) {
        this.statut.setText(msg);
    }
}
