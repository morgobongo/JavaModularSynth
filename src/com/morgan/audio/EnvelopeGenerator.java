package com.morgan.audio;

public class EnvelopeGenerator implements AudioModule {

    // 1. Simplification des états
    private enum State { IDLE, ATTACK, SUSTAIN, RELEASE }
    private State currentState = State.IDLE;

    // 2. Garder seulement Attack et Release
    public double attackTime = 0.01;
    public double releaseTime = 0.3;
    // (Decay et Sustain sont supprimés)

    private final double SAMPLE_RATE;
    private double currentValue = 0.0;

    private double attackIncrement;
    private double releaseIncrement;

    public EnvelopeGenerator(double sampleRate) {
        this.SAMPLE_RATE = sampleRate;
        updateIncrements();
    }

    // 3. Simplification de updateIncrements
    public void updateIncrements() {
        // Monte jusqu'à 1.0
        attackIncrement = (attackTime > 0) ? (1.0 / (attackTime * SAMPLE_RATE)) : 1.0;
        // Descend depuis 1.0 (et non plus depuis sustainLevel)
        releaseIncrement = (releaseTime > 0) ? (1.0 / (releaseTime * SAMPLE_RATE)) : 1.0;
    }

    // --- Triggers (inchangés) ---
    public void noteOn() {
        currentState = State.ATTACK;
    }

    public void noteOff() {
        currentState = State.RELEASE;
    }
    // ---------------------------------

    // 4. Simplification de la logique tick()
    @Override
    public double tick() {
        switch (currentState) {
            case IDLE:
                currentValue = 0.0;
                break;

            case ATTACK:
                currentValue += attackIncrement;
                if (currentValue >= 1.0) {
                    currentValue = 1.0;
                    currentState = State.SUSTAIN; // Passe en mode "Hold"
                }
                break;

            // (Case DECAY supprimée)

            case SUSTAIN:
                currentValue = 1.0; // Reste à 100% tant que la touche est tenue
                break;

            case RELEASE:
                currentValue -= releaseIncrement; // Descend depuis la valeur actuelle
                if (currentValue <= 0.0) {
                    currentValue = 0.0;
                    currentState = State.IDLE;
                }
                break;
        }
        return currentValue;
    }
}