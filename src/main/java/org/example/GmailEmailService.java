package org.example;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GmailEmailService {
    private final GmailAuthService authService;

    public GmailEmailService() throws Exception {
        this.authService = new GmailAuthService();
    }


    public JsonObject getTodaysEmails(String date) {
        try {
            if (!authService.isAuthenticated()) {
                return new JsonObject()
                        .put("error", "Gmail authentication required. Please run the authentication flow first.")
                        .put("authenticated", false);
            }

            Gmail gmailService = authService.getGmailService();
            
            // Get today's date in Gmail query format
            LocalDate today = LocalDate.now();
            String todayQuery = today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            
            // Query for today's emails in inbox
            String query = "in:inbox after:" + date;
            
            ListMessagesResponse response = gmailService.users().messages()
                    .list("me")
                    .setQ(query)
                    .setMaxResults(50L)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                return new JsonObject()
                        .put("emails", new JsonArray())
                        .put("count", 0)
                        .put("message", "No emails found for today")
                        .put("authenticated", true);
            }

            JsonArray emailsArray = new JsonArray();
            for (Message message : messages) {
                Message fullMessage = gmailService.users().messages()
                        .get("me", message.getId())
                        .setFormat("full")
                        .execute();

                JsonObject email = new JsonObject()
                        .put("id", message.getId())
                        .put("threadId", message.getThreadId());

                // Extract headers
                if (fullMessage.getPayload() != null && fullMessage.getPayload().getHeaders() != null) {
                    for (var header : fullMessage.getPayload().getHeaders()) {
                        switch (header.getName()) {
                            case "Subject":
                                email.put("subject", header.getValue());
                                break;
                            case "From":
                                email.put("from", header.getValue());
                                break;
                            case "Date":
                                email.put("date", header.getValue());
                                break;
                        }
                    }
                }

                // Extract plain text body
                String plainText = extractPlainText(fullMessage.getPayload());
                email.put("body", plainText);

                emailsArray.add(email);
            }

            return new JsonObject()
                    .put("emails", emailsArray)
                    .put("count", emailsArray.size())
                    .put("message", "Successfully retrieved today's emails")
                    .put("authenticated", true);

        } catch (IOException e) {
            return new JsonObject()
                    .put("error", "Failed to fetch emails: " + e.getMessage())
                    .put("authenticated", authService.isAuthenticated());
        } catch (Exception e) {
            return new JsonObject()
                    .put("error", "Service initialization failed: " + e.getMessage())
                    .put("authenticated", false);
        }
    }

    public JsonObject authenticate() {
        try {
            // This will trigger the OAuth flow
            authService.getGmailService();
            return new JsonObject()
                    .put("success", true)
                    .put("message", "Gmail authentication successful");
        } catch (Exception e) {
            return new JsonObject()
                    .put("success", false)
                    .put("error", "Authentication failed: " + e.getMessage());
        }
    }

    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    // Helper method to extract plain text from message payload
    private String extractPlainText(com.google.api.services.gmail.model.MessagePart payload) {
        if (payload == null) return "";
        if (payload.getMimeType() != null && payload.getMimeType().equals("text/plain") && payload.getBody() != null && payload.getBody().getData() != null) {
            return decodeBase64Url(payload.getBody().getData());
        }
        if (payload.getParts() != null) {
            for (com.google.api.services.gmail.model.MessagePart part : payload.getParts()) {
                String text = extractPlainText(part);
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }

    private String decodeBase64Url(String data) {
        if (data == null) return "";
        byte[] bodyBytes = java.util.Base64.getUrlDecoder().decode(data);
        return new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
    }
} 