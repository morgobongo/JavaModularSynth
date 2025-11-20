package com.morgan;

/**
 * VCA (Voltage-Controlled Amplifier).
 * Multiplie l'entrée audio par l'entrée de contrôle.
 */
public class VCA implements AudioModule {

    private AudioModule audioInput;
    private AudioModule controlInput;

    // --- "Patchs" d'entrée ---
    public void setAudioInput(AudioModule input) {
        this.audioInput = input;
    }

    public void setControlInput(AudioModule input) {
        this.controlInput = input;
    }
    // --------------------------

    @Override
    public double tick() {
        // Si une entrée n'est pas branchée, retourne silence
        if (audioInput == null || controlInput == null) {
            return 0.0;
        }

        double audio = audioInput.tick();    // Signal de l'oscillateur (-1 à 1)
        double control = controlInput.tick(); // Signal de l'enveloppe (0 à 1)

        return audio * control; // Multiplication simple
    }
}