package com.morgan.audio;

/**
 * Un module Mixer qui prend deux entrées AudioModule et les mélange.
 */
public class Mixer implements AudioModule {

    private AudioModule inputA;
    private AudioModule inputB;
    public double blend = 0.5; // 0.0 = 100% A, 1.0 = 100% B

    public void setInputA(AudioModule input) {
        this.inputA = input;
    }

    public void setInputB(AudioModule input) {
        this.inputB = input;
    }

    public void setBlend(double blend) {
        this.blend = Math.max(0.0, Math.min(1.0, blend)); // Clamper la valeur entre 0 et 1
    }

    @Override
    public double tick() {
        double sampleA = (inputA != null) ? inputA.tick() : 0.0;
        double sampleB = (inputB != null) ? inputB.tick() : 0.0;

        // Formule "Equal Power" pour un crossfade plus naturel
        double blendA = Math.cos(blend * 0.5 * Math.PI);
        double blendB = Math.sin(blend * 0.5 * Math.PI);

        return (sampleA * blendA) + (sampleB * blendB);
    }
}