package com.arn.goodmod.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroqClient {
    private static final Logger LOGGER = LogManager.getLogger("Goodcraft-AI");
    private static final String GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public record ChatMessage(String role, String content) {}

    public static final String DEFAULT_FALLBACK_MODEL = "openai/gpt-oss-20b";

    public static CompletableFuture<String> askGroq(
            String apiKey,
            String model,
            String systemPrompt,
            List<ChatMessage> history,
            String userMessage
    ) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        String chosenModel = (model != null && !model.trim().isEmpty()) ? model.trim() : DEFAULT_FALLBACK_MODEL;

        return CompletableFuture.supplyAsync(() -> executeGroqRequest(apiKey.trim(), chosenModel, systemPrompt, history, userMessage, true));
    }

    private static String executeGroqRequest(
            String apiKey,
            String model,
            String systemPrompt,
            List<ChatMessage> history,
            String userMessage,
            boolean allowFallback
    ) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("model", model);
            root.addProperty("max_tokens", 150);
            root.addProperty("temperature", 0.8);

            JsonArray messages = new JsonArray();

            // System instructions
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                JsonObject sysMsg = new JsonObject();
                sysMsg.addProperty("role", "system");
                sysMsg.addProperty("content", systemPrompt.trim());
                messages.add(sysMsg);
            }

            // Previous conversation turns
            if (history != null) {
                for (ChatMessage msg : history) {
                    JsonObject m = new JsonObject();
                    m.addProperty("role", msg.role());
                    m.addProperty("content", msg.content());
                    messages.add(m);
                }
            }

            // Current user message
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            messages.add(userMsg);

            root.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            int statusCode = response.statusCode();
            String body = response.body();

            if (statusCode == 200) {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray choices = json.getAsJsonArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JsonObject first = choices.get(0).getAsJsonObject();
                    JsonObject message = first.getAsJsonObject("message");
                    if (message != null && message.has("content")) {
                        String reply = message.get("content").getAsString();
                        return sanitizeReply(reply);
                    }
                }
                return "§c[Groq returned empty response]";
            } else if (statusCode == 404 && allowFallback && !DEFAULT_FALLBACK_MODEL.equalsIgnoreCase(model)) {
                LOGGER.warn("Groq model '{}' not found (404). Retrying with fallback model '{}'...", model, DEFAULT_FALLBACK_MODEL);
                return executeGroqRequest(apiKey, DEFAULT_FALLBACK_MODEL, systemPrompt, history, userMessage, false);
            } else if (statusCode == 401) {
                LOGGER.warn("Groq API returned 401 Unauthorized. Check your API key.");
                return "§c[Groq API Key Invalid or Expired - check Mods -> Goodcraft -> Config]";
            } else if (statusCode == 429) {
                LOGGER.warn("Groq API rate limit reached.");
                return "§c[Groq Rate Limit Exceeded - please wait a moment]";
            } else {
                String errMsg = "HTTP " + statusCode;
                try {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (json.has("error") && json.getAsJsonObject("error").has("message")) {
                        errMsg = json.getAsJsonObject("error").get("message").getAsString();
                    }
                } catch (Exception ignored) {}
                LOGGER.warn("Groq API error {}: {}", statusCode, body);
                return "§c[Groq Error: " + errMsg + "]";
            }
        } catch (Exception e) {
            LOGGER.error("Error querying Groq API: {}", e.getMessage());
            return "§c[Groq Connection Error: " + e.getMessage() + "]";
        }
    }

    private static String sanitizeReply(String raw) {
        if (raw == null) return null;
        String clean = raw.trim();
        // Remove surrounding quotation marks if the LLM wrapped the whole response
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() > 1) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }
        // Normalize multiple spaces or internal newlines for Minecraft chat
        clean = clean.replace("\r\n", " ").replace("\n", " ");
        while (clean.contains("  ")) {
            clean = clean.replace("  ", " ");
        }
        // Truncate to reasonable chat length if unusually long (max 280 chars)
        if (clean.length() > 280) {
            int lastPunct = Math.max(clean.lastIndexOf('.', 280), Math.max(clean.lastIndexOf('!', 280), clean.lastIndexOf('?', 280)));
            if (lastPunct > 180) {
                clean = clean.substring(0, lastPunct + 1);
            } else {
                clean = clean.substring(0, 277) + "...";
            }
        }
        return clean;
    }
}
