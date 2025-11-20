package com.morgan.main;

import com.morgan.audio.AudioEngine;
import com.morgan.audio.MidiReceiver;
import com.morgan.sequenceur.StepSequencer;
import com.morgan.gui.SynthGui;

import javax.sound.midi.*;
import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;

public class SynthMain {

    // --- CLASSE POUR L'ÉTAT DU MAPPING (inchangée) ---
    public static class MappingState {
        public volatile String awaitingParameter = null;
        public final Map<Integer, String> assignmentMap = new HashMap<>();

        public MappingState() {
            assignmentMap.put(74, "filter.cutoff");
            assignmentMap.put(71, "filter.resonance");
            assignmentMap.put(7, "masterVolume");
            assignmentMap.put(1, "mixer.blend");
        }
    }

    public static final MappingState MAPPING_STATE = new MappingState();

    public static void main(String[] args) throws Exception {
        // 1. Démarrage du moteur et du séquenceur (inchangé)
        AudioEngine audioEngine = new AudioEngine();
        Thread audioThread = new Thread(audioEngine);
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();

        StepSequencer sequencer = new StepSequencer(audioEngine);
        Thread sequencerThread = new Thread(sequencer);
        sequencerThread.start();

        SwingUtilities.invokeLater(() -> {
            new SynthGui(audioEngine, sequencer);
        });

        // 2. Détection et connexion du MIDI (LOGIQUE CORRIGÉE)
        MidiDevice genericInputDevice = null; // Utilisé comme fallback
        MidiDevice preferredInputDevice = null; // L'appareil que nous voulons (MPK Mini)

        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        System.out.println("\n--- Périphériques MIDI détectés par Java ---");
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);

                // Affichage du diagnostic
                String type = (device.getMaxTransmitters() != 0) ? "INPUT" : "OUTPUT";
                System.out.println("Nom: " + info.getName() + " | Type: " + type + " | MaxTransmitters: " + device.getMaxTransmitters());

                // --- LOGIQUE DE SÉLECTION CORRIGÉE ---
                if (device.getMaxTransmitters() != 0) {
                    String name = info.getName();

                    // Priorité 1: Sélectionner spécifiquement le MPK Mini
                    if (name.contains("MPK Mini") || name.contains("akai")) {
                        preferredInputDevice = device;
                    }

                    // Priorité 2: Garder le premier appareil d'entrée trouvé comme option générique
                    if (genericInputDevice == null) {
                        genericInputDevice = device;
                    }
                }

            } catch (MidiUnavailableException e) {
                // Ignore les périphériques non disponibles
            }
        }
        System.out.println("------------------------------------------");

        // 3. Connexion au périphérique final (Priorité au MPK Mini)
        MidiDevice finalDevice = (preferredInputDevice != null) ? preferredInputDevice : genericInputDevice;

        if (finalDevice != null) {
            try {
                finalDevice.open();
                Transmitter transmitter = finalDevice.getTransmitter();
                transmitter.setReceiver(new MidiReceiver(audioEngine));
                System.out.println("✅ Contrôle MIDI connecté à: " + finalDevice.getDeviceInfo().getName());
            } catch (MidiUnavailableException e) {
                System.out.println("ERREUR: Impossible d'ouvrir le périphérique MIDI: " + finalDevice.getDeviceInfo().getName() + " (Il est peut-être déjà utilisé).");
            }
        } else {
            System.out.println("❌ Aucun périphérique MIDI d'entrée n'a pu être sélectionné. Vérifiez la connexion du clavier.");
        }
    }
}