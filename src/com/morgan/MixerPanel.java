package com.morgan;

import com.morgan.audio.AudioEngine;
import com.morgan.gui.MidiUIFactory;

import javax.swing.*;
import java.awt.*;

public class MixerPanel extends JPanel {

    public MixerPanel(AudioEngine engine, MidiUIFactory factory) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Mixer & Master"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // --- Blend ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Blend (OSC1 <-> OSC2):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int blendInitial = (int) (engine.mixer.blend * 100);
        JSlider blendSlider = new JSlider(0, 100, blendInitial);
        JLabel blendLabel = new JLabel(String.valueOf(blendInitial));

        blendSlider.addChangeListener(factory.createParamListener("mixer.blend", blendSlider, blendLabel, 100, false));

        JPanel blendPanel = new JPanel(new BorderLayout(5, 0));
        blendPanel.add(blendSlider, BorderLayout.CENTER);
        blendPanel.add(blendLabel, BorderLayout.EAST);
        add(blendPanel, gbc);

        // --- Master Volume ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Master Volume:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int volInitial = (int) (engine.masterVolume * 100);
        JSlider volumeSlider = new JSlider(0, 100, volInitial);
        JLabel volumeLabel = new JLabel(String.valueOf(volInitial));

        volumeSlider.addChangeListener(factory.createParamListener("masterVolume", volumeSlider, volumeLabel, 100, false));

        JPanel volumePanel = new JPanel(new BorderLayout(5, 0));
        volumePanel.add(volumeSlider, BorderLayout.CENTER);
        volumePanel.add(volumeLabel, BorderLayout.EAST);
        add(volumePanel, gbc);

        // Spacer
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }
}