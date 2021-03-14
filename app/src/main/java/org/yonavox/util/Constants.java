package org.yonavox.util;

public class Constants {
    public static final String RAW_PCM_DATA = "raw.pcm.data";
    public static final String SENSIBO_API_KEY = "sensibo.api.key";
    public static final String SENSIBO_DEVICE_ID = "sensibo.device.id";

    public static final int SAMPLE_RATE = 44100;
    public static final int DOWNSAMPLE_COEFFICIENT = 5;
    public static final int DOWNSAMPLE_RATE = SAMPLE_RATE / DOWNSAMPLE_COEFFICIENT;
    public static final int SPECTROGRAM_TIMESTAMPS = 18664;
    public static final int CUTOFF_FREQUENCY = SAMPLE_RATE / (2 * DOWNSAMPLE_COEFFICIENT);
    public static final int FILTER_ORDER = 5;

    public static final int FRAME_LENGTH = 1024;
    public static final int HOP_LENGTH = 128;
    public static final int MEL_BINS = 80;
    public static final int LOWER_EDGE_HERTZ = 0;
    public static final int UPPER_EDGE_HERTZ = CUTOFF_FREQUENCY;

    //UTTER_WINDOW_SIZE must be smaller than SILENCE_WINDOW_SIZE
    public static final int MIN_UTTERED_SAMPLES = (int)Math.floor(0.25 * SAMPLE_RATE);
    public static final int MAX_SILENT_SAMPLES = (int)Math.floor(0.3 * SAMPLE_RATE);
    public static final int UTTER_WINDOW_SIZE = (int)Math.floor(0.3 * SAMPLE_RATE);
    public static final int SILENCE_WINDOW_SIZE = (int)Math.floor(0.33 * SAMPLE_RATE);
    public static final float SPEECH_AMPLITUDE = 1e-3f;

    public static final float EPSILON = 1e-12f;
}
