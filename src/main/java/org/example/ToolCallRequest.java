package org.example;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ToolCallRequest {
    @SerializedName("jsonrpc")
    private String jsonrpc;
    
    @SerializedName("method")
    private String method;
    
    @SerializedName("id")
    private long id;
    
    @SerializedName("params")
    private ToolCallParams params;
    
    public ToolCallRequest(String toolName, Map<String, Object> arguments) {
        this.jsonrpc = "2.0";
        this.method = "tools/call";
        this.id = System.currentTimeMillis();
        this.params = new ToolCallParams(toolName, arguments);
    }
    
    // Getters
    public String getJsonrpc() { return jsonrpc; }
    public String getMethod() { return method; }
    public long getId() { return id; }
    public ToolCallParams getParams() { return params; }
    
    // Inner class for params
    public static class ToolCallParams {
        @SerializedName("name")
        private String name;
        
        @SerializedName("arguments")
        private Map<String, Object> arguments;
        
        public ToolCallParams(String name, Map<String, Object> arguments) {
            this.name = name;
            this.arguments = arguments;
        }
        
        // Getters
        public String getName() { return name; }
        public Map<String, Object> getArguments() { return arguments; }
    }
} 