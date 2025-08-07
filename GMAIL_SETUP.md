# Gmail Integration Setup Guide

This guide will help you set up Gmail authentication and integrate the email fetching functionality into your MCP server.

## Prerequisites

1. A Google account
2. Access to Google Cloud Console
3. Java 17 or later

## Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Gmail API:
   - Go to "APIs & Services" > "Library"
   - Search for "Gmail API"
   - Click on it and press "Enable"

## Step 2: Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth 2.0 Client IDs"
3. Choose "Desktop application" as the application type
4. Give it a name (e.g., "MCP Gmail Integration")
5. Click "Create"
6. Download the JSON file

## Step 3: Configure Credentials

1. Rename the downloaded JSON file to `credentials.json`
2. Place it in the root directory of your project (same level as `build.gradle`)
3. The file structure should look like:
   ```
   VertxMcpSseServer/
   ├── build.gradle
   ├── credentials.json  ← Place here
   ├── src/
   └── ...
   ```

## Step 4: Build and Run

1. Refresh your Gradle dependencies:
   ```bash
   ./gradlew build
   ```

2. Run the server:
   ```bash
   ./gradlew run
   ```

## Step 5: Authenticate with Gmail

1. The first time you use the Gmail functionality, you'll need to authenticate
2. Send a POST request to authenticate:
   ```bash
   curl -X POST http://localhost:8080/mcp/gmail/auth
   ```

3. This will open a browser window for OAuth authentication
4. Follow the prompts to authorize the application
5. The tokens will be stored locally in a `tokens/` directory

## Step 6: Use the Gmail Tool

Once authenticated, you can use the `get_todays_emails` tool:

```bash
curl -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "method": "get_todays_emails",
    "params": {},
    "id": "1"
  }'
```

## API Endpoints

### Gmail Authentication
- **POST** `/mcp/gmail/auth` - Authenticate with Gmail (triggers OAuth flow)

### MCP Message Endpoint
- **POST** `/mcp/message` - Send JSON-RPC requests including `get_todays_emails`

## Tool Description

The `get_todays_emails` tool:
- Fetches emails from today in your Gmail inbox
- Returns email metadata (subject, from, date, id, threadId)
- Maximum 50 emails per request
- Requires Gmail authentication

## Response Format

```json
{
  "emails": [
    {
      "id": "email_id",
      "threadId": "thread_id",
      "subject": "Email Subject",
      "from": "sender@example.com",
      "date": "Wed, 15 Nov 2023 10:30:00 +0000"
    }
  ],
  "count": 5,
  "message": "Successfully retrieved today's emails",
  "authenticated": true
}
```

## Troubleshooting

### Common Issues

1. **"Gmail service not initialized"**
   - Ensure `credentials.json` is in the project root
   - Check that the file has the correct format

2. **"Authentication failed"**
   - Make sure you've enabled the Gmail API in Google Cloud Console
   - Verify your OAuth credentials are correct
   - Check that the redirect URI matches (http://localhost:8888/Callback)

3. **"Resource not found: credentials.json"**
   - Ensure the file is named exactly `credentials.json`
   - Place it in the project root directory

4. **Port conflicts**
   - The OAuth flow uses port 8888
   - Ensure this port is available

### Security Notes

- Keep your `credentials.json` file secure and don't commit it to version control
- The `tokens/` directory contains sensitive authentication tokens
- Add both to your `.gitignore` file

## File Structure After Setup

```
VertxMcpSseServer/
├── build.gradle
├── credentials.json          ← Your OAuth credentials
├── tokens/                   ← Generated after first auth
│   └── StoredCredential
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── example/
│                   ├── VertxMcpSseServer.java
│                   ├── GmailAuthService.java
│                   └── GmailEmailService.java
└── GMAIL_SETUP.md
``` 