package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.util.Map;

public class SaasMcpServer {
    private static final String BASE_URL = "http://dice-uat.eka.io/mcp";
    private static final String TOOLS_ENDPOINT = "/mcp/tools";
    private static final String MESSAGE_ENDPOINT = "/message";
    
    private final WebClient webClient;
    
    public SaasMcpServer(Vertx vertx) {
        // Configure WebClient with proper options for HTTPS
this .webClient = WebClient.create(vertx);
//            this.webClient = WebClient.create(vertx, new io.vertx.ext.web.client.WebClientOptions()
//                .setFollowRedirects(true)
//                .setMaxRedirects(5)
//                .setKeepAlive(true)
//                .setUserAgent("Vert.x-WebClient/4.0.0")
//                .setTrustAll(true) // For development - remove in production
//                .setVerifyHost(false)); // For development - remove in production
    }
    
    /**
     * Fetch available tools from the SaaS MCP server
     */

    public Future<JsonObject> getTools() {
        Promise<JsonObject> promise = Promise.promise();
        
        String fullUrl = BASE_URL + TOOLS_ENDPOINT;
        System.out.println("Fetching tools from: " + fullUrl);
        System.out.println("BASE_URL: " + BASE_URL);
        System.out.println("TOOLS_ENDPOINT: " + TOOLS_ENDPOINT);
        
        webClient.get(fullUrl)
                .timeout(30000) // 30 second timeout
                .send()
                .onSuccess(response -> {
                    System.out.println("Tools response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject result = response.bodyAsJsonObject();
                            System.out.println("Successfully fetched tools: " + result.size() + " tools found");
                            promise.complete(result);
                        } catch (Exception e) {
                            System.err.println("Failed to parse tools response: " + e.getMessage());
                            promise.fail("Failed to parse tools response: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Failed to fetch tools. Status: " + response.statusCode() + ", Body: " + response.bodyAsString());
                        promise.fail("Failed to fetch tools. Status: " + response.statusCode() + ", Body: " + response.bodyAsString());
                    }
                })
                .onFailure(error -> {
                    System.err.println("Error fetching tools: " + error.getMessage());
                    error.printStackTrace();
                    promise.fail("Error fetching tools: " + error.getMessage());
                });
        
        return promise.future();
    }
    
    /**
     * Call a specific tool on the SaaS MCP server
     */
    public Future<JsonObject> callTool(String toolName, JsonObject arguments, 
                                      String diceAppId, String clientId, String clientSecret) {
        Promise<JsonObject> promise = Promise.promise();
        
        JsonObject requestBody = new JsonObject()
                .put("jsonrpc", "2.0")
                .put("method", "tools/call")
                .put("id", System.currentTimeMillis())
                .put("params", new JsonObject()
                        .put("name", toolName)
                        .put("arguments", arguments));
        
        System.out.println("Calling tool: " + toolName + " at: " + BASE_URL + MESSAGE_ENDPOINT);
        System.out.println("Request body: " + requestBody.encodePrettily());
        
        webClient.post(BASE_URL + MESSAGE_ENDPOINT)
                .timeout(30000) // 30 second timeout
                .putHeader("DICE-APP-ID", diceAppId)
                .putHeader("X-CLIENT-ID", clientId)
                .putHeader("X-CLIENT-SECRET", clientSecret)
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(requestBody)
                .onSuccess(response -> {
                    System.out.println("Tool call response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        try {
                            JsonObject result = response.bodyAsJsonObject();
                            System.out.println("Successfully called tool: " + toolName);
                            promise.complete(result);
                        } catch (Exception e) {
                            System.err.println("Failed to parse tool call response: " + e.getMessage());
                            promise.fail("Failed to parse tool call response: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Failed to call tool. Status: " + response.statusCode() + 
                                   ", Body: " + response.bodyAsString());
                        promise.fail("Failed to call tool. Status: " + response.statusCode() + 
                                   ", Body: " + response.bodyAsString());
                    }
                })
                .onFailure(error -> {
                    System.err.println("Error calling tool " + toolName + ": " + error.getMessage());
                    error.printStackTrace();
                    promise.fail("Error calling tool " + toolName + ": " + error.getMessage());
                });
        
        return promise.future();
    }

    public JsonObject callToolViaRetrofit(String toolName, JsonObject arguments,
                                       String diceAppId, String clientId, String clientSecret) {
        JsonObject jsonObject = new JsonObject();
        
        try {
            // Convert JsonObject arguments to Map
            Map<String, Object> argumentsMap = arguments.getMap();
            
            // Create tool call request
            ToolCallRequest request = new ToolCallRequest(toolName, argumentsMap);
            
            // Get Retrofit client and API
            SaasMcpApi api = UatClient.getInstance().getApi();
            
            // Make the API call
            retrofit2.Response<Map<String, Object>> response = api.callTool(diceAppId, clientId, clientSecret, request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                // Convert Map response back to JsonObject
                jsonObject = new JsonObject(response.body());
                System.out.println("Successfully called tool via Retrofit: " + toolName);
                System.out.println("Response: " + jsonObject.encodePrettily());
            } else {
                System.err.println("Failed to call tool via Retrofit. Status: " + response.code());
                if (response.errorBody() != null) {
                    System.err.println("Error body: " + response.errorBody().string());
                }
                jsonObject.put("error", "Failed to call tool. Status: " + response.code());
            }
            
        } catch (Exception e) {
            System.err.println("Exception calling tool via Retrofit: " + e.getMessage());
            e.printStackTrace();
            jsonObject.put("error", "Exception: " + e.getMessage());
        }
        
        return jsonObject;
    }

    public JsonObject getToolViaRetrofit() {
        JsonObject jsonObject = new JsonObject();
        
        try {
            // Get Retrofit client and API
            SaasMcpApi api = UatClient.getInstance().getApi();
            
            // Make the API call
            retrofit2.Response<Map<String, Object>> response = api.getTools().execute();
            
            if (response.isSuccessful() && response.body() != null) {
                // Convert Map response back to JsonObject
                jsonObject = new JsonObject(response.body());
                System.out.println("Successfully fetched tools via Retrofit");
            } else {
                System.err.println("Failed to fetch tools via Retrofit. Status: " + response.code());
                if (response.errorBody() != null) {
                    System.err.println("Error body: " + response.errorBody().string());
                }
                jsonObject.put("error", "Failed to fetch tools. Status: " + response.code());
            }
            
        } catch (Exception e) {
            System.err.println("Exception fetching tools via Retrofit: " + e.getMessage());
            e.printStackTrace();
            jsonObject.put("error", "Exception: " + e.getMessage());
        }
        
        return jsonObject;
    }
}
