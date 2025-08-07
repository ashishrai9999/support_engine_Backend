package org.example;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.example.Mongo.MongoClientNew;

import java.util.ArrayList;
import java.util.List;

public enum ChatManager {
    Instance;

    public void updateChat(String sessionId) {
        // This method can be used for future updates if needed
    }

    public void saveChat(ChatSession chatSession) throws MongoException {
        MongoCollection<Document> collection = MongoClientNew.getChatSessionCollection();
        collection.insertOne(chatSession.toDocument());
    }

    public void updateChatSession(ChatSession chatSession) throws MongoException {
        MongoCollection<Document> collection = MongoClientNew.getChatSessionCollection();
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        collection.replaceOne(
            new Document("sessionId", chatSession.getSessionId()),
            chatSession.toDocument(),
            options
        );
    }

    public ChatSession getChatBySessionId(String sessionId) throws MongoException {
        MongoCollection<Document> collection = MongoClientNew.getChatSessionCollection();
        Document doc = collection.find(new Document("sessionId", sessionId)).first();
        if (doc != null) {
            return ChatSession.fromDocument(doc);
        }
        return null;
    }

    public List<ChatSession> getAllChats(String clientId, String employeeId) throws MongoException {
        MongoCollection<Document> collection = MongoClientNew.getChatSessionCollection();
        List<ChatSession> chatSessions = new ArrayList<>();
        Document filter = new Document();
        if (clientId != null && !clientId.isEmpty()) {
            filter.append("clientId", clientId);
        }
        if (employeeId != null && !employeeId.isEmpty()) {
            filter.append("employeeId", employeeId);
        }
        for (Document doc : collection.find(filter).sort(new Document("updatedAt", -1)).limit(5)) {
            chatSessions.add(ChatSession.fromDocument(doc));
        }
        return chatSessions;
    }
}
