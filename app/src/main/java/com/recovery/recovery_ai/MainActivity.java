package com.recovery.recovery_ai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;
import org.json.JSONArray;

import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import okhttp3.Call;
import okhttp3.Callback;

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

        // TODO Button action - once the inputs have been implemented correctly then we can write the predict logic that sends the data to the model and outputs the risk!!!
    }

    private void getRecoveryAdvice(String risk) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String prompt = "A user has a " + risk + " injury risk. Give 3 short recovery suggestions based on the risk level.";

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);

                JSONArray messages = new JSONArray();
                messages.put(message);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "gpt-4.1-mini");
                jsonBody.put("messages", messages);

                RequestBody requestBody = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer YOUR_API_KEY")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JSONObject result = new JSONObject(responseBody);

                String advice = result
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                runOnUiThread(() -> {
                    tvResult.append("\n\nAdvice:\n" + advice);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    }
