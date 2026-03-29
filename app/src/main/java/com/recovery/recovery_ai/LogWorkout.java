package com.recovery.recovery_ai;
import org.tensorflow.lite.Interpreter;

public class LogWorkout {
    private Interpreter tflite;
    public LogWorkout(Interpreter interpreter) {
        this.tflite = interpreter;
    }

    // Method to calculate the training load
    public float calculateTrainingLoad(float intensity, float duration) {
        return intensity * duration;
    }

    // Sending the data to the model
    public String predictInjuryRisk(float intensity, float duration, float heartRate) {
        // Calculating the training load
        float load = calculateTrainingLoad(intensity, duration);

        // Preparing inputs for the model
        float[][] input = new float[1][4];
        input[0][0] = intensity;
        input[0][1] = duration;
        input[0][3] = load;

        // Running the model
        float[][] output = new float[1][3];

        // Running TensorFlow model
        tflite.run(input, output);

        // Finding the highest probability
        int maxIndex = 0;
        float maxValue = output[0][0];

        for (int i = 1; i < 3; i++) {
            if (output[0][i] > maxValue) {
                maxValue = output[0][i];
                maxIndex = i;
            }
        }

        if(maxIndex == 0) return "Low";
        if(maxIndex == 1) return "Medium";
        if(maxIndex == 2) return "High";

        return "Unknown";

    }
}
