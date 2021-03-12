package client;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * La classe SaisieConnexion herite de JPanel. La classe SaisieConnexion permet
 * de gerer le placement du label et la zone de texte lorsque le client veut se connecter.
 * Une SaisieConnexion est constituee de :
 * - un label
 * - une zone de texte
 */
public class SaisieConnexion extends JPanel {
    /** variable pour la serialisation */
    private static final long serialVersionUID = 6441540400102248260L;

    /** Label de la zone de saisie */
    private JLabel label;
    /** Zone reservee a la saisie */
    private JTextField saisie;

    /**
     * Constructeur qui permet de creer une SaisieConnexion
     * @param nomLabel nom du label
     */
    public SaisieConnexion(String nomLabel) {
        this.label = new JLabel(nomLabel);
        this.saisie = new JTextField(15);

        // Gestion du positionement
        this.setLayout(new FlowLayout(FlowLayout.TRAILING, 15, 5));

        // Ajout du label et de la zone de saisie
        this.add(label);
        this.add(saisie);
    }

    // GETTERS
    /**
     * Getter de la saisie du client
     * @return la saisie du client
     */
    public String getSaisie() {
        return saisie.getText();
    }

    /**
     * Getter du JTextField
     * @return le JTextField
     */
    public JTextField getTextField(){
        return saisie;
    }
}