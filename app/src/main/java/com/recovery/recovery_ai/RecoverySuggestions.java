package com.recovery.recovery_ai;

import org.json.JSONObject;
import org.json.JSONArray;

import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;

public class RecoverySuggestions {
    private static final String URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();

    public interface SuggestionCallBack {
        void onSuggestionRecieved(String suggestions);
        void onError(String error);
    }

    public static void getRecoveryAdvice(String risk, SuggestionCallBack callback) {
        new Thread(() -> {
            try {

                String prompt = "A user has a " + risk +
                        " injury risk after a workout." +
                        "Give exactly 3 short recovery suggestions focused on rest, hydration, and next workout intensity.";

                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", prompt);

                JSONArray messages = new JSONArray();
                messages.put(message);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "gpt-4o-mini");
                jsonBody.put("messages", messages);

                RequestBody requestBody = RequestBody.create(
                        jsonBody.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url(URL)
                        .addHeader("Authorization", "Bearer " + BuildConfig.API_KEY)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JSONObject result = new JSONObject(responseBody);

                String suggestion = result
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                callback.onSuggestionRecieved(suggestion);


            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
