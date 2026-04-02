package com.recovery.recovery_ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecoverySuggestions {

    private static final String TAG = "RecoverySuggestions";
    private static final String URL = "https://api.openai.com/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    public interface SuggestionCallBack {
        void onSuggestionRecieved(String suggestions);
        void onError(String error);
    }



    public static void getRecoveryAdvice(String risk, SuggestionCallBack callback) {
        new Thread(() -> {
            try {
                String prompt = "A user has a " + risk +
                        " injury risk after a workout. " +
                        "Give exactly 3 short recovery suggestions. " +
                        "Return each suggestion on a new line with NO bullet points and NO numbering.";

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);

                JSONArray messages = new JSONArray();
                messages.put(message);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "gpt-4o-mini");
                jsonBody.put("messages", messages);
                jsonBody.put("temperature", 0.7);

                RequestBody requestBody = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(URL)
                        .addHeader("Authorization", "Bearer " + BuildConfig.API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null
                            ? response.body().string()
                            : "No error body";

                    Log.e(TAG, "OPENAI ERROR " + response.code() + ": " + errorBody);

                    postError(callback, "API " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "API Response: " + responseBody);

                JSONObject result = new JSONObject(responseBody);
                JSONArray choices = result.getJSONArray("choices");

                String suggestion = choices
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim();

                postSuccess(callback, suggestion);

            } catch (Exception e) {
                Log.e(TAG, "Suggestion Error", e);
                postError(callback, e.getMessage());
            }
        }).start();
    }

    private static void postSuccess(SuggestionCallBack callback, String text) {
        new Handler(Looper.getMainLooper()).post(() ->
                callback.onSuggestionRecieved(text)
        );
    }

    private static void postError(SuggestionCallBack callback, String error) {
        new Handler(Looper.getMainLooper()).post(() ->
                callback.onError(error)
        );
    }
}
