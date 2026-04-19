package com.recovery.recovery_ai;

import org.tensorflow.lite.Interpreter;
public class LogWorkout {

    private final Interpreter tflite;

    private static final float[] SCALER_MEANS = {
            23.97829716193656f,   // age
            0.6283020720809191f,   // gender
            23.136104403925376f,   // bmi
            6.379375682110802f,   // training_intensity
            87.60846469758899f,   // training_duration
            587.3082011800775f,   // training_load
    };

    private static final float[] SCALER_STDS = {
            3.90358368641638f,   // age
            0.48325829356543876f,   // gender
            2.311347121872575f,   // bmi
            1.7345712240179185f,   // training_intensity
            27.572720143614777f,   // training_duration
            298.2444270903771f,   // training_load
    };

    private static final String[] CLASS_LABELS = {"Low", "Medium", "High"};

    public LogWorkout(Interpreter interpreter) {
        this.tflite = interpreter;
    }

    public float calculateTrainingLoad(float intensity, float duration) {
        return intensity * duration;
    }

    public String predictInjuryRisk(float age, float gender, float bmi, float intensity, float duration) {
        float training_load = intensity * duration;
        float[] raw = {age, gender, bmi, intensity, duration, training_load};

        float[] scaled = new float[raw.length];
        for (int i = 0; i < raw.length; i++) {
            scaled[i] = (raw[i] - SCALER_MEANS[i]) / SCALER_STDS[i];
        }

        float[][] input  = new float[1][raw.length];
        float[][] output = new float[1][CLASS_LABELS.length];
        input[0] = scaled;
        tflite.run(input, output);

        float[] probs  = output[0];
        int     maxIdx = 0;
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > probs[maxIdx]) maxIdx = i;
        }
        return CLASS_LABELS[maxIdx];
    }
}