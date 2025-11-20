package com.morgan.gui;

import com.morgan.sequenceur.StepSequencer;

import javax.swing.*;
import java.awt.*;

public class StepGridPanel extends JPanel {

    public StepGridPanel(StepSequencer sequencer) {
        super(new GridLayout(2, 16, 2, 2));
        setBorder(BorderFactory.createTitledBorder("Step Grid (Degré / Gate)"));
        setPreferredSize(new Dimension(750, 80));

        for (int i = 0; i < 16; i++) {
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                    sequencer.scaleDegrees[i], -12, 12, 1 );
            JSpinner noteSpinner = new JSpinner(spinnerModel);
            final int stepIndex = i;
            noteSpinner.addChangeListener(e -> sequencer.scaleDegrees[stepIndex] = (int) noteSpinner.getValue());
            noteSpinner.setEditor(new JSpinner.NumberEditor(noteSpinner, "#"));
            add(noteSpinner);
        }

        for (int i = 0; i < 16; i++) {
            JCheckBox gateCheck = new JCheckBox();
            gateCheck.setSelected(sequencer.gates[i]);
            final int stepIndex = i;
            gateCheck.addActionListener(e -> sequencer.gates[stepIndex] = gateCheck.isSelected());
            gateCheck.setHorizontalAlignment(SwingConstants.CENTER);
            add(gateCheck);
        }
    }
}