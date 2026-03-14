package com.recovery.recovery_ai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // Our injury model
    Interpreter tflite;

    // Our UI elements
    EditText etDuration;
    EditText etIntensity;
    EditText etHeartRate;
    Button btnPredict;
    TextView tvResult;

    // Workout processor
    LogWorkout logWorkout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO Connecting XML inputs!!!

        // Load our model
        try {
            tflite = new Interpreter(FileUtil.loadMappedFile(this, "risk_model.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initializing our LogWorkout class
        logWorkout = new LogWorkout(tflite);

        // TODO Until we understand what is happening with the inputs this logic will not be in use.
        /*buttonPredict.setOnClickListener(v -> {
            try {
                int duration = Integer.parseInt(etDuration.getText().toString());
                int intensity = Integer.parseInt(etIntensity.getText().toString());
                int heartRate = Integer.parseInt(etHeartRate.getText().toString());

                // Calling the model
                String risk = logWorkout.predictInjuryRisk(intensity, duration, heartRate);
                tvResult.setText("Injury Risk: " + risk);
                if (risk == "Medium" || risk == "High") {
                    RecoverySuggestions.getRecoveryAdvice(
                            risk,
                            new RecoverySuggestions.SuggestionCallBack() {
                                @Override
                                public void onSuggestionRecieved(String suggestions) {
                                    runOnUiThread(() -> {
                                        tvResult.append("\n\nRecovery Suggestions:\n" + suggestions);
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> {
                                        tvResult.append("\n\nError getting suggestions please try again");
                                    });
                                }
                            }
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }

}
