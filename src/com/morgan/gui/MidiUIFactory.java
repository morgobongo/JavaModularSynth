package com.morgan.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Interface pour permettre aux panneaux enfants de demander la création
 * de listeners connectés au système de MIDI Learn et à l'AudioEngine.
 */
public interface MidiUIFactory {
    // Pour les potards standards (0-127 ou valeurs brutes)
    ChangeListener createParamListener(String paramName, JSlider slider, JLabel label, int maxRange, boolean isCents);

    // Pour les temps d'enveloppe (conversion spécifique secondes -> midi)
    ChangeListener createTimeListener(String paramName, JSlider slider, JLabel label);
}