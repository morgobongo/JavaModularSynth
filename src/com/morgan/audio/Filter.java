package com.morgan.audio;

/**
 * Filtre Passe-Bas à deux pôles (Low-Pass Filter) avec résonance.
 */
public class Filter implements AudioModule {

    private AudioModule input;

    // --- Contrôles publics (rendus VOLATILE pour la lecture thread-safe) ---
    public volatile double cutoffFrequency = 10000.0;
    public volatile double resonance = 0.0;

    // NOUVEAU: Coefficients pré-calculés
    private double currentAlpha = 0.0;
    private double currentFeedback = 0.0;
    private volatile boolean coeffsDirty = true;

    // Variables d'état interne
    private double delay1 = 0.0;
    private double delay2 = 0.0;
    private final double SAMPLE_RATE;

    public Filter(double sampleRate) {
        this.SAMPLE_RATE = sampleRate;
        updateCoefficients();
    }

    public void setInput(AudioModule input) { this.input = input; }

    public void setCutoff(double newCutoff) {
        this.cutoffFrequency = newCutoff;
        this.coeffsDirty = true;
    }

    public void setResonance(double newResonance) {
        this.resonance = newResonance;
        this.coeffsDirty = true;
    }

    /** Calcule les coefficients une seule fois lorsque nécessaire. */
    private void updateCoefficients() {
        // Facteur de gain interne (Nous permet d'atteindre l'auto-oscillation)
        final double MAX_Q_BOOST = 4.0; // Augmente l'effet de résonance par 4

        // 1. Calculer g (Normalized frequency)
        double g = cutoffFrequency / SAMPLE_RATE;
        g = Math.min(0.49, g);

        // 2. Clamper la résonance (input 0.0 à 1.0)
        double Q_input = Math.max(0.0, Math.min(1.0, resonance));

        // 3. APPLICATION DU GAIN INTERNE (Le correctif)
        double Q_applied = Q_input * MAX_Q_BOOST;

        // Sauvegarder les valeurs
        this.currentAlpha = g;
        // Calcul du terme de feedback: utilise le gain appliqué Q_applied
        this.currentFeedback = 2.0 * Q_applied * g;

        this.coeffsDirty = false;
    }


    @Override
    public double tick() {
        if (input == null) return 0.0;

        if (coeffsDirty) {
            updateCoefficients();
        }

        // 1. Lire et Clamper l'entrée (Safety Clamp)
        double inputSample = Math.max(-1.0, Math.min(1.0, input.tick()));

        double alpha = currentAlpha;
        double feedback = currentFeedback;

        // --- 2. Application de l'algorithme à 2 pôles ---
        double inputToFilter = inputSample - feedback * delay2;

        // 1er Pôle
        delay1 = delay1 + alpha * (inputToFilter - delay1);

        // 2ème Pôle
        delay2 = delay2 + alpha * (delay1 - delay2);

        // 3. Clamper la sortie finale (Anti-Clipping)
        return Math.max(-1.0, Math.min(1.0, delay2));
    }
}