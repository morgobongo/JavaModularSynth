package com.morgan.gui;

import com.morgan.main.SynthMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LearnPanel extends JPanel {
    private JButton learnButton;

    public LearnPanel() {
        super(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("MIDI Learn"));

        learnButton = new JButton("Activer MIDI Learn");

        // Logique locale au bouton mais impactant l'état global SynthMain
        learnButton.addActionListener(e -> toggleLearnMode());

        add(learnButton);
        add(new JLabel("Cliquez sur un contrôle GUI puis bougez un CC MIDI pour l'assigner."));
    }

    private void toggleLearnMode() {
        if (SynthMain.MAPPING_STATE.awaitingParameter != null) {
            // Désactivation
            SynthMain.MAPPING_STATE.awaitingParameter = null;
            learnButton.setText("Activer MIDI Learn");
            System.out.println("Mode MIDI Learn désactivé.");
        } else {
            // Activation
            SynthMain.MAPPING_STATE.awaitingParameter = "ACTIF";
            learnButton.setText("APPRENTISSAGE ACTIF: Cliquez un contrôle GUI");
            System.out.println("Mode MIDI Learn activé. Cliquez un contrôle dans la GUI.");
        }
    }

    // Méthode publique pour permettre à SynthGui de mettre à jour le texte du bouton
    public void updateButtonText(String text) {
        learnButton.setText(text);
    }
}