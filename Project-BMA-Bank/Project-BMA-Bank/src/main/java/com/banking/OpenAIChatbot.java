package com.banking;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
public class OpenAIChatbot {
    private static final String API_KEY = "amgad";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();

    public static String getChatResponse(String userMessage) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", new JSONObject[]{
                new JSONObject()
                        .put("role", "system")
                        .put("content", "You are a helpful assistant for a banking application. Answer general questions about banking services but do not process or store sensitive user data like account numbers or passwords."),
                new JSONObject()
                        .put("role", "user")
                        .put("content", userMessage)
        });
        requestBody.put("temperature", 0.7);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
}