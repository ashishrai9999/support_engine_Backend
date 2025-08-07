package org.example.Mongo;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoClientNew {
    private static final String uri = "mongodb+srv://ashishrair500:1lqvaVmK43x6aa4n@cluster0.brmuie0.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private static MongoClient mongoClient;

    static {
        try {
            mongoClient = MongoClients.create(uri);
            // Add a shutdown hook to close the client when the JVM shuts down
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (mongoClient != null) {
                    mongoClient.close();
                }
            }));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MongoDB client", e);
        }
    }

    public static MongoCollection<Document> getLoginCollection() {
        try {
            MongoDatabase mongoDatabase = mongoClient.getDatabase("supportAgent");
            return mongoDatabase.getCollection("LoginFaqs");
        } catch (MongoException e) {
            throw new RuntimeException("Failed to get MongoDB collection", e);
        }
    }

    public static MongoCollection<Document> getExpenseCollection() {
        try {
            MongoDatabase mongoDatabase = mongoClient.getDatabase("supportAgent");
            return mongoDatabase.getCollection("expense");
        } catch (MongoException e) {
            throw new RuntimeException("Failed to get MongoDB collection", e);
        }
    }

    public static MongoCollection<Document> getChatSessionCollection() {
        try {
            MongoDatabase mongoDatabase = mongoClient.getDatabase("supportAgent");
            return mongoDatabase.getCollection("chatSessions");
        } catch (MongoException e) {
            throw new RuntimeException("Failed to get chatSessions collection", e);
        }
    }

    // Add this method to manually close the client when needed
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}