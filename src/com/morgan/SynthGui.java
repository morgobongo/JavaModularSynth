package com.morgan;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * SynthGui.java
 * Version complète et fusionnée du GUI avec MIDI Learn, panels complets et listeners
 * Dépendances: AudioEngine, StepSequencer, Oscillator, Mixer, Filter, EnvelopeGenerator, ScaleType, SynthMain
 */
public class SynthGui extends JFrame {

    private AudioEngine audioEngine;
    private StepSequencer sequencer;
    private JButton learnButton;

    // --- Classe interne pour représenter une note dans les ComboBox ---
    private static class NoteEntry {
        String name;
        int midiValue;
        public NoteEntry(String name, int midiValue) { this.name = name; this.midiValue = midiValue; }
        @Override public String toString() { return name; }
        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NoteEntry noteEntry = (NoteEntry) obj;
            return midiValue == noteEntry.midiValue;
        }
        @Override public int hashCode() { return Integer.hashCode(midiValue); }
    }

    public SynthGui(AudioEngine engine, StepSequencer seq) {
        this.audioEngine = engine;
        this.sequencer = seq;

        setTitle("Synthétiseur Modulaire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainRack = new JPanel();
        mainRack.setLayout(new BoxLayout(mainRack, BoxLayout.Y_AXIS));

        // Ordre des panneaux - le nouveau panneau Learn est ajouté en premier
        mainRack.add(createLearnControlPanel());
        mainRack.add(createMixerPanel());   // Inclut Master Volume
        mainRack.add(createEnvelopePanel());
        mainRack.add(createOscillatorPanel("OSC 1", audioEngine.osc1));
        mainRack.add(createOscillatorPanel("OSC 2", audioEngine.osc2));
        mainRack.add(createFilterPanel());  // Utilise GridBagLayout
        mainRack.add(createSequencerPanel());
        mainRack.add(createStepGridPanel());

        JScrollPane scrollPane = new JScrollPane(mainRack);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        setPreferredSize(new Dimension(800, 850));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // --- NOUVEAU PANNEAU : Activation du MIDI Learn ---
    private JPanel createLearnControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("MIDI Learn"));

        learnButton = new JButton("Activer MIDI Learn");
        learnButton.addActionListener(new LearnButtonListener());

        panel.add(learnButton);
        panel.add(new JLabel("Cliquez sur un contrôle GUI puis bougez un CC MIDI pour l'assigner."));

        return panel;
    }

    private class LearnButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (SynthMain.MAPPING_STATE.awaitingParameter != null) {
                // Si l'apprentissage était actif, on désactive
                SynthMain.MAPPING_STATE.awaitingParameter = null;
                learnButton.setText("Activer MIDI Learn");
                System.out.println("Mode MIDI Learn désactivé.");
            } else {
                // Activation en mode "global" - l'utilisateur doit ensuite cliquer le contrôle GUI
                SynthMain.MAPPING_STATE.awaitingParameter = "ACTIF"; // Marqueur générique
                learnButton.setText("APPRENTISSAGE ACTIF: Cliquez un contrôle GUI");
                System.out.println("Mode MIDI Learn activé. Cliquez un contrôle dans la GUI.");
            }
        }
    }

    // --- Créateur de ChangeListener générique pour sliders qui supporte MIDI Learn ---
    private ChangeListener createParameterChangeListener(String paramName, JSlider slider, JLabel label, int maxRange, boolean isCents) {
        return e -> {
            int sliderValue = slider.getValue();

            // 1) MIDI Learn: si on est en mode "ACTIF", on attend que l'utilisateur clique un contrôle GUI
            if (SynthMain.MAPPING_STATE.awaitingParameter != null && SynthMain.MAPPING_STATE.awaitingParameter.equals("ACTIF")) {
                // On assigne le nom de paramètre à attendre par le receveur MIDI
                SynthMain.MAPPING_STATE.awaitingParameter = paramName;
                learnButton.setText("Prêt: bougez un CC MIDI pour '" + paramName + "'");
                System.out.println("GUI: contrôle sélectionné pour apprentissage => " + paramName);
                return; // Pas d'action de contrôle tant que le mapping n'est pas finalisé
            }

            // 2) Traduction de la valeur du slider en valeur MIDI (0-127)
            //    maxRange représente l'intervalle entier du slider (ex: 100 pour 0..100, 200 pour -100..100, 19950 pour 50..20000 etc.)
            int midiValue;
            if (maxRange <= 0) {
                midiValue = 0;
            } else {
                // On normalise selon l'intervalle du slider
                // Si le slider a un offset (par ex. -100..100), l'appelant doit s'être assuré que min..max sont mappés en conséquence
                midiValue = (int) (((double) sliderValue / (double) maxRange) * 127.0);
                if (midiValue < 0) midiValue = 0;
                if (midiValue > 127) midiValue = 127;
            }

            // Envoi au moteur
            audioEngine.handleMappedControl(paramName, midiValue);

            // 3) Mise à jour du label affiché
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

    // Dans SynthGui.java (avant createMixerPanel)

    // Crée un ChangeListener spécifique pour Attack et Release (Temps)
    private ChangeListener createEnvTimeChangeListener(String paramName, JSlider slider, JLabel label) {
        return e -> {
            int sliderValue = slider.getValue();

            // 1. Logique MIDI Learn
            if (SynthMain.MAPPING_STATE.awaitingParameter != null && SynthMain.MAPPING_STATE.awaitingParameter.equals("ACTIF")) {
                SynthMain.MAPPING_STATE.awaitingParameter = paramName;
                learnButton.setText("Prêt! Bougez CC# pour " + paramName);
                return;
            }

            // 2. Calcul du temps pour l'affichage (0-100 -> 0.0-2.0s)
            double timeValue = sliderValue / 50.0;
            label.setText(String.format("%.2f", timeValue));

            // 3. Conversion en valeur MIDI (0-127) pour le moteur
            int midiValue = (int)(((double)sliderValue / 100.0) * 127.0);

            // 4. Mise à jour centralisée via handleMappedControl
            audioEngine.handleMappedControl(paramName, midiValue);
        };
    }

    // --- PANNEAU MIXER (Intègre Master Volume) ---
    private JPanel createMixerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Mixer & Master"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // Espacement

        // Ligne 1: Blend (OSC1 <-> OSC2)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Blend (OSC1 <-> OSC2):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int blendInitial = (int) (audioEngine.mixer.blend * 100);
        JSlider blendSlider = new JSlider(0, 100, blendInitial);
        JLabel blendLabel = new JLabel(String.valueOf(blendInitial));
        // Utilisation du listener générique (maxRange=100)
        blendSlider.addChangeListener(createParameterChangeListener("mixer.blend", blendSlider, blendLabel, 100, false));
        JPanel blendPanel = new JPanel(new BorderLayout(5,0));
        blendPanel.add(blendSlider, BorderLayout.CENTER);
        blendPanel.add(blendLabel, BorderLayout.EAST);
        panel.add(blendPanel, gbc);

        // Ligne 2: Master Volume
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Master Volume:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int volInitial = (int) (audioEngine.masterVolume * 100);
        JSlider volumeSlider = new JSlider(0, 100, volInitial);
        JLabel volumeLabel = new JLabel(String.valueOf(volInitial));
        volumeSlider.addChangeListener(createParameterChangeListener("masterVolume", volumeSlider, volumeLabel, 100, false));
        JPanel volumePanel = new JPanel(new BorderLayout(5,0));
        volumePanel.add(volumeSlider, BorderLayout.CENTER);
        volumePanel.add(volumeLabel, BorderLayout.EAST);
        panel.add(volumePanel, gbc);

        // Panel vide pour prendre l'espace restant
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    // --- PANNEAU FILTRE (Nouvelle Disposition) ---
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Filter (VCF)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // Cutoff (Hz) - slider large. Pour le mapping vers 0-127 on utilise maxRange = 19950 (20000-50)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Cutoff (Hz):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int cutoffInitial = (int) audioEngine.filter.cutoffFrequency;
        JSlider cutoffSlider = new JSlider(50, 20000, cutoffInitial);
        JLabel cutoffLabel = new JLabel(String.valueOf(cutoffInitial));
        // On passe maxRange = 19950 pour que createParameterChangeListener calcule midiValue correctement
        cutoffSlider.addChangeListener(createParameterChangeListener("filter.cutoff", cutoffSlider, cutoffLabel, 19950, false));
        JPanel cutoffPanel = new JPanel(new BorderLayout(5,0));
        cutoffPanel.add(cutoffSlider, BorderLayout.CENTER);
        cutoffPanel.add(cutoffLabel, BorderLayout.EAST);
        panel.add(cutoffPanel, gbc);

        // Resonance (Q) 0..1 mapped to slider 0..100 (maxRange=100)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Résonance (Q):"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int resInitial = (int) (audioEngine.filter.resonance * 100);
        JSlider resonanceSlider = new JSlider(0, 100, resInitial);
        JLabel resonanceLabel = new JLabel(String.format("%.2f", audioEngine.filter.resonance));
        resonanceSlider.addChangeListener(createParameterChangeListener("filter.resonance", resonanceSlider, resonanceLabel, 100, false));
        JPanel resonancePanel = new JPanel(new BorderLayout(5,0));
        resonancePanel.add(resonanceSlider, BorderLayout.CENTER);
        resonancePanel.add(resonanceLabel, BorderLayout.EAST);
        panel.add(resonancePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    // --- PANNEAU ENVELOPE (AR) ---
    // --- PANNEAU ENVELOPE (FIXÉ pour l'Alignement) ---
    private JPanel createEnvelopePanel() {
        // Utilisation du GridLayout simple (2 colonnes) pour un empilement vertical stable
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Envelope (AR)"));

        // Ligne 1: Attack (s)
        panel.add(new JLabel("Attack (s):")); // Colonne 1

        int attackInitial = (int) (audioEngine.vcaEnv.attackTime * 50);
        JSlider attackSlider = new JSlider(0, 100, attackInitial);
        JLabel attackLabel = new JLabel(String.format("%.2f", audioEngine.vcaEnv.attackTime));

        // Listener
        attackSlider.addChangeListener(createEnvTimeChangeListener("envelope.attack", attackSlider, attackLabel));

        // Empaquetage : Slider + Label de Valeur dans un sous-panneau (Colonne 2)
        JPanel attackPanel = new JPanel(new BorderLayout(5, 0));
        attackPanel.add(attackSlider, BorderLayout.CENTER);
        attackPanel.add(attackLabel, BorderLayout.EAST);
        panel.add(attackPanel);

        // Ligne 2: Release (s)
        panel.add(new JLabel("Release (s):")); // Colonne 1

        int releaseInitial = (int) (audioEngine.vcaEnv.releaseTime * 50);
        JSlider releaseSlider = new JSlider(0, 100, releaseInitial);
        JLabel releaseLabel = new JLabel(String.format("%.2f", audioEngine.vcaEnv.releaseTime));

        // Listener
        releaseSlider.addChangeListener(createEnvTimeChangeListener("envelope.release", releaseSlider, releaseLabel));

        // Empaquetage : Slider + Label de Valeur dans un sous-panneau (Colonne 2)
        JPanel releasePanel = new JPanel(new BorderLayout(5, 0));
        releasePanel.add(releaseSlider, BorderLayout.CENTER);
        releasePanel.add(releaseLabel, BorderLayout.EAST);
        panel.add(releasePanel);

        // Aucune cellule vide n'est nécessaire car GridLayout(0, 2) est parfaitement rempli.
        return panel;
    }

    // --- PANNEAU OSCILLATEUR ---
    private JPanel createOscillatorPanel(String title, Oscillator osc) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        // Waveform
        panel.add(new JLabel("Forme d'onde:"));
        JComboBox<Oscillator.Waveform> waveBox = new JComboBox<>(Oscillator.Waveform.values());
        waveBox.setSelectedItem(osc.waveform);
        waveBox.addActionListener(e -> {
            osc.setWaveform((Oscillator.Waveform) waveBox.getSelectedItem());
        });
        panel.add(waveBox);

        // Octave
        panel.add(new JLabel("Octave:"));
        JSlider octaveSlider = new JSlider(-2, 2, osc.octave);
        octaveSlider.setMajorTickSpacing(1);
        octaveSlider.setPaintTicks(true);
        octaveSlider.setPaintLabels(true);
        octaveSlider.setSnapToTicks(true);
        octaveSlider.addChangeListener(e -> {
            osc.octave = octaveSlider.getValue();
            osc.updateFrequency();
        });
        panel.add(octaveSlider);

        // Pitch (Cents)
        panel.add(new JLabel("Pitch (cents):"));
        JSlider pitchSlider = new JSlider(-100, 100, osc.pitchCents);
        JLabel pitchLabel = new JLabel(String.valueOf(osc.pitchCents));
        // maxRange = 200 (pour -100..100). isCents = true
        pitchSlider.addChangeListener(createParameterChangeListener(title.equals("OSC 1") ? "osc1.pitch" : "osc2.pitch", pitchSlider, pitchLabel, 200, true));
        panel.add(pitchSlider);
        panel.add(pitchLabel);

        return panel;
    }

    // --- SEQUENCER PANEL ---
    private JPanel createSequencerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sequencer"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // Start/Stop + BPM
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JButton startStopButton = new JButton("Start");
        startStopButton.addActionListener(e -> {
            if (sequencer.isRunning()) {
                sequencer.stop(); startStopButton.setText("Start");
            } else {
                sequencer.start(); startStopButton.setText("Stop");
            }
        });
        panel.add(startStopButton, gbc);

        gbc.gridx = 1; gbc.gridy = 0; panel.add(new JLabel("BPM:"), gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel bpmPanel = new JPanel(new BorderLayout(5,0));
        JSlider bpmSlider = new JSlider(60, 240, (int) sequencer.bpm);
        JLabel bpmLabel = new JLabel(String.valueOf((int) sequencer.bpm));
        bpmSlider.addChangeListener(e -> {
            sequencer.bpm = bpmSlider.getValue();
            bpmLabel.setText(String.valueOf(bpmSlider.getValue()));
        });
        bpmPanel.add(bpmSlider, BorderLayout.CENTER);
        bpmPanel.add(bpmLabel, BorderLayout.EAST);
        panel.add(bpmPanel, gbc);

        // Root note selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tonalité :"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<NoteEntry> rootNoteBox = new JComboBox<>();
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        NoteEntry defaultNote = null;
        for (int midiVal = 48; midiVal <= 72; midiVal++) {
            int noteIndex = midiVal % 12;
            int octave = (midiVal / 12) - 1;
            String noteName = noteNames[noteIndex] + octave;
            NoteEntry entry = new NoteEntry(noteName, midiVal);
            rootNoteBox.addItem(entry);
            if (midiVal == sequencer.rootNote) defaultNote = entry;
        }
        if (defaultNote != null) rootNoteBox.setSelectedItem(defaultNote);
        rootNoteBox.addActionListener(e -> {
            NoteEntry selected = (NoteEntry) rootNoteBox.getSelectedItem();
            if (selected != null) {
                sequencer.rootNote = selected.midiValue;
            }
        });
        panel.add(rootNoteBox, gbc);

        // Scale selection
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Gamme (Mode):"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<ScaleType> scaleBox = new JComboBox<>(ScaleType.values());
        scaleBox.setSelectedItem(sequencer.currentScale);
        scaleBox.addActionListener(e -> sequencer.currentScale = (ScaleType) scaleBox.getSelectedItem());
        panel.add(scaleBox, gbc);

        // Steps
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Nombre de Pas:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JSlider stepsSlider = new JSlider(1, 16, sequencer.numSteps);
        stepsSlider.setMajorTickSpacing(4);
        stepsSlider.setMinorTickSpacing(1);
        stepsSlider.setPaintTicks(true);
        stepsSlider.setPaintLabels(true);
        stepsSlider.setSnapToTicks(true);
        stepsSlider.addChangeListener(e -> sequencer.numSteps = stepsSlider.getValue());
        panel.add(stepsSlider, gbc);

        // Random checkboxes
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JCheckBox randomNotes = new JCheckBox("Notes Aléatoires");
        randomNotes.setSelected(sequencer.randomNotes);
        randomNotes.addActionListener(e -> sequencer.randomNotes = randomNotes.isSelected());
        panel.add(randomNotes, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        JCheckBox randomRhythm = new JCheckBox("Rythme Aléatoire");
        randomRhythm.setSelected(sequencer.randomRhythm);
        randomRhythm.addActionListener(e -> sequencer.randomRhythm = randomRhythm.isSelected());
        panel.add(randomRhythm, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    // --- Step Grid ---
    private JPanel createStepGridPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 16, 2, 2));
        panel.setBorder(BorderFactory.createTitledBorder("Step Grid (Degré / Gate)"));
        panel.setPreferredSize(new Dimension(750, 80));

        for (int i = 0; i < 16; i++) {
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                    sequencer.scaleDegrees[i], -12, 12, 1 );
            JSpinner noteSpinner = new JSpinner(spinnerModel);
            final int stepIndex = i;
            noteSpinner.addChangeListener(e -> sequencer.scaleDegrees[stepIndex] = (int) noteSpinner.getValue());
            noteSpinner.setEditor(new JSpinner.NumberEditor(noteSpinner, "#"));
            panel.add(noteSpinner);
        }

        for (int i = 0; i < 16; i++) {
            JCheckBox gateCheck = new JCheckBox();
            gateCheck.setSelected(sequencer.gates[i]);
            final int stepIndex = i;
            gateCheck.addActionListener(e -> sequencer.gates[stepIndex] = gateCheck.isSelected());
            gateCheck.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(gateCheck);
        }
        return panel;
    }

    // --- Helper for envelope slider creation (if needed elsewhere) ---
    private JSlider createEnvSlider(String name, int value, ChangeListener listener) {
        JSlider slider = new JSlider(0, 100, value);
        slider.setName(name);
        slider.addChangeListener(listener);
        return slider;
    }
}
