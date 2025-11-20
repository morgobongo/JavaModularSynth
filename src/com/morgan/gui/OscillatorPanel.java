package com.morgan.gui;

import com.morgan.audio.Oscillator;
import com.morgan.gui.MidiUIFactory;

import javax.swing.*;
import java.awt.*;

public class OscillatorPanel extends JPanel {

    public OscillatorPanel(String title, Oscillator osc, MidiUIFactory factory) {
        super(new GridLayout(0, 2, 10, 5));
        setBorder(BorderFactory.createTitledBorder(title));

        // Waveform
        add(new JLabel("Forme d'onde:"));
        JComboBox<Oscillator.Waveform> waveBox = new JComboBox<>(Oscillator.Waveform.values());
        waveBox.setSelectedItem(osc.waveform);
        waveBox.addActionListener(e -> {
            osc.setWaveform((Oscillator.Waveform) waveBox.getSelectedItem());
        });
        add(waveBox);

        // Octave
        add(new JLabel("Octave:"));
        JSlider octaveSlider = new JSlider(-2, 2, osc.octave);
        octaveSlider.setMajorTickSpacing(1);
        octaveSlider.setPaintTicks(true);
        octaveSlider.setPaintLabels(true);
        octaveSlider.setSnapToTicks(true);
        octaveSlider.addChangeListener(e -> {
            osc.octave = octaveSlider.getValue();
            osc.updateFrequency();
        });
        add(octaveSlider);

        // Pitch (Cents)
        add(new JLabel("Pitch (cents):"));
        JSlider pitchSlider = new JSlider(-100, 100, osc.pitchCents);
        JLabel pitchLabel = new JLabel(String.valueOf(osc.pitchCents));

        String paramName = title.equals("OSC 1") ? "osc1.pitch" : "osc2.pitch";
        // Appel à la factory pour gérer le listener complexe
        pitchSlider.addChangeListener(factory.createParamListener(paramName, pitchSlider, pitchLabel, 200, true));

        add(pitchSlider);
        add(pitchLabel);
    }
}