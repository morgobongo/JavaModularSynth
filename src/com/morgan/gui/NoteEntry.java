package com.morgan.gui;

public class NoteEntry {
    public String name;
    public int midiValue;

    public NoteEntry(String name, int midiValue) {
        this.name = name;
        this.midiValue = midiValue;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NoteEntry noteEntry = (NoteEntry) obj;
        return midiValue == noteEntry.midiValue;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(midiValue);
    }
}