package com.morgan.gui;

import com.morgan.audio.EnvelopeGenerator;

import javax.swing.*;
import java.awt.*;

public class EnvelopePanel extends JPanel {

    public EnvelopePanel(EnvelopeGenerator env, MidiUIFactory factory) {
        super(new GridLayout(0, 2, 10, 5));
        setBorder(BorderFactory.createTitledBorder("Envelope (AR)"));

        // Attack
        add(new JLabel("Attack (s):"));
        int attackInitial = (int) (env.attackTime * 50);
        JSlider attackSlider = new JSlider(0, 100, attackInitial);
        JLabel attackLabel = new JLabel(String.format("%.2f", env.attackTime));

        attackSlider.addChangeListener(factory.createTimeListener("envelope.attack", attackSlider, attackLabel));

        JPanel attackPanel = new JPanel(new BorderLayout(5, 0));
        attackPanel.add(attackSlider, BorderLayout.CENTER);
        attackPanel.add(attackLabel, BorderLayout.EAST);
        add(attackPanel);

        // Release
        add(new JLabel("Release (s):"));
        int releaseInitial = (int) (env.releaseTime * 50);
        JSlider releaseSlider = new JSlider(0, 100, releaseInitial);
        JLabel releaseLabel = new JLabel(String.format("%.2f", env.releaseTime));

        releaseSlider.addChangeListener(factory.createTimeListener("envelope.release", releaseSlider, releaseLabel));

        JPanel releasePanel = new JPanel(new BorderLayout(5, 0));
        releasePanel.add(releaseSlider, BorderLayout.CENTER);
        releasePanel.add(releaseLabel, BorderLayout.EAST);
        add(releasePanel);
    }
}