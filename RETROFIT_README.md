# SaaS MCP Server - Retrofit Implementation

This document describes the Retrofit implementation for the SaaS MCP server, which provides HTTP client functionality for calling tools and getting tool definitions.

## Overview

The implementation includes:
- `SaasMcpApi` - Retrofit API interface
- `ToolCallRequest` - Data class for tool call requests
- `RetrofitClient` - Singleton client for HTTP operations
- `SaasMcpServer` - Main server class with Retrofit methods

## Dependencies

The following Retrofit dependencies have been added to `build.gradle`:

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
```

## API Endpoints

### 1. Get Tools
- **URL**: `GET https://dice-uat.eka.io/mcp/mcp/tools`
- **Method**: `getToolViaRetrofit()`
- **Description**: Fetches available tools from the SaaS MCP server

### 2. Call Tool
- **URL**: `POST https://dice-uat.eka.io/mcp/message`
- **Method**: `callToolViaRetrofit()`
- **Headers**:
  - `DICE-APP-ID`: Application ID
  - `X-CLIENT-ID`: Client ID
  - `X-CLIENT-SECRET`: Client Secret
  - `Content-Type`: application/json
- **Body**: JSON-RPC 2.0 format with tool name and arguments

## Usage Examples

### Getting Available Tools

```java
SaasMcpServer server = new SaasMcpServer(null);
JsonObject toolsResponse = server.getToolViaRetrofit();
System.out.println("Tools: " + toolsResponse.encodePrettily());
```

### Calling a Specific Tool

```java
// Create arguments for the tool
Map<String, Object> arguments = new HashMap<>();
arguments.put("offset", "1");
arguments.put("limit", "12");
arguments.put("start_date", 1751516877000L);
arguments.put("end_date", 1754022477000L);
arguments.put("status", "settlement_pending");
arguments.put("type", "employee_expense");

JsonObject argumentsJson = new JsonObject(arguments);

// Call the tool
JsonObject response = server.callToolViaRetrofit(
    "get_transactions",
    argumentsJson,
    "INDIAN",
    "03ca45af9",
    "d99c6259a4964f5ea7e575b6c0adfe4b"
);
```

## Request Format

The tool call request follows JSON-RPC 2.0 format:

```json
{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 1232,
    "params": {
        "name": "get_transactions",
        "arguments": {
            "offset": "1",
            "limit": "12",
            "start_date": 1751516877000,
            "end_date": 1754022477000,
            "status": "settlement_pending",
            "type": "employee_expense"
        }
    }
}
```

## Error Handling

Both methods return a `JsonObject` that may contain:
- Success response data
- Error information if the request fails
- Exception details if an error occurs

## Testing

Run the test class to verify the implementation:

```bash
./gradlew run -PmainClass=org.example.SaasMcpRetrofitTest
```

## Configuration

The base URL is configured in `RetrofitClient.java`:
- Base URL: `https://dice-uat.eka.io/`
- Timeout: 30 seconds for connect, read, and write operations

## Notes

- The implementation uses synchronous calls for simplicity
- Error handling includes logging to console
- The client is configured as a singleton for efficiency
- All responses are converted to Vert.x JsonObject format for consistency 