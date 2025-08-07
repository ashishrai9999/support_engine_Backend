package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.core.http.HttpClientOptions;

public class GeminiService {

    private Vertx vertx;
    private final String GEMINI_API_KEY = "AIzaSyAhHEajvahyJVELOZuGNM5YmqHn-KFnEYk";
    private final String HOST = "generativelanguage.googleapis.com";
    private final int PORT = 443;
    private WebClient webClient;
    private HttpClient httpClient;

    public GeminiService(Vertx vertx) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx); // keep for other usages if needed
        HttpClientOptions options = new HttpClientOptions().setSsl(true);
        this.httpClient = vertx.createHttpClient(options);
    }

    public void streamChatResponse(String prompt, Handler<String> onData, Handler<Throwable> onError, Handler<Void> onEnd) {
        JsonObject requestBody = new JsonObject()
                .put("contents", new JsonArray().add(
                        new JsonObject().put("parts", new JsonArray().add(
                                new JsonObject().put("text", prompt)
                        ))
                ));
        String path = "/v1beta/models/gemini-2.5-flash:streamGenerateContent?key=" + GEMINI_API_KEY;

        httpClient.request(HttpMethod.POST, PORT, HOST, path, reqAr -> {
            if (reqAr.succeeded()) {
                HttpClientRequest req = reqAr.result();
                req.putHeader("Content-Type", "application/json");
                req.putHeader("Accept", "text/event-stream");
                req.setChunked(true);
                req.exceptionHandler(onError);
                req.send(Buffer.buffer(requestBody.encode()), resAr -> {
                    if (resAr.succeeded()) {
                        handleStreamingResponse(resAr.result(), onData, onError, onEnd);
                    } else {
                        onError.handle(resAr.cause());
                    }
                });
            } else {
                onError.handle(reqAr.cause());
            }
        });
    }

    private void handleStreamingResponse(HttpClientResponse response, Handler<String> onData,
                                         Handler<Throwable> onError, Handler<Void> onEnd) {
        response.handler(buffer -> {
            try {
                String jsonStr = buffer.toString().trim();
                if (jsonStr.startsWith("[")) {
                    jsonStr = jsonStr.substring(1, jsonStr.length());
                }else{
                    jsonStr = jsonStr.substring(1);
                }
                if (jsonStr.equals("]")) {
                    onEnd.handle(null);
                    return;
                }
                if (jsonStr.isEmpty()) return;
                JsonObject chunk = new JsonObject(jsonStr);
                if (chunk.containsKey("candidates")) {
                    JsonArray candidates = chunk.getJsonArray("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        JsonObject content = candidates.getJsonObject(0).getJsonObject("content");
                        JsonArray parts = content.getJsonArray("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String text = parts.getJsonObject(0).getString("text");
                            onData.handle(text);
                        }
                    }
                }
            } catch (Exception e) {
                onError.handle(e);
            }
        });
        response.exceptionHandler(onError);
        response.endHandler(onEnd);
    }
}
