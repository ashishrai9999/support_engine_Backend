package org.example;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class SaasMcpRetrofitTest {
    
    public static void main(String[] args) {
        SaasMcpServer server = new SaasMcpServer(null); // Vertx not needed for Retrofit
        
        // Test 1: Get available tools
        System.out.println("=== Testing getToolViaRetrofit ===");
        JsonObject toolsResponse = server.getToolViaRetrofit();
        System.out.println("Tools Response: " + toolsResponse.encodePrettily());
        
        // Test 2: Call a specific tool (get_transactions)
        System.out.println("\n=== Testing callToolViaRetrofit ===");
        
        // Create arguments for get_transactions tool
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("offset", "1");
        arguments.put("limit", "12");
        arguments.put("start_date", 1751516877000L);
        arguments.put("end_date", 1754022477000L);
        arguments.put("status", "settlement_pending");
        arguments.put("type", "employee_expense");
        
        JsonObject argumentsJson = new JsonObject(arguments);
        
        // Call the tool
        JsonObject toolResponse = server.callToolViaRetrofit(
            "get_transactions",
            argumentsJson,
            "INDIAN",
            "03ca45af9",
            "d99c6259a4964f5ea7e575b6c0adfe4b"
        );
        
        System.out.println("Tool Call Response: " + toolResponse.encodePrettily());
    }
} 