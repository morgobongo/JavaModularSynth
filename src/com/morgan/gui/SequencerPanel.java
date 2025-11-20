package com.morgan.gui;

import com.morgan.sequenceur.ScaleType;
import com.morgan.sequenceur.StepSequencer;

import javax.swing.*;
import java.awt.*;

public class SequencerPanel extends JPanel {

    public SequencerPanel(StepSequencer sequencer) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Sequencer"));

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
        add(startStopButton, gbc);

        gbc.gridx = 1; gbc.gridy = 0; add(new JLabel("BPM:"), gbc);

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
        add(bpmPanel, gbc);

        // Root note
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Tonalité :"), gbc);

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
            if (selected != null) sequencer.rootNote = selected.midiValue;
        });
        add(rootNoteBox, gbc);

        // Scale
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Gamme (Mode):"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox<ScaleType> scaleBox = new JComboBox<>(ScaleType.values());
        scaleBox.setSelectedItem(sequencer.currentScale);
        scaleBox.addActionListener(e -> sequencer.currentScale = (ScaleType) scaleBox.getSelectedItem());
        add(scaleBox, gbc);

        // Steps
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Nombre de Pas:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JSlider stepsSlider = new JSlider(1, 16, sequencer.numSteps);
        stepsSlider.setMajorTickSpacing(4);
        stepsSlider.setMinorTickSpacing(1);
        stepsSlider.setPaintTicks(true);
        stepsSlider.setPaintLabels(true);
        stepsSlider.setSnapToTicks(true);
        stepsSlider.addChangeListener(e -> sequencer.numSteps = stepsSlider.getValue());
        add(stepsSlider, gbc);

        // Random
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        JCheckBox randomNotes = new JCheckBox("Notes Aléatoires");
        randomNotes.setSelected(sequencer.randomNotes);
        randomNotes.addActionListener(e -> sequencer.randomNotes = randomNotes.isSelected());
        add(randomNotes, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        JCheckBox randomRhythm = new JCheckBox("Rythme Aléatoire");
        randomRhythm.setSelected(sequencer.randomRhythm);
        randomRhythm.addActionListener(e -> sequencer.randomRhythm = randomRhythm.isSelected());
        add(randomRhythm, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }
}