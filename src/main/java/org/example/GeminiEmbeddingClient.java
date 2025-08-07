package org.example;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.codec.BodyCodec;

public class GeminiEmbeddingClient {

    private final WebClient client;
    private final String apiKey;

    public GeminiEmbeddingClient(Vertx vertx, String apiKey) {
        this.client = WebClient.create(vertx);
        this.apiKey = apiKey;
    }

    public void generateEmbedding(String text, Handler<Future<Float[]>> handler) {
        JsonObject request = new JsonObject()
                .put("model", "models/embedding-001")
                .put("content", new JsonObject().put("parts", new JsonArray().add(new JsonObject().put("text", text))));

        client.postAbs("https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=" + apiKey)
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(request, ar -> {
                    if (ar.succeeded()) {
                        JsonObject body = ar.result().body();
                        JsonArray values = body.getJsonObject("embedding").getJsonArray("values");
                        Float[] vector = new Float[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            vector[i] = values.getDouble(i).floatValue();
                        }
                        handler.handle(Future.succeededFuture(vector));
                    } else {
                        handler.handle(Future.failedFuture(ar.cause()));
                    }
                });
    }
}
