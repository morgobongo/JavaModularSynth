package com.morgan.audio;

import com.morgan.main.SynthMain;

import javax.sound.midi.*;

public class MidiReceiver implements Receiver {

    private AudioEngine engine;

    public MidiReceiver(AudioEngine engine) {
        this.engine = engine;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;

            // --- CORRECTION CRITIQUE : SYNCHRONISER LE BLOC CC POUR ÉVITER LES PLANTAGES ---
            // Le bloc synchronized assure que la logique de mapping n'est pas interrompue par un autre CC
            // (La gestion des notes reste en dehors, car elle ne cause pas de conflit d'état ici)
            synchronized (engine) {
                if (sm.getCommand() >= ShortMessage.CONTROL_CHANGE && sm.getCommand() <= (ShortMessage.CONTROL_CHANGE | 0x0F)) {
                    int ccNumber = sm.getData1();
                    int ccValue = sm.getData2();

                    // 1. Vérification du mode "Mapping Learn"
                    if (SynthMain.MAPPING_STATE.awaitingParameter != null) {

                        String paramToAssign = SynthMain.MAPPING_STATE.awaitingParameter;

                        SynthMain.MAPPING_STATE.assignmentMap.put(ccNumber, paramToAssign);
                        SynthMain.MAPPING_STATE.awaitingParameter = null;

                        System.out.println("MIDI LEARN: CC#" + ccNumber + " assigné à " + paramToAssign);
                        return; // Sortir après l'assignation
                    }

                    // 2. Mode Normal : Appliquer le mapping trouvé
                    String paramName = SynthMain.MAPPING_STATE.assignmentMap.get(ccNumber);
                    if (paramName != null) {
                        engine.handleMappedControl(paramName, ccValue);
                    }
                }
            } // --- FIN DU BLOC SYNCHRONIZED ---

            // --- GESTION DES NOTES (Reste inchangé) ---

            // Note ON (velocity > 0)
            if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                int note = sm.getData1();
                int velocity = sm.getData2();
                engine.noteOn(note, velocity);
            }
            // Note OFF (via status 128 ou Note ON avec velocity 0)
            else if (sm.getCommand() == ShortMessage.NOTE_OFF ||
                    (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                int note = sm.getData1();
                engine.noteOff(note);
            }
        }
    }

    @Override
    public void close() {}
}