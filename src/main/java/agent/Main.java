package agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Scanner;

public class Main {

    static void main() throws Exception {
        System.out.println("=== ShopEasy Customer Support ===");
        System.out.println("Type your message (or 'quit' to exit)");
        System.out.println("=================================\n");

        // Wire everything together here — one place, easy to see the full picture
        ObjectMapper  mapper   = new ObjectMapper();
        ClaudeClient  client   = new ClaudeClient(System.getenv("ANTHROPIC_API_KEY"), mapper);
        ToolRegistry  registry = new ToolRegistry(mapper);
        ToolExecutor  executor = new ToolExecutor();
        AgenticLoop   loop     = new AgenticLoop(client, registry, executor, mapper);

        ArrayNode conversationHistory = mapper.createArrayNode();
        Scanner   scanner             = new Scanner(System.in);

        while (true) {
            System.out.print("You: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                break;
            }
            if (userInput.isEmpty()) continue;

            // Add user message to history
            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role",    "user");
            userMsg.put("content", userInput);
            conversationHistory.add(userMsg);

            // Run the loop and get a response
            String response = loop.run(conversationHistory);

            // Add Claude's response to history for next turn
            ObjectNode assistantMsg = mapper.createObjectNode();
            assistantMsg.put("role",    "assistant");
            assistantMsg.put("content", response);
            conversationHistory.add(assistantMsg);

            System.out.println("\nAgent: " + response + "\n");
        }
    }

}
