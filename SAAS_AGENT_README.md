# SaaS Agent Documentation

## Overview

The SaaS Agent is a new agent that integrates with external SaaS MCP servers to provide dynamic tool calling capabilities. Unlike other agents, it does not store chat sessions and operates in a stateless manner.

## Flow

1. **User Query + Tools Fetch**: The agent receives a user query and fetches available tools from the SaaS MCP server
2. **LLM Tool Decision**: The LLM analyzes the user query and available tools to decide which tool(s) to call
3. **Tool Execution**: The selected tool is called with appropriate arguments
4. **Final Response Generation**: The LLM processes the tool response and generates a user-friendly final response

## API Endpoint

```
POST /stream/saas
```

## Request Format

```json
{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 1234,
    "params": {
        "query": "Get details for transaction ID 5437",
        "diceAppId": "INDIAN",
        "clientId": "03ca45af9",
        "clientSecret": "d99c6259a4964f5ea7e575b6c0adfe4b"
    }
}
```

### Parameters

- `query` (required): The user's question or request
- `diceAppId` (optional): The DICE application ID (default: "INDIAN")
- `clientId` (optional): The client ID for authentication (default: "03ca45af9")
- `clientSecret` (optional): The client secret for authentication (default: "d99c6259a4964f5ea7e575b6c0adfe4b")

## Response Format

The response is streamed as Server-Sent Events (SSE) with the following format:

```
data: {"token": "final response content", "tokenCount": 0}
```

## Example Usage

### Test Script

Run the provided test script:

```bash
./test_saas_agent.sh
```

### Manual Testing

```bash
curl --location 'http://localhost:8080/stream/saas' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 1234,
    "params": {
        "query": "Get details for transaction ID 5437",
        "diceAppId": "INDIAN",
        "clientId": "03ca45af9",
        "clientSecret": "d99c6259a4964f5ea7e575b6c0adfe4b"
    }
}'
```

## Integration with SaaS MCP Server

The agent integrates with the SaaS MCP server at `https://dice-uat.eka.io/mcp`:

- **Tools Endpoint**: `GET /mcp/mcp/tools` - Fetches available tools
- **Message Endpoint**: `POST /mcp/message` - Calls specific tools

### Tool Call Format

When the LLM decides to call a tool, it uses this format:

```json
{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 1232,
    "params": {
        "name": "get_single_transaction",
        "arguments": {
            "transId": 5437
        }
    }
}
```

## Key Features

1. **Stateless Operation**: No chat session storage
2. **Dynamic Tool Discovery**: Fetches available tools at runtime
3. **Intelligent Tool Selection**: LLM decides which tools to call based on user query
4. **Error Handling**: Graceful handling of tool call failures
5. **Streaming Response**: Real-time response streaming via SSE

## Error Handling

- If tool fetching fails, the agent will return an error
- If tool calling fails, the agent will inform the user
- If no suitable tool is found, the agent will provide a direct response
- If LLM processing fails, appropriate error messages are returned

## Configuration

The SaaS MCP server URL and default credentials can be modified in the `SaasMcpServer.java` file:

```java
private static final String BASE_URL = "https://dice-uat.eka.io/mcp";
```

Default credentials are provided as fallbacks but should be overridden in production requests. 