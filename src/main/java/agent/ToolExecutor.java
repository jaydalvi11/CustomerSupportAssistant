package agent;

import com.fasterxml.jackson.databind.JsonNode;

public class ToolExecutor {

    // Dispatcher — routes tool name to the right method
    public String execute(String toolName, JsonNode input) {
        return switch (toolName) {
            case "lookup_order"   -> lookupOrder(input);
            case "process_refund" -> processRefund(input);
            case "search_faq"     -> searchFaq(input);
            default -> "Error: unknown tool '" + toolName + "'";
        };
    }

    private String lookupOrder(JsonNode input) {
        String orderId = input.get("order_id").asText();
        return switch (orderId) {
            case "ORD-001" -> """
                Order ORD-001: DELIVERED
                Items: Blue Sneakers x1 ($120.00)
                Total: $120.00
                Delivered: 3 days ago
                """;
            case "ORD-002" -> """
                Order ORD-002: SHIPPED
                Items: Wireless Headphones x1 ($250.00)
                Total: $250.00
                Expected delivery: tomorrow
                """;
            case "ORD-003" -> """
                Order ORD-003: DELIVERED
                Items: Coffee Maker x1 ($89.99), Filters x2 ($12.00)
                Total: $101.99
                Delivered: 1 week ago
                """;
            default -> "Order " + orderId + " not found in system.";
        };
    }

    private String processRefund(JsonNode input) {
        String orderId = input.get("order_id").asText();
        double amount  = input.get("amount").asDouble();

        // NOTE: HookEngine will intercept HERE in Step 3
        // For now, everything goes through
        return String.format(
                "Refund of $%.2f for order %s processed successfully. " +
                        "Amount will appear in 3-5 business days.", amount, orderId);
    }

    private String searchFaq(JsonNode input) {
        String query = input.get("query").asText().toLowerCase();

        if (query.contains("return") || query.contains("refund")) {
            return "Return Policy: Items can be returned within 30 days. " +
                    "Refunds processed within 3-5 business days.";
        }
        if (query.contains("shipping") || query.contains("delivery")) {
            return "Shipping Policy: Standard 3-5 days. " +
                    "Express (1-2 days) available for $15 extra.";
        }
        if (query.contains("cancel")) {
            return "Cancellation Policy: Cancel within 1 hour of placing. " +
                    "After that, wait for delivery then request a return.";
        }
        return "No FAQ found for that topic. Please contact support.";
    }


}
