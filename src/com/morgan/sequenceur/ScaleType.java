package com.morgan.sequenceur;

import java.util.Map;
import java.util.HashMap;

public enum ScaleType {
    MAJOR,
    MINOR_NATURAL,
    DORIAN,
    PENTATONIC_MAJOR,
    PENTATONIC_MINOR,
    CHROMATIC; // Juste pour tester

    // Map pour stocker les intervalles de chaque gamme (en demi-tons)
    private static final Map<ScaleType, int[]> SCALES = new HashMap<>();

    static {
        SCALES.put(MAJOR, new int[] {0, 2, 4, 5, 7, 9, 11});
        SCALES.put(MINOR_NATURAL, new int[] {0, 2, 3, 5, 7, 8, 10});
        SCALES.put(DORIAN, new int[] {0, 2, 3, 5, 7, 9, 10});
        SCALES.put(PENTATONIC_MAJOR, new int[] {0, 2, 4, 7, 9});
        SCALES.put(PENTATONIC_MINOR, new int[] {0, 2, 3, 5, 7}); // Ex: Blues mineur (sans la blue note)
        SCALES.put(CHROMATIC, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
    }

    public int[] getIntervals() {
        return SCALES.get(this);
    }
}