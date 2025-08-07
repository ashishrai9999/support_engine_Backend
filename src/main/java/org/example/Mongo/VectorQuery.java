package org.example.Mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.search.FieldSearchPath;
import io.vertx.core.Vertx;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.TextPreprocessor;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.vectorSearch;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.metaVectorSearchScore;
import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;
import static java.util.Arrays.asList;


public class VectorQuery {


    public static void main(String args[]) {
        System.out.println(getExpenseResolution("Steps to change your password", "64", true));
    }

    public static List<Document> getLoginResolution(String query, String module, Boolean useCohere) {
        Vertx vertx = Vertx.vertx();
        EmbeddingProvider.initialize(vertx, "AIzaSyDXb4Z2YYL5OkhipzCcq6u0idGzMPHsuvA");
        EmbeddingProvider.initialize(vertx, "0P2TxtY7PB22EzYRBv1HR7ooOKPIXgnRRJk3Jkzg", useCohere);

        EmbeddingProvider embeddingProvider = new EmbeddingProvider();
        String preprocessedQuery = null;
        try {
            preprocessedQuery = TextPreprocessor.preprocess(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        BsonArray embeddingBsonArray = embeddingProvider.getEmbedding(preprocessedQuery, false);
        List<Double> embedding = new ArrayList<>();
        for (BsonValue value : embeddingBsonArray.stream().toList()) {
            embedding.add(value.asDouble().getValue());
        }

        String indexName = "vector_index";
        FieldSearchPath fieldSearchPath = fieldPath("embedding");
        int limit = 2;
        if (module == null) {
            module = "Travel";
        }
        Bson criteria = Filters.and(Filters.eq("module", module));
        List<Bson> pipeline = asList(
                vectorSearch(
                        fieldSearchPath,
                        embedding,
                        indexName,
                        limit,
                        exactVectorSearchOptions()
                ),
                project(
                        fields(exclude("_id"), include("answer"), include("query"),
                                metaVectorSearchScore("score"))));
        List<Document> results = MongoClientNew.getLoginCollection().aggregate(pipeline).into(new ArrayList<>());

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            results.forEach(doc -> {
                System.out.println("Text: " + doc.getString("query"));
                System.out.println("Score: " + doc.getDouble("score"));
                System.out.println("answer: " + doc.getString("answer"));

            });
        }
        return results;
    }

    public static List<Document> getExpenseResolution(String query, String module, Boolean useCohere) {
        {
            Vertx vertx = Vertx.vertx();
            EmbeddingProvider.initialize(vertx, "AIzaSyDXb4Z2YYL5OkhipzCcq6u0idGzMPHsuvA");
            EmbeddingProvider.initialize(vertx, "0P2TxtY7PB22EzYRBv1HR7ooOKPIXgnRRJk3Jkzg", useCohere);

            EmbeddingProvider embeddingProvider = new EmbeddingProvider();
            String preprocessedQuery = null;
            try {
                preprocessedQuery = TextPreprocessor.preprocess(query);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            BsonArray embeddingBsonArray = embeddingProvider.getEmbedding(preprocessedQuery, false);
            List<Double> embedding = new ArrayList<>();
            for (BsonValue value : embeddingBsonArray.stream().toList()) {
                embedding.add(value.asDouble().getValue());
            }

            String indexName = "vector_index";
            FieldSearchPath fieldSearchPath = fieldPath("embedding");
            int limit = 2;
            if (module == null) {
                module = "Travel";
            }
            Bson criteria = Filters.and(Filters.eq("module", module));
            List<Bson> pipeline = asList(
                    vectorSearch(
                            fieldSearchPath,
                            embedding,
                            indexName,
                            limit,
                            exactVectorSearchOptions()
                    ),
                    project(
                            fields(exclude("_id"), include("answer"), include("query"),
                                    metaVectorSearchScore("score"))));
            List<Document> results = MongoClientNew.getExpenseCollection().aggregate(pipeline).into(new ArrayList<>());

            if (results.isEmpty()) {
                System.out.println("No results found.");
            } else {
                results.forEach(doc -> {
                    System.out.println("Text: " + doc.getString("query"));
                    System.out.println("Score: " + doc.getDouble("score"));
                    System.out.println("answer: " + doc.getString("answer"));

                });
            }
            return results;
        }
    }

    public static List<Document> getDecisionResolution(String query, String module, Boolean useCohere) {
        {
            Vertx vertx = Vertx.vertx();
            EmbeddingProvider.initialize(vertx, "AIzaSyDXb4Z2YYL5OkhipzCcq6u0idGzMPHsuvA");
            EmbeddingProvider.initialize(vertx, "0P2TxtY7PB22EzYRBv1HR7ooOKPIXgnRRJk3Jkzg", useCohere);

            EmbeddingProvider embeddingProvider = new EmbeddingProvider();
            String preprocessedQuery = null;
            try {
                preprocessedQuery = TextPreprocessor.preprocess(query);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            BsonArray embeddingBsonArray = embeddingProvider.getEmbedding(preprocessedQuery, false);
            List<Double> embedding = new ArrayList<>();
            for (BsonValue value : embeddingBsonArray.stream().toList()) {
                embedding.add(value.asDouble().getValue());
            }

            String indexName = "vector_index";
            FieldSearchPath fieldSearchPath = fieldPath("embedding");
            int limit = 2;
            if (module == null) {
                module = "Travel";
            }
            Bson criteria = Filters.and(Filters.eq("module", module));
            List<Bson> pipeline = asList(
                    vectorSearch(
                            fieldSearchPath,
                            embedding,
                            indexName,
                            limit,
                            exactVectorSearchOptions()
                    ),
                    project(
                            fields(exclude("_id"), include("answer"), include("query"),
                                    metaVectorSearchScore("score"))));
            List<Document> results = MongoClientNew.getExpenseCollection().aggregate(pipeline).into(new ArrayList<>());

            if (results.isEmpty()) {
                System.out.println("No results found.");
            } else {
                results.forEach(doc -> {
                    System.out.println("Text: " + doc.getString("query"));
                    System.out.println("Score: " + doc.getDouble("score"));
                    System.out.println("answer: " + doc.getString("answer"));

                });
            }
            return results;
        }
    }
}

