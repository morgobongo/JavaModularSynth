package com.morgan.audio;

import java.util.Random;

public class Oscillator implements AudioModule {

    public enum Waveform { SINE, SQUARE, SAWTOOTH, TRIANGLE, NOISE }

    // --- FACTEURS DE GAIN POUR ÉGALISATION RMS ---
    // Ces facteurs multiplient l'onde pour lui donner la même puissance effective
    private static final double SINE_GAIN = 1.41421356;   // sqrt(2) = 1 / (1/sqrt(2))
    private static final double SAW_TRI_GAIN = 1.7320508; // sqrt(3) = 1 / (1/sqrt(3))
    // Square et Noise utilisent un gain de 1.0 (non appliqué)

    private final double SAMPLE_RATE;
    public Waveform waveform = Waveform.SINE;
    private double baseFrequency = 440.0;
    public int octave = 0;
    public int pitchCents = 0;

    private double currentFrequency;
    private double phase = 0.0;
    private Random random = new Random();

    public Oscillator(double sampleRate) {
        this.SAMPLE_RATE = sampleRate;
        updateFrequency();
    }

    // --- Méthodes de contrôle (inchangées) ---
    public void setBaseFrequency(double baseFrequency) {
        this.baseFrequency = baseFrequency;
        updateFrequency();
    }

    public void setWaveform(Waveform waveform) {
        this.waveform = waveform;
    }

    public void updateFrequency() {
        this.currentFrequency = baseFrequency *
                Math.pow(2.0, octave) *
                Math.pow(2.0, (double)pitchCents / 1200.0);
    }
    // ----------------------------

    @Override
    public double tick() {
        double sample;

        switch (waveform) {
            case SINE:
                sample = Math.sin(phase) * SINE_GAIN; // Application du gain
                break;
            case SQUARE:
                sample = (phase < Math.PI) ? 1.0 : -1.0;
                break;
            case SAWTOOTH:
                sample = (phase / Math.PI) - 1.0;
                sample *= SAW_TRI_GAIN; // Application du gain
                break;
            case TRIANGLE:
                sample = (2.0 / Math.PI) * Math.asin(Math.sin(phase));
                sample *= SAW_TRI_GAIN; // Application du gain
                break;
            case NOISE:
                sample = random.nextDouble() * 2.0 - 1.0;
                break;
            default:
                sample = 0.0;
        }

        // Avancer la phase (sauf pour le bruit)
        if (waveform != Waveform.NOISE) {
            double phaseIncrement = (2.0 * Math.PI * currentFrequency) / SAMPLE_RATE;
            phase += phaseIncrement;
            phase %= (2.0 * Math.PI);
        }

        // S'assurer que le signal ne dépasse pas +1/-1 après le gain
        return Math.max(-1.0, Math.min(1.0, sample));
    }
}