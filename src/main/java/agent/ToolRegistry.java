package agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ToolRegistry {

    private final ObjectMapper mapper;

    public ToolRegistry(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // Returns all tool definitions Claude is allowed to use
    public ArrayNode getTools() {
        ArrayNode tools = mapper.createArrayNode();
        tools.add(buildLookupOrderTool());
        tools.add(buildProcessRefundTool());
        tools.add(buildSearchFaqTool());
        return tools;
    }

    private ObjectNode buildLookupOrderTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "lookup_order");
        tool.put("description",
                "Look up an order by its ID. Returns order status, items, and total amount.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();
        ObjectNode orderIdProp = mapper.createObjectNode();
        orderIdProp.put("type", "string");
        orderIdProp.put("description", "The order ID to look up, e.g. ORD-001");
        props.set("order_id", orderIdProp);

        schema.set("properties", props);
        schema.set("required", mapper.createArrayNode().add("order_id"));
        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode buildProcessRefundTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "process_refund");
        tool.put("description",
                "Process a refund for an order. Requires order ID and refund amount.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode orderIdProp = mapper.createObjectNode();
        orderIdProp.put("type", "string");
        orderIdProp.put("description", "The order ID to refund");

        ObjectNode amountProp = mapper.createObjectNode();
        amountProp.put("type", "number");
        amountProp.put("description", "The amount to refund in USD");

        props.set("order_id", orderIdProp);
        props.set("amount",   amountProp);

        schema.set("properties", props);
        schema.set("required",
                mapper.createArrayNode().add("order_id").add("amount"));
        tool.set("input_schema", schema);
        return tool;
    }

    private ObjectNode buildSearchFaqTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("name", "search_faq");
        tool.put("description",
                "Search the FAQ database for answers to common questions.");

        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode props = mapper.createObjectNode();
        ObjectNode queryProp = mapper.createObjectNode();
        queryProp.put("type", "string");
        queryProp.put("description", "The question or topic to search for");
        props.set("query", queryProp);

        schema.set("properties", props);
        schema.set("required", mapper.createArrayNode().add("query"));
        tool.set("input_schema", schema);
        return tool;
    }

}
