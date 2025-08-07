package org.example.Mongo;

import com.mongodb.MongoException;
import com.mongodb.client.result.InsertManyResult;
import io.vertx.core.Vertx;
import org.bson.BsonArray;
import org.bson.Document;
import org.example.ExpenseFaqs;
import org.example.LoginFaqs;
import org.example.SaasIssues;
import org.example.TextPreprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CreateEmbeddings {

    //static List<String> data2 = SaasIssues.data;
    static List<ExpenseFaqs> data = new ArrayList<>();

    public static void main(String args[]) {
        createEmbedding(data);
    }

    public static String createEmbedding(List<ExpenseFaqs> faqs) {
        Vertx vertx = Vertx.vertx();
        List<ExpenseFaqs> data = SaasIssues.getData();
        data.addAll(faqs);
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        EmbeddingProvider.initialize(vertx, "AIzaSyDXb4Z2YYL5OkhipzCcq6u0idGzMPHsuvA");
        EmbeddingProvider.initialize(vertx, "0P2TxtY7PB22EzYRBv1HR7ooOKPIXgnRRJk3Jkzg", true);


        System.out.println("Creating embeddings for " + data.size() + " documents");

        EmbeddingProvider embeddingProvider = new EmbeddingProvider();

        int batchSize = 10;
        List<String> insertedIds = new ArrayList<>();

        for (int start = 0; start < data.size(); start += batchSize) {
            int end = Math.min(start + batchSize, data.size());
            List<ExpenseFaqs> batch = data.subList(start, end);

            // Generate embeddings for this batch
            List<ExpenseFaqs> preprocessedBatch = new ArrayList<>();
            for (ExpenseFaqs doc : batch) {
                try {
                    doc.setProcessedQuery(TextPreprocessor.preprocess(doc.getQuery()));
                    preprocessedBatch.add(doc);
                } catch (Exception e) {
                    throw new RuntimeException("Preprocessing failed for: " + doc, e);
                }
            }
            List<BsonArray> embeddings = embeddingProvider.getEmbeddings(preprocessedBatch, false);

            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < batch.size(); i++) {
               // Document doc = toLoginDocument(i, batch, embeddings);
                Document doc1 = toExpenseDocument(i, batch, embeddings);
                documents.add(doc1);
            }
            try {
                InsertManyResult result = MongoClientNew.getExpenseCollection().insertMany(documents);
                result.getInsertedIds().values()
                        .forEach(doc -> insertedIds.add(doc.toString()));

            } catch (MongoException me) {
                throw new RuntimeException("Failed to insert documents in batch starting at index " + start, me);
            } catch (Exception e) {
                throw new RuntimeException("Operation failed for batch starting at index " + start, e);
            }
            System.out.println("Inserted batch: " + (start / batchSize + 1) + ", Total inserted so far: " + insertedIds.size());
        }
        System.out.println("Completed processing. Total documents inserted: " + insertedIds.size());
        return "query inserted successfully";
    }

    private static Document toLoginDocument(int i, List<LoginFaqs> batch , List<BsonArray> embeddings) {
        Document doc = batch.get(i).toLoginDocument()
                .append("embedding", embeddings.get(i))
                .append("faqId", "FID" + System.currentTimeMillis());
        return doc;
    }
    private static Document toExpenseDocument(int i, List<ExpenseFaqs> batch , List<BsonArray> embeddings) {
        Document doc = batch.get(i).toExpenseDocument()
                .append("embedding", embeddings.get(i))
                .append("faqId", UUID.randomUUID().toString());
        return doc;
    }
}