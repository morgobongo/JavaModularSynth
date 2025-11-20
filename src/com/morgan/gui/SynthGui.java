package com.morgan.gui;

import com.morgan.MixerPanel;
import com.morgan.audio.AudioEngine;
import com.morgan.main.SynthMain;
import com.morgan.sequenceur.StepSequencer;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * SynthGui.java - Version Refactorisée
 * Orchestrateur principal. Implémente MidiUIFactory pour fournir
 * la logique de mapping MIDI aux panneaux enfants.
 */
public class SynthGui extends JFrame implements MidiUIFactory {

    private AudioEngine audioEngine;
    private StepSequencer sequencer;
    private LearnPanel learnPanel;

    public SynthGui(AudioEngine engine, StepSequencer seq) {
        this.audioEngine = engine;
        this.sequencer = seq;

        setTitle("Synthétiseur Modulaire (Refactored)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainRack = new JPanel();
        mainRack.setLayout(new BoxLayout(mainRack, BoxLayout.Y_AXIS));

        // Initialisation des panneaux
        // Note: 'this' est passé comme MidiUIFactory
        learnPanel = new LearnPanel();

        mainRack.add(learnPanel);
        mainRack.add(new MixerPanel(audioEngine, this));
        mainRack.add(new EnvelopePanel(audioEngine.vcaEnv, this));
        mainRack.add(new OscillatorPanel("OSC 1", audioEngine.osc1, this));
        mainRack.add(new OscillatorPanel("OSC 2", audioEngine.osc2, this));
        mainRack.add(new FilterPanel(audioEngine.filter, this));
        mainRack.add(new SequencerPanel(sequencer));
        mainRack.add(new StepGridPanel(sequencer));

        JScrollPane scrollPane = new JScrollPane(mainRack);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        setPreferredSize(new Dimension(800, 850));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- Implémentation de MidiUIFactory ---

    @Override
    public ChangeListener createParamListener(String paramName, JSlider slider, JLabel label, int maxRange, boolean isCents) {
        return e -> {
            int sliderValue = slider.getValue();

            // 1) MIDI Learn Global Check
            if (SynthMain.MAPPING_STATE.awaitingParameter != null && SynthMain.MAPPING_STATE.awaitingParameter.equals("ACTIF")) {
                SynthMain.MAPPING_STATE.awaitingParameter = paramName;
                // Mise à jour du texte du bouton via l'objet learnPanel
                learnPanel.updateButtonText("Prêt: bougez un CC MIDI pour '" + paramName + "'");
                System.out.println("GUI: contrôle sélectionné pour apprentissage => " + paramName);
                return;
            }

            // 2) Traduction Valeur
            int midiValue;
            if (maxRange <= 0) {
                midiValue = 0;
            } else {
                midiValue = (int) (((double) sliderValue / (double) maxRange) * 127.0);
                if (midiValue < 0) midiValue = 0;
                if (midiValue > 127) midiValue = 127;
            }

            // Envoi moteur
            audioEngine.handleMappedControl(paramName, midiValue);

            // 3) Update UI Label
            if (isCents) {
                label.setText(String.valueOf(sliderValue));
            } else if (maxRange == 100) {
                double val = sliderValue / (double) maxRange;
                label.setText(String.format("%.2f", val));
            } else {
                label.setText(String.valueOf(sliderValue));
            }
        };
    }

    @Override
    public ChangeListener createTimeListener(String paramName, JSlider slider, JLabel label) {
        return e -> {
            int sliderValue = slider.getValue();

            // 1. Logique MIDI Learn
            if (SynthMain.MAPPING_STATE.awaitingParameter != null && SynthMain.MAPPING_STATE.awaitingParameter.equals("ACTIF")) {
                SynthMain.MAPPING_STATE.awaitingParameter = paramName;
                learnPanel.updateButtonText("Prêt! Bougez CC# pour " + paramName);
                return;
            }

            // 2. Calcul du temps pour l'affichage
            double timeValue = sliderValue / 50.0;
            label.setText(String.format("%.2f", timeValue));

            // 3. Conversion en valeur MIDI
            int midiValue = (int)(((double)sliderValue / 100.0) * 127.0);

            // 4. Mise à jour moteur
            audioEngine.handleMappedControl(paramName, midiValue);
        };
    }
}