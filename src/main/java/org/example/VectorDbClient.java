package org.example;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VectorDbClient {

    private final WebClient client;
    private final String host;
    private final int port;

    public VectorDbClient(Vertx vertx, String host, int port) {
        this.client = WebClient.create(vertx);
        this.host = host;
        this.port = port;
    }

    public void insertVector(String collection, String id, Float[] vector, JsonObject metadata, Handler<AsyncResult<Void>> handler) {
        JsonObject payload = new JsonObject()
                .put("points", new JsonArray()
                        .add(new JsonObject()
                                .put("id", id)
                                .put("vector", new JsonArray(Arrays.stream(vector).collect(Collectors.toList())))
                                .put("payload", metadata)
                        )
                );

        client.put(port, host, "/collections/" + collection + "/points")
                .sendJsonObject(payload, ar -> {
                    if (ar.succeeded()) {
                        handler.handle(Future.succeededFuture());
                    } else {
                        handler.handle(Future.failedFuture(ar.cause()));
                    }
                });
    }

    public void searchVector(String collection, Float[] vector, int topK, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject payload = new JsonObject()
                .put("vector", new JsonArray(Arrays.stream(vector).collect(Collectors.toList())))
                .put("top", topK);

        client.post(port, host, "/collections/" + collection + "/points/search")
                .sendJsonObject(payload, ar -> {
                    if (ar.succeeded()) {
                        handler.handle(Future.succeededFuture(ar.result().bodyAsJsonObject()));
                    } else {
                        handler.handle(Future.failedFuture(ar.cause()));
                    }
                });
    }

    public void updateVector(String collection, String id, Float[] newVector, JsonObject newPayload, Handler<AsyncResult<Void>> handler) {
        insertVector(collection, id, newVector, newPayload, handler); // Overwrite logic
    }
}
