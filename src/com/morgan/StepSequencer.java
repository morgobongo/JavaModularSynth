package com.morgan;

import java.util.Random;

public class StepSequencer implements Runnable {

    private AudioEngine audioEngine;
    private Random random = new Random();

    // --- Paramètres Publics ---
    public volatile double bpm = 120.0;
    public volatile int numSteps = 16;
    public volatile int rootNote = 60;
    public volatile ScaleType currentScale = ScaleType.MAJOR;
    public volatile boolean randomNotes = false;
    public volatile boolean randomRhythm = false;

    private int currentStep = 0;
    private volatile boolean isRunning = false;
    private int lastNotePlayed = -1;

    // --- LES PATTERNS (Rendus publics pour la GUI) ---
    // 'scaleDegrees' stocke le DEGRÉ de la gamme (ex: 0, 1, 2, ... 7, etc.)
    public int[] scaleDegrees = new int[16];
    // 'gates' stocke si la note doit jouer (true) or non (false)
    public boolean[] gates = new boolean[16];

    public StepSequencer(AudioEngine engine) {
        this.audioEngine = engine;
        // Initialiser un pattern par défaut
        initializePatterns();
    }

    // --- Méthodes de contrôle (inchangées) ---
    public void start() { isRunning = true; }
    public void stop() {
        isRunning = false;
        if (lastNotePlayed != -1) {
            audioEngine.noteOff(lastNotePlayed);
            lastNotePlayed = -1;
        }
    }
    public boolean isRunning() { return isRunning; }

    // N'est plus nécessaire, la GUI met à jour les valeurs directement
    // public void updateScale() {}

    @Override
    public void run() {
        while (true) {
            if (isRunning) {
                long sleepTimeMs = (long) (1000.0 / ( (bpm / 60.0) * 4.0 ));

                // Traiter le pas actuel
                processStep(currentStep);

                // Avancer le pas
                currentStep = (currentStep + 1) % numSteps;

                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    // --- LOGIQUE DE LECTURE DU PATTERN (Modifiée) ---
    private void processStep(int step) {
        // 1. Éteindre la note précédente
        if (lastNotePlayed != -1) {
            audioEngine.noteOff(lastNotePlayed);
            lastNotePlayed = -1;
        }

        // 2. Lire le 'Gate' : soit random, soit la valeur du pattern
        boolean gateOn = randomRhythm ? random.nextBoolean() : gates[step];

        if (gateOn) {
            int noteToPlay;

            if (randomNotes) {
                // Le mode Random génère une note MIDI absolue
                noteToPlay = generateRandomNoteInScale();
            } else {
                // --- LIRE LE PATTERN ---
                // 3. Lire le degré de la note (ex: 0, 1, 2...)
                int scaleDegree = scaleDegrees[step];

                // 4. Calculer la note MIDI à partir du degré
                int[] intervals = currentScale.getIntervals();
                int numIntervals = intervals.length;

                // Math.floorMod gère les degrés négatifs (ex: -1 = 7e note, octave -1)
                int intervalIndex = Math.floorMod(scaleDegree, numIntervals);
                int octave = (int) Math.floor((double) scaleDegree / numIntervals);

                noteToPlay = rootNote + intervals[intervalIndex] + (octave * 12);
            }

            // 5. Jouer la note
            audioEngine.noteOn(noteToPlay, 100);
            lastNotePlayed = noteToPlay;
        }
    }

    // Renommée (était generateDefaultPattern)
    private void initializePatterns() {
        int[] intervals = currentScale.getIntervals();
        for (int i = 0; i < 16; i++) {
            // Rythme par défaut : un pas sur deux
            gates[i] = (i % 2 == 0);

            // Note par défaut : monte la gamme (0, 1, 2, 3...)
            scaleDegrees[i] = i % intervals.length;
        }
    }

    // Génère une note aléatoire (inchangé)
    private int generateRandomNoteInScale() {
        int[] intervals = currentScale.getIntervals();
        int intervalIndex = random.nextInt(intervals.length);
        int octave = random.nextInt(3) - 1;
        return rootNote + intervals[intervalIndex] + (octave * 12);
    }
}