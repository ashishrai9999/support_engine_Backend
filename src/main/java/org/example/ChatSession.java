package org.example;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class ChatSession {
    private String sessionId;
    private String clientId;
    private String employeeId;
    private Long createdAt;
    private Long updatedAt;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    private String module;
    private List<Message> messages = new ArrayList<>();
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.put("sessionId", sessionId);
        doc.put("clientId", clientId);
        doc.put("employeeId", employeeId);
        doc.put("createdAt", createdAt);
        doc.put("updatedAt", updatedAt);
        doc.put("module", module);

        List<Document> messageDocs = new ArrayList<>();
        for (Message m : messages) {
            Document mDoc = new Document();
            mDoc.put("role", m.getRole());
            mDoc.put("content", m.getContent());
            mDoc.put("timestamp", m.getTimestamp());
            messageDocs.add(mDoc);
        }
        doc.put("messages", messageDocs);
        return doc;
    }

    public static ChatSession fromDocument(Document doc) {
        ChatSession session = new ChatSession();
        session.setSessionId(doc.getString("sessionId"));
        session.setClientId(doc.getString("clientId"));
        session.setEmployeeId(doc.getString("employeeId"));
        session.setCreatedAt(doc.getLong("createdAt"));
        session.setUpdatedAt(doc.getLong("updatedAt"));
        session.setModule(doc.getString("module"));

        List<Message> messages = new ArrayList<>();
        List<Document> messageDocs = (List<Document>) doc.get("messages");
        if (messageDocs != null) {
            for (Document mDoc : messageDocs) {
                Message m = new Message();
                m.setRole(mDoc.getString("role"));
                m.setContent(mDoc.getString("content"));
                m.setTimestamp(mDoc.getLong("timestamp"));
                messages.add(m);
            }
        }
        session.setMessages(messages);
        return session;
    }

    public static class Message {

        private String role;
        private String content;
        private Long timestamp;

        public void setRole(String role) {
            this.role = role;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public String getContent() {
            return content;
        }

        public String getRole() {
            return role;
        }
    }
}
