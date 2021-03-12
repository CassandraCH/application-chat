package client;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.border.EmptyBorder;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * La Classe PanneauConnexion herite de JPanel Le PanneauConnexion represente la
 * partie haute de la fenetre principale du client, Le PanneauConnexion est constitue de :
 * - une zone de saisie pour le nom,
 * - une zone de saisie pour l'adresse IP
 * - une zone de saisie pour numero de port
 * - un tableau des zones de texte
 * - un bouton pour se connecter/deconnecter
 * - une fenetre
 */
public class PanneauConnexion extends JPanel {
    /** variable pour la serialisation */
    private static final long serialVersionUID = -5033538180186748250L;

    /** Panneau de la saisie du nom du client */
    private SaisieConnexion saisieNom;
    /** Panneau de la saisie du nom de l'adresse IP du client */
    private SaisieConnexion saisieIP;
    /** Panneau de la saisie du numero de port du client */
    private SaisieConnexion saisiePort;

    /** Bouton pour se connecter */
    private JButton btn;
    /** Tableau des 3 zones de texte pour la connexion */
    private JTextField[] tabFields;
    /** Fenetre du client */
    private FenetreClient fenetre;

    /**
     * Constructeur pour creer un PanneauConnexion
     * @param f fenetre du client
     */
    public PanneauConnexion(FenetreClient f) {
        this.fenetre = f;

        // Gestion des zones de saisie
        this.saisieNom = new SaisieConnexion("Nom");
        this.saisieIP = new SaisieConnexion("IP");
        this.saisiePort = new SaisieConnexion("Port");

        // Utilisation d'un tableau pour simplifier le code (plus simple pour gerer les listeners notamment)
        this.tabFields = new JTextField[3];
        // Ajout des zones de saisie dans le tableau
        tabFields[0] = saisieNom.getTextField();
        tabFields[1] = saisieIP.getTextField();
        tabFields[2] = saisiePort.getTextField();

        // Ajout de listeners sur les zones de texte
        for (JTextField field : tabFields)
            field.getDocument().addDocumentListener(new FieldListener());

        this.btn = new JButton("Connexion");

        // Ajout d'un listener sur le bouton
        btn.addActionListener(new ActionListener() {
            /**
             * Methode qui va etre appelee quand le client va cliquer sur le bouton
             * 'Connexion' ou 'Deconnexion'
             *
             * @param e evenement
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cas ou le client se connecte
                if (btn.getText().equals("Connexion")) {
                    for (JTextField field : tabFields)
                        field.setEnabled(false); // bloquer les zones de texte de la connexion
                    btn.setText("Deconnexion");

                    fenetre.afficherDiscussion();

                    // Creation du client et ajout a la fenetre
                    fenetre.setClient(new Client(fenetre, getNom(), getIP(), Integer.parseInt(getPort())));
                    System.out.println("Nouveau client : " + getNom());

                    // Connexion du client au serveur
                    fenetre.getClient().connexion();
                }
                // Cas ou le client se deconnecte
                else {
                    // Deconnexion du client
                    fenetre.getClient().deconnexion();
                    fenetre.setClient(null); // supprimer le client de la fenetre

                    fenetre.effacerDiscussion();
                    fenetre.cacherDiscussion();

                    for (JTextField field : tabFields)
                        field.setEnabled(true); // les zones de saisie pour la connexion sont de nouveau accessible

                    btn.setText("Connexion");
                }
            }
        });

        //Gestion de la partie gauche
        JPanel gauche = new JPanel(new BorderLayout(5, 5));
        gauche.add(saisieNom, BorderLayout.NORTH);
        gauche.add(saisieIP, BorderLayout.SOUTH);
        btn.setEnabled(false); // griser le bouton

        //Gestion de la partie droite
        JPanel droit = new JPanel(new BorderLayout(5, 5));
        droit.add(btn, BorderLayout.NORTH);
        droit.add(saisiePort, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(6, 6, 6, 6));
        this.add(gauche, BorderLayout.WEST);
        this.add(droit, BorderLayout.EAST);
    }

    /**
     * Classe interne pour gerer les listeners des zones de textes
     */
    private class FieldListener implements DocumentListener {
        /**
         * Methode qui permet de savoir si quelque chose a ete ajoute au document
         * @param e evenement
         */
        @Override
        public void insertUpdate(DocumentEvent e) {
            verifierSaisie();
        }

        /**
         * Methode qui permet de savoir si quelque chose a ete supprime du document
         * @param e evenement
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            verifierSaisie();
        }

        /**
         * Methode qui permet de savoir s'il y eu des changements
         * @param e evenement
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            verifierSaisie();
        }

        /**
         * Methode qui permet de verifier que le client a bien rempli tous les champs de connexion
         */
        public void verifierSaisie() {
            boolean ok = true;

            for(JTextField field : tabFields)
                if (field.getText().trim().equals(""))
                    ok = false;
            // Pour que le bouton de connexion soit dispo, il faut que le client ait rempli tous les champs
            if(ok)
                btn.setEnabled(true); // activer le bouton
            else
                btn.setEnabled(false); // griser le bouton
        }
    }

    // GETTERS
    /**
     * Getter de l'IP saisi par le client
     * @return l'adresse IP
     */
    public String getIP() {
        return saisieIP.getSaisie();
    }

    /**
     * Getter du numero de port saisi par le client
     * @return le numero de port saisi
     */
    public String getPort() {
        return saisiePort.getSaisie();
    }

    /**
     * Getter du nom du client
     * @return le nom du client
     */
    public String getNom() {
        return saisieNom.getSaisie();
    }
}
