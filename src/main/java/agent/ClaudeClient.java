package agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClaudeClient {

    static final String API_URL = "https://api.anthropic.com/v1/messages";
    static final String MODEL   = "claude-haiku-4-5-20251001";

    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public ClaudeClient(String apiKey, ObjectMapper mapper) {
        this.apiKey  = apiKey;
        this.http    = HttpClient.newHttpClient();
        this.mapper  = mapper;
    }

    // Sends messages to Claude and returns the full response
    public JsonNode sendMessage(String systemPrompt, ArrayNode messages, ArrayNode tools)
            throws Exception {

        ObjectNode body = mapper.createObjectNode();
        body.put("model",      MODEL);
        body.put("max_tokens", 1024);
        body.put("system",     systemPrompt);
        body.set("messages",   messages);
        body.set("tools",      tools);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = http.send(
                request, HttpResponse.BodyHandlers.ofString());

        JsonNode result = mapper.readTree(response.body());

        if (result.has("error")) {
            throw new RuntimeException("API Error: " +
                    result.get("error").get("message").asText());
        }

        return result;
    }

}
