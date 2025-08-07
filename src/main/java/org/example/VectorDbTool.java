package org.example;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VectorDbTool{

    private final VectorDbClient dbClient;

    public VectorDbTool(Vertx vertx, GeminiEmbeddingClient embeddingClient) {
        this.dbClient = new VectorDbClient(vertx, "localhost", 6333); // example for Qdrant
        this.embeddingClient = embeddingClient;
    }


    public String name() {
        return "vector-db-tool";
    }


    public String description() {
        return "Tool to insert, search, and update vectors in vector DB";
    }



    public JsonObject schema() {
        return new JsonObject()
                .put("type", "object")
                .put("properties", new JsonObject()
                        .put("operation", new JsonObject().put("type", "string").put("enum", new JsonArray().add("insert").add("search").add("update")))
                        .put("collection", new JsonObject().put("type", "string"))
                        .put("id", new JsonObject().put("type", "string"))
                        .put("text", new JsonObject().put("type", "string")) // ← NEW
                        .put("vector", new JsonObject().put("type", "array").put("items", new JsonObject().put("type", "number")))
                        .put("metadata", new JsonObject().put("type", "object"))
                        .put("topK", new JsonObject().put("type", "integer"))
                )
                .put("required", new JsonArray().add("operation").add("collection"));
    }



    private final GeminiEmbeddingClient embeddingClient;

    public VectorDbTool(Vertx vertx, String geminiApiKey) {
        this.dbClient = new VectorDbClient(vertx, "localhost", 6333); // Qdrant example
        this.embeddingClient = new GeminiEmbeddingClient(vertx, geminiApiKey);
    }


    public void handle(JsonObject input, Handler<AsyncResult<JsonObject>> handler) {
        String operation = input.getString("operation");
        String collection = input.getString("collection");

        Handler<Float[]> vectorHandler = vector -> {
            switch (operation) {
                case "insert":
                    dbClient.insertVector(
                            collection,
                            input.getString("id"),
                            vector,
                            input.getJsonObject("metadata", new JsonObject()),
                            ar -> respond(ar, handler, "inserted")
                    );
                    break;

                case "search":
                    dbClient.searchVector(
                            collection,
                            vector,
                            input.getInteger("topK", 3),
                            handler
                    );
                    break;

                case "update":
                    dbClient.updateVector(
                            collection,
                            input.getString("id"),
                            vector,
                            input.getJsonObject("metadata", new JsonObject()),
                            ar -> respond(ar, handler, "updated")
                    );
                    break;

                default:
                    handler.handle(Future.failedFuture("Unsupported operation"));
            }
        };

        if (input.containsKey("vector")) {
            vectorHandler.handle(toFloatArray(input.getJsonArray("vector")));
        } else if (input.containsKey("text")) {
            embeddingClient.generateEmbedding(input.getString("text"), ar -> {
                if (ar.succeeded()) {
                    vectorHandler.handle(ar.result());
                } else {
                    handler.handle(Future.failedFuture("Embedding failed: " + ar.cause().getMessage()));
                }
            });
        } else {
            handler.handle(Future.failedFuture("Either 'vector' or 'text' is required."));
        }
    }

    private void respond(AsyncResult<Void> ar, Handler<AsyncResult<JsonObject>> handler, String status) {
        if (ar.succeeded()) {
            handler.handle(Future.succeededFuture(new JsonObject().put("status", status)));
        } else {
            handler.handle(Future.failedFuture(ar.cause()));
        }
    }

    private Float[] toFloatArray(JsonArray arr) {
        Float[] result = new Float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            result[i] = arr.getDouble(i).floatValue();
        }
        return result;
    }
}
