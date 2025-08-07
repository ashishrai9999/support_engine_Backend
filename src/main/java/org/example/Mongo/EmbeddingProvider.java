package org.example.Mongo;

import io.vertx.core.Vertx;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.example.ExpenseFaqs;
import org.example.LoginFaqs;
import org.example.GeminiEmbeddingClient;
import org.example.cohereEmbedding.CohereEmbeddingClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EmbeddingProvider {

    private static GeminiEmbeddingClient geminiClient;
    private static CohereEmbeddingClient cohereClient;
    private static Vertx vertx;
    private static String apiKey;

    public static void initialize(Vertx vertxInstance, String cohereApiKey, boolean useCohere) {
        vertx = vertxInstance;
        apiKey = cohereApiKey;
        cohereClient = new CohereEmbeddingClient(apiKey);
    }

    public static void initialize(Vertx vertxInstance, String geminiApiKey) {
        vertx = vertxInstance;
        apiKey = geminiApiKey;
        geminiClient = new GeminiEmbeddingClient(vertx, apiKey);
    }

    public List<BsonArray> getEmbeddings(List<ExpenseFaqs> texts , boolean useCohere) {
        List<BsonArray> result = new ArrayList<>();
        for (ExpenseFaqs text : texts) {
            result.add(getEmbedding(text.getProcessedQuery(), useCohere));
            System.out.println("Embedding processing " + text.getQuery());
        }
        return result;
    }

    public BsonArray getEmbedding(String text, boolean useCohere) {
        if (!useCohere) {
            if (geminiClient == null) {
                throw new IllegalStateException("GeminiEmbeddingClient not initialized. Call EmbeddingProvider.initialize() first.");
            }
            final Float[][] vectorHolder = new Float[1][];
            final Throwable[] errorHolder = new Throwable[1];
            CountDownLatch latch = new CountDownLatch(1);
            geminiClient.generateEmbedding(text, ar -> {
                if (ar.succeeded()) {
                    vectorHolder[0] = ar.result();
                } else {
                    errorHolder[0] = ar.cause();
                }
                latch.countDown();
            });
            try {
                if (!latch.await(30, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout waiting for Gemini embedding response");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting for Gemini embedding response", e);
            }
            if (errorHolder[0] != null) {
                throw new RuntimeException("Failed to get embedding from Gemini", errorHolder[0]);
            }
            Float[] vector = vectorHolder[0];
            List<BsonDouble> bsonDoubles = new ArrayList<>();
            for (Float v : vector) {
                bsonDoubles.add(new BsonDouble(v));
            }
            return new BsonArray(bsonDoubles);
        }else {
            if (cohereClient == null) {
                throw new IllegalStateException("CohereEmbeddingClient not initialized. Call EmbeddingProvider.initialize() first.");
            }
            List<Double> embedding;
//            final Float[][] vectorHolder = new Float[1][];
//            final Throwable[] errorHolder = new Throwable[1];
//            CountDownLatch latch = new CountDownLatch(1);
            try {
                List<List<Double>> embeddings = cohereClient.getEmbeddings(List.of(text));
                embedding = embeddings.get(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            try {
//                if (!latch.await(30, TimeUnit.SECONDS)) {
//                    throw new RuntimeException("Timeout waiting for  embedding response");
//                }
//            } catch (InterruptedException e) {
//                throw new RuntimeException("Interrupted while waiting for  embedding response", e);
//            }
//            if (errorHolder[0] != null) {
//                throw new RuntimeException("Failed to get embedding from ", errorHolder[0]);
//            }
            BsonArray bsonArray = new BsonArray();
            for (Double value : embedding) {
                bsonArray.add(new BsonDouble(value));
            }
            return bsonArray;
        }

    }
}
