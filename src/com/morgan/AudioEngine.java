package com.morgan;

import javax.sound.sampled.*;

public class AudioEngine implements Runnable {

    public final float SAMPLE_RATE = 44100.0f;
    private SourceDataLine line;

    // --- MODULES PUBLICS (pour GUI et mapping) ---
    public Oscillator osc1;
    public Oscillator osc2;
    public Mixer mixer;
    public Filter filter;
    public EnvelopeGenerator vcaEnv;
    private VCA vca; // VCA reste interne

    private AudioModule masterOutput;
    public volatile double masterVolume = 0.8;

    // --- ÉTAT MONOPHONIQUE : Seul currentNote est nécessaire pour la priorité ---
    private int currentNote = 60; // Note MIDI actuellement jouée


    // ============================================================
    //  CONSTRUCTEUR : INITIALISATION + PATCHING
    // ============================================================
    public AudioEngine() {

        // 1. Initialisation des modules
        osc1   = new Oscillator(SAMPLE_RATE);
        osc2   = new Oscillator(SAMPLE_RATE);
        mixer  = new Mixer();
        filter = new Filter(SAMPLE_RATE);
        vcaEnv = new EnvelopeGenerator(SAMPLE_RATE);
        vca    = new VCA();

        // 2. PATCHING AUDIO (ordre : osc → mixer → filter → VCA → master)
        mixer.setInputA(osc1);
        mixer.setInputB(osc2);

        filter.setInput(mixer);

        vca.setAudioInput(filter);
        vca.setControlInput(vcaEnv);

        masterOutput = vca;

        // 3. Paramètres par défaut
        osc1.setWaveform(Oscillator.Waveform.SAWTOOTH);
        osc2.setWaveform(Oscillator.Waveform.SINE);
        osc2.octave = -1;

        mixer.setBlend(0.5);

        vcaEnv.attackTime = 0.02;
        vcaEnv.releaseTime = 0.2;
    }


    // ============================================================
    //  THREAD AUDIO
    // ============================================================
    @Override
    public void run() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            // Buffer de 1024 bytes (compromis stable)
            line.open(format, 1024);
            line.start();

            byte[] buffer = new byte[256];

            while (true) {
                for (int i = 0; i < buffer.length; i += 2) {

                    // sortie finale
                    double sample = masterOutput.tick() * masterVolume;

                    short pcm = (short) (sample * Short.MAX_VALUE);
                    buffer[i]     = (byte) (pcm & 0xFF);
                    buffer[i + 1] = (byte) ((pcm >> 8) & 0xFF);
                }
                line.write(buffer, 0, buffer.length);
            }

        } catch (LineUnavailableException e) {
            throw new RuntimeException("Erreur ligne audio", e);
        }
    }


    // ============================================================
    //  NOTE ON / NOTE OFF (LOGIQUE MONO CORRIGÉE)
    // ============================================================
    public void noteOn(int note, int velocity) {

        // --- NOUVEAU : Toujours prendre la note la plus récente (Priorité Dernière Note) ---
        currentNote = note;

        double freq = midiToFrequency(currentNote);
        osc1.setBaseFrequency(freq);
        osc2.setBaseFrequency(freq);

        // --- NOUVEAU : Toujours déclencher l'attaque (Permet le re-trigger de la même note) ---
        vcaEnv.noteOn();
    }

    public void noteOff(int note) {

        // Le Release n'est déclenché QUE si la note relâchée est la note actuellement jouée.
        // Si l'utilisateur est en legato, la note relâchée (ex: C4) est différente
        // de la currentNote (ex: D4), et le release est ignoré.
        if (note == currentNote) {
            vcaEnv.noteOff();
        }
    }

    private double midiToFrequency(int note) {
        return 440.0 * Math.pow(2.0, (note - 69.0) / 12.0);
    }


    // ============================================================
    //  HANDLE MAPPED CONTROL (GUI + MIDI)
    // ============================================================
    public void handleMappedControl(String paramName, int value) {

        double normalizedValue = value / 127.0;

        switch (paramName) {

            // --- FILTRE ---
            case "filter.cutoff":
                // Utilisation du setter pour déclencher le recalcul des coefficients
                filter.setCutoff(50.0 + (19950.0 * normalizedValue));
                break;

            case "filter.resonance":
                filter.setResonance(normalizedValue);
                break;

            // --- MASTER ---
            case "masterVolume":
                masterVolume = normalizedValue;
                break;

            // --- MIXER ---
            case "mixer.blend":
                mixer.setBlend(normalizedValue);
                break;

            // --- ENVELOPPE ---
            case "envelope.attack":
                vcaEnv.attackTime = normalizedValue * 2.0;
                vcaEnv.updateIncrements();
                break;

            case "envelope.release":
                vcaEnv.releaseTime = normalizedValue * 2.0;
                vcaEnv.updateIncrements();
                break;

            // --- OSCILLATEURS ---
            case "osc1.pitch":
                osc1.pitchCents = (int)((normalizedValue * 200.0) - 100.0);
                osc1.updateFrequency();
                break;

            case "osc2.pitch":
                osc2.pitchCents = (int)((normalizedValue * 200.0) - 100.0);
                osc2.updateFrequency();
                break;

            default:
                break;
        }
    }
}