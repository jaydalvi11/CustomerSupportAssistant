package agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AgenticLoop {

    private static final int MAX_ITERATIONS = 10;

    private static final String SYSTEM_PROMPT = """
        You are a helpful customer support agent for ShopEasy.
        You help customers with order lookups, refunds, and general questions.
        Always be polite and use the available tools to help customers.
        For refunds, always look up the order first before processing.
        """;

    private final ClaudeClient client;
    private final ToolRegistry registry;
    private final ToolExecutor executor;
    private final ObjectMapper mapper;

    public AgenticLoop(ClaudeClient client, ToolRegistry registry,
                       ToolExecutor executor, ObjectMapper mapper) {
        this.client   = client;
        this.registry = registry;
        this.executor = executor;
        this.mapper   = mapper;
    }

    public String run(ArrayNode history) throws Exception {
        ArrayNode workingHistory = history.deepCopy();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            System.out.println("  [Iteration " + (i + 1) + "] Calling Claude...");

            JsonNode response = client.sendMessage(
                    SYSTEM_PROMPT, workingHistory, registry.getTools());

            String   stopReason = response.get("stop_reason").asText();
            JsonNode content    = response.get("content");

            System.out.println("  [stop_reason = \"" + stopReason + "\"]");

            // Claude is done — return final answer
            if ("end_turn".equals(stopReason)) {
                return extractText(content);
            }

            // Claude wants a tool — execute it and loop again
            if ("tool_use".equals(stopReason)) {
                // Add Claude's response to history
                ObjectNode assistantTurn = mapper.createObjectNode();
                assistantTurn.put("role", "assistant");
                assistantTurn.set("content", content);
                workingHistory.add(assistantTurn);

                // Execute each tool call
                ArrayNode toolResults = mapper.createArrayNode();
                for (JsonNode block : content) {
                    if ("tool_use".equals(block.get("type").asText())) {
                        String   toolName  = block.get("name").asText();
                        String   toolUseId = block.get("id").asText();
                        JsonNode input     = block.get("input");

                        System.out.println("  [Tool: " + toolName + " → " + input + "]");

                        String result = executor.execute(toolName, input);

                        System.out.println("  [Result: " + result.trim() + "]");

                        ObjectNode toolResult = mapper.createObjectNode();
                        toolResult.put("type",        "tool_result");
                        toolResult.put("tool_use_id", toolUseId);
                        toolResult.put("content",     result);
                        toolResults.add(toolResult);
                    }
                }

                // Add tool results and loop again
                ObjectNode toolResultMsg = mapper.createObjectNode();
                toolResultMsg.put("role", "user");
                toolResultMsg.set("content", toolResults);
                workingHistory.add(toolResultMsg);
            }
        }

        return "Sorry, I could not complete that request. Please try again.";
    }

    private String extractText(JsonNode content) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.get("type").asText())) {
                sb.append(block.get("text").asText());
            }
        }
        return sb.toString();
    }

}
