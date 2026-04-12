package agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    static final String API_URL = "https://api.anthropic.com/v1/messages";
    static final String API_KEY = System.getenv("ANTHROPIC_API_KEY");
    static final String MODEL   = "claude-haiku-4-5-20251001";

    static final ObjectMapper mapper = new ObjectMapper();
    static final HttpClient http   = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        System.out.println("Testing API connection...");

        ObjectNode body = mapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", 100);

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode msg = mapper.createObjectNode();
        msg.put("role", "user");
        msg.put("content", "Say exactly: API connection successful!");
        messages.add(msg);
        body.set("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = http.send(
                request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Raw API response: " + response.body());

        JsonNode result = mapper.readTree(response.body());
        System.out.println("Claude says: " +
                result.get("content").get(0).get("text").asText());
    }
}
