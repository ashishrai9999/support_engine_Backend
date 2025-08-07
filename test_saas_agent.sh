#!/bin/bash

echo "Testing SaaS Agent - Get transaction details"
echo "============================================"

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

echo -e "\n\nTesting SaaS Agent - General question"
echo "========================================="

curl --location 'http://localhost:8080/stream/saas' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": 1235,
    "params": {
        "query": "What is the current system status?",
        "diceAppId": "INDIAN",
        "clientId": "03ca45af9",
        "clientSecret": "d99c6259a4964f5ea7e575b6c0adfe4b"
    }
}' 