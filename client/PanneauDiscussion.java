package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import javax.swing.border.EmptyBorder;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Le PanneauDiscussion herite de JPanel. Le PanneauDiscussion represente la
 * partie basse de la fenetre principale du client.
 * Le PanneauDiscussion est constitue de :
 * - la liste des personnes connectees
 * - la discussion
 * - la zone de texte pour envoyer un message
 * - le bouton pour envoyer un message
 * - une combobox pour choisir a qui envoyer le message
 * - la fenetre
 * - une tableau ou sont stockes les personnes connectees ainsi que leur couleur respective
 * - une option d'envoi (tout le monde ou une personne)
 */
public class PanneauDiscussion extends JPanel {
    /** variable pour la serialisation */
    private static final long serialVersionUID = -8290976470062054660L;

    /** Zone reservee pour afficher les personnes connectees */
    private JTextPane connectes;
    /** Zone reservee pour afficher la discussion */
    private JTextPane discussion;
    /** Zone pour ecrire le message */
    private JTextArea saisieMessage;

    /** Bouton d'envoi d'un message */
    private JButton btn;

    /** Combobox pour choisir le destinataire */
    private JComboBox<String> choixMessage;
    /** Option d'envoi */
    private String optionEnvoi;

    /** Fenetre du client */
    private FenetreClient fenetre;

    /** Stockage des noms des personnes connectees et leurs couleurs associées */
    private ConcurrentHashMap<String, Color> couleurs;

    /**
     * Constructeur qui permet de creer un PanneauDiscussion
     *
     * @param f fenetre du client
     */
    public PanneauDiscussion(FenetreClient f) {
        // Initialisation des attributs du panneau de discussion
        this.couleurs = new ConcurrentHashMap<>();
        this.connectes = new JTextPane();
        this.discussion = new JTextPane();
        this.saisieMessage = new JTextArea(5, 28);
        this.btn = new JButton("Envoyer");
        this.fenetre = f;
        this.choixMessage = new JComboBox<>();
        this.optionEnvoi = "Tout le monde";

        // Ajout de barres de scrolling pour la liste des personnes connectées et la
        // discussion
        JScrollPane scrollConn = new JScrollPane(connectes);
        scrollConn.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JScrollPane scrollDisc = new JScrollPane(discussion);
        scrollDisc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollDisc.setPreferredSize(new Dimension(320, 350));

        // Les zones ou sont ecrit la liste des personnes connectees et celle de la
        // discussion ne sont evidemment pas editables
        this.connectes.setEditable(false);
        this.discussion.setEditable(false);

        // Gestion de la taille de la zone de discussion et la liste des personnes
        // connectees
        this.connectes.setPreferredSize(new Dimension(130, 80));
        this.connectes.setCaretPosition(connectes.getDocument().getLength());

        // Ajout d'un listener sur la zone de texte reservee au message
        this.saisieMessage.getDocument().addDocumentListener(new TextAreaListener());

        this.btn.setEnabled(false);

        // Ajout d'un listener sur le bouton
        this.btn.addActionListener(new ActionListener() {
            /**
             * Methode qui est appelee quand le client va cliquer sur le bouton 'Envoyer'
             * @param e evenement
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                fenetre.getClient().envoyerMessage(getMessage(), optionEnvoi); // Envoie du message
                saisieMessage.setText(""); // On supprime le texte de la saisie message
            }
        });

        JPanel panelConnectes = new JPanel(new BorderLayout(10, 10));
        JPanel panelDiscussion = new JPanel(new BorderLayout(10, 10));
        JPanel panelChoixMessage = new JPanel();
        JPanel panelMessage = new JPanel(new BorderLayout(10, 10));
        JPanel panelDroit = new JPanel(new BorderLayout(10, 10));

        // Gestion de la partie gauche
        panelConnectes.add(new JLabel("Connectes"), BorderLayout.NORTH);
        panelConnectes.add(scrollConn, BorderLayout.CENTER);

        // Gestion du panel avec la combobox
        panelChoixMessage.add(new JLabel("Message a "));
        choixMessage.setPreferredSize(new Dimension(250,25));
        panelChoixMessage.add(choixMessage);

        // Ajout d'un listener sur la combobox
        choixMessage.addActionListener(new ActionListener(){
            /**
             * Methode qui est appelee quand met a jour l'option d'envoi choisi par le
             * client, chaque fois qu'il interagit avec la combobox
             * @param e evenement
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                optionEnvoi = choixMessage.getItemAt(choixMessage.getSelectedIndex());
            }
        });

        // Gestion de la partie droite
        panelDiscussion.add(new JLabel("Discussion"), BorderLayout.NORTH);
        panelDiscussion.add(scrollDisc, BorderLayout.SOUTH);

        panelMessage.add(panelChoixMessage, BorderLayout.NORTH);
        panelMessage.add(saisieMessage, BorderLayout.SOUTH);

        panelDroit.add(panelDiscussion, BorderLayout.NORTH);
        panelDroit.add(panelMessage, BorderLayout.CENTER);
        panelDroit.add(btn, BorderLayout.PAGE_END);

        // Accrochages des panels au PanneauDiscussion
        this.setLayout(new BorderLayout(25, 25));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(panelDroit, BorderLayout.EAST);
        this.add(panelConnectes, BorderLayout.WEST);

        this.setPreferredSize(new Dimension(300, 450));
    }

    /**
     * Methode qui permet d'effacer la discussion (utilise lors de la deconnexion)
     */
    public void effacerDiscussion() {
        discussion.setText("");
    }

    /**
     * Methode qui permet de vider la liste des personnes connectes
     */
    public void effacerConnectes() {
        connectes.setText("");
    }

    /**
     * Methode qui permet de mettre a jour l'affichage des personnes connectees =>
     * Mise a jour du panneau des personnes connectees et mise a jour de la combobox
     */
    public void majAffichageConnectes() {
        effacerConnectes(); // on efface ce qui est deja ecrit dans la zone reservees a la liste des
                            // connectes

        choixMessage.removeAllItems(); // Suppression de tous les items de la comboBox
        choixMessage.addItem("Tout le monde");
        choixMessage.setSelectedIndex(0); // Par defaut => envoi a tout le monde

        // Parcours de la liste des personnes connectees
        for (Map.Entry<String, Color> entry : couleurs.entrySet()) {
            String nom = entry.getKey(); // recuperation du nom
            Color rgb = new Color(chercherCouleur(nom)); // recuperation de la couleur correspondante

            // Mise a jour de la combobox
            if(!fenetre.getClient().getNom().equals(nom)){
                choixMessage.addItem(nom);
            }

            // Mise a jour de l'affichage dans le panneau "Connectes"
            StyledDocument doc = connectes.getStyledDocument();
            StyleContext style = StyleContext.getDefaultStyleContext();
            // Modification de la couleur
            AttributeSet aset = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, rgb);
            // Mettre en gras
            aset = style.addAttributes(aset, style.addAttribute(style.getEmptySet(), StyleConstants.Bold, true));

            // ajout de la personne dans la liste avec la couleur correspondante
            try {
                doc.insertString(doc.getLength(), nom+"\n", aset);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        System.out.println("maj de l'affichage de la liste des connectes");
    }

    /**
     * Methode qui permet de trouver la couleur d'un client donne
     * @param nom nom du client pour qui on cherche sa couleur
     * @return la couleur (sous forme rgb = un entier)
     */
    public int chercherCouleur(String nom){
        // Lecture dans le fichier "utilisateurs.txt"
        try (BufferedReader reader = new BufferedReader(new FileReader("utilisateurs.txt"))) {
            String line = "";

            // Tant qu'on lit des choses dans le fichier
            while ((line = reader.readLine()) != null) {
                String[] tmp = (line.split(":")); // decoupage de la chaine pour recuperer le nom et la couleur associee

                String s = tmp[0]; // on recupere le nom de la personne
                if(s.equalsIgnoreCase(nom)){
                    return Integer.valueOf(tmp[1]); // on recupere la couleur de la personne
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        return 0; //retourne 0 si on n'a pas trouve le nom dans la liste
    }

    /**
     * Methode qui permet d'ajouter un client dans la liste des connectes
     * @param nom nom du client a ajouter
     */
    public void ajouterClientListe(String nom){
        if (!couleurs.containsKey(nom)){
            Color couleur = new Color(chercherCouleur(nom));
            couleurs.put(nom, couleur);
        }
    }

    /**
     * Methode qui permer de vider la liste des connectes
     */
    public void videListeClient(){
        couleurs.clear();
    }

    /**
     * Classe interne pour gerer le listener de la saisie du message
     */
    private class TextAreaListener implements DocumentListener{
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
         * Methode qui permet de verifier si le client a saisi quelque chose dans la zone de texte
         */
        public void verifierSaisie(){
            boolean ok = true;

            // On regarde si le client a ecrit quelque chose
            if(saisieMessage.getText().trim().equals(""))
                ok = false;

            if(ok)
                btn.setEnabled(true); //activer le bouton
            else
                btn.setEnabled(false);  //griser le bouton
        }
    }

    // GETTERS
    /**
     * Getter du message saisi par l'utilisateur
     * @return le message
     */
    public String getMessage(){
        return saisieMessage.getText();
    }

    /**
     * Getter de la discussion
     * @return la discussion
     */
    public JTextPane getDiscussion(){
        return discussion;
    }

    /**
     * Getter de du tableau des noms d'utilisateurs et de leurs couleurs
     * @return le tableau des noms d'utilisateurs associes a leur couleur
     */
    public ConcurrentHashMap<String, Color> getCouleurs() {
        return couleurs;
    }
}
