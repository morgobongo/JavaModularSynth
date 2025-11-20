package com.morgan.audio;

/**
 * L'interface "câble" universelle.
 * Tout module audio (oscillateur, filtre, VCA) l'implémente.
 * La méthode tick() retourne un seul échantillon audio (-1.0 à 1.0).
 */
public interface AudioModule {
    double tick(); // Calcule et retourne le prochain échantillon
}