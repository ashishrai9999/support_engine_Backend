#!/bin/bash

# Test script for Gmail integration
echo "Testing Gmail Integration for MCP Server"
echo "========================================"

# Check if server is running
echo "1. Checking if server is running..."
if curl -s http://localhost:8080 > /dev/null; then
    echo "   ✓ Server is running on port 8080"
else
    echo "   ✗ Server is not running. Please start the server first:"
    echo "     ./gradlew run"
    exit 1
fi

# Test Gmail authentication endpoint
echo ""
echo "2. Testing Gmail authentication endpoint..."
AUTH_RESPONSE=$(curl -s -X POST http://localhost:8080/mcp/gmail/auth)
echo "   Response: $AUTH_RESPONSE"

# Test get_todays_emails tool
echo ""
echo "3. Testing get_todays_emails tool..."
EMAIL_RESPONSE=$(curl -s -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "method": "get_todays_emails",
    "params": {},
    "id": "test-1"
  }')
echo "   Response: $EMAIL_RESPONSE"

# Test get_tools to verify the new tool is available
echo ""
echo "4. Testing get_tools to verify Gmail tool is available..."
TOOLS_RESPONSE=$(curl -s -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "method": "get_tools",
    "params": {},
    "id": "test-2"
  }')
echo "   Response: $TOOLS_RESPONSE"

echo ""
echo "Test completed!"
echo ""
echo "If you see authentication errors, make sure to:"
echo "1. Place credentials.json in the project root"
echo "2. Run the authentication flow: curl -X POST http://localhost:8080/mcp/gmail/auth"
echo "3. Follow the browser prompts to authorize the application" 