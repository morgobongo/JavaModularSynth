package com.morgan.gui;

import com.morgan.audio.Filter;
import com.morgan.gui.MidiUIFactory;

import javax.swing.*;
import java.awt.*;

public class FilterPanel extends JPanel {

    public FilterPanel(Filter filter, MidiUIFactory factory) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Filter (VCF)"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);

        // Cutoff
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Cutoff (Hz):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int cutoffInitial = (int) filter.cutoffFrequency;
        JSlider cutoffSlider = new JSlider(50, 20000, cutoffInitial);
        JLabel cutoffLabel = new JLabel(String.valueOf(cutoffInitial));

        cutoffSlider.addChangeListener(factory.createParamListener("filter.cutoff", cutoffSlider, cutoffLabel, 19950, false));

        JPanel cutoffPanel = new JPanel(new BorderLayout(5, 0));
        cutoffPanel.add(cutoffSlider, BorderLayout.CENTER);
        cutoffPanel.add(cutoffLabel, BorderLayout.EAST);
        add(cutoffPanel, gbc);

        // Resonance
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("Résonance (Q):"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        int resInitial = (int) (filter.resonance * 100);
        JSlider resonanceSlider = new JSlider(0, 100, resInitial);
        JLabel resonanceLabel = new JLabel(String.format("%.2f", filter.resonance));

        resonanceSlider.addChangeListener(factory.createParamListener("filter.resonance", resonanceSlider, resonanceLabel, 100, false));

        JPanel resonancePanel = new JPanel(new BorderLayout(5, 0));
        resonancePanel.add(resonanceSlider, BorderLayout.CENTER);
        resonancePanel.add(resonanceLabel, BorderLayout.EAST);
        add(resonancePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }
}