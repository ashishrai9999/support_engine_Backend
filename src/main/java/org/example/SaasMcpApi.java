package org.example;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface SaasMcpApi {
    
    /**
     * Get available tools from the SaaS MCP server
     * GET https://dice-uat.eka.io/mcp/mcp/tools
     */
    @GET("mcp/mcp/tools")
    Call<Map<String, Object>> getTools();
    
    /**
     * Call a specific tool on the SaaS MCP server
     * POST https://dice-uat.eka.io/mcp/message
     */
    @POST("mcp/message")
    @Headers({
        "Content-Type: application/json"
    })
    Call<Map<String, Object>> callTool(
        @Header("DICE-APP-ID") String diceAppId,
        @Header("X-CLIENT-ID") String clientId,
        @Header("X-CLIENT-SECRET") String clientSecret,
        @Body ToolCallRequest request
    );
} 