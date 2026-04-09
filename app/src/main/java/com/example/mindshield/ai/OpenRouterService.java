package com.example.mindshield.ai;

import android.util.Log;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class OpenRouterService {

    private static final String API_KEY = "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";
    private static final String URL = "https://openrouter.ai/api/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();

    public interface AIResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public void getChatResponse(String userMessage, String context, AIResponseCallback callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("model", "openrouter/auto");

            JSONArray messages = new JSONArray();

            // SYSTEM PROMPT (this is where intelligence comes from)
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content",
                    "You are a mental health and wellness assistant. " +
                            "Use the following user data to give personalized advice:\n" + context);
            messages.put(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", userMessage);
            messages.put(user);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String res = response.body().string();
                        Log.d("AI_RESPONSE", res);

                        JSONObject obj = new JSONObject(res);

                        if (!obj.has("choices")) {
                            callback.onError("Invalid API response");
                            return;
                        }

                        JSONArray choices = obj.getJSONArray("choices");

                        if (choices.length() == 0) {
                            callback.onError("Empty response");
                            return;
                        }

                        JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");

                        String reply = messageObj.optString("content", "No response");

                        callback.onResponse(reply);

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}