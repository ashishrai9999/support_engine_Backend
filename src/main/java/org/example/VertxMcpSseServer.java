package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.core.json.JsonObject;
import org.example.Mongo.CreateEmbeddings;
import org.example.Mongo.VectorQuery;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VertxMcpSseServer extends AbstractVerticle {

    private final Map<String, HttpServerResponse> clients = new ConcurrentHashMap<>();
    private final Map<String, Promise<String>> jobs = new ConcurrentHashMap<>();
    private final Map<String, Long> jobTimers = new ConcurrentHashMap<>();
    private GmailEmailService gmailService;
    private KnowledgeBasePdfTool knowledgeBasePdfTool;
    private GeminiService geminiService;


    @Override
    public void start() {
        try {
            geminiService = new GeminiService(vertx);
            System.out.println("Gemini service initialized successfully");
        } catch (Exception e) {
            System.out.println("Warning: Gemini service initialization failed: " + e.getMessage());
            System.out.println("Gemini functionality will not be available until credentials.json is configured");
        }
        // Initialize Gmail service
        try {
            gmailService = new GmailEmailService();
            System.out.println("Gmail service initialized successfully");
        } catch (Exception e) {
            System.out.println("Warning: Gmail service initialization failed: " + e.getMessage());
            System.out.println("Gmail functionality will not be available until credentials.json is configured");
        }
        // Initialize KnowledgeBasePdfTool with demo PDF
        try {
            String geminiApiKey = System.getenv("GEMINI_API_KEY");
            org.example.GeminiEmbeddingClient embeddingClient = new org.example.GeminiEmbeddingClient(vertx, "AIzaSyDXb4Z2YYL5OkhipzCcq6u0idGzMPHsuvA");
            knowledgeBasePdfTool = new KnowledgeBasePdfTool(embeddingClient);
            String demoPdfPath = "/Users/ashish/IdeaProjects/mcp-vertx/src/resources/new_resume (1).pdf"; // TODO: Replace with actual PDF path
          //  knowledgeBasePdfTool.loadAndEmbedPdf(demoPdfPath);
            System.out.println("Knowledge base PDF loaded and embedded.");
        } catch (Exception e) {
            System.out.println("Warning: Knowledge base PDF load failed: " + e.getMessage());
        }

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        // Add CORS handler to handle OPTIONS requests
        router.route().handler(CorsHandler.create()
                .addOrigin("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .allowedHeader("X-Requested-With"));
        
        router.route().handler(BodyHandler.create());
        router.get("/mcp/sse").handler(this::handleSseConnect);
        router.post("/mcp/message").handler(this::handleMessage);
        router.post("/mcp/cancel").handler(this::handleCancel);
        router.post("/mcp/gmail/auth").handler(this::handleGmailAuth);
        router.post("/stream/chat").handler(ChatBot.Instance::query_assistant_agent);
        router.post("/mcp/expense/stream/chat").handler(ChatBot.Instance::query_assistant_agent);
        router.post("/stream/login").handler(ChatBot.Instance::login_issuer_agent);
        router.post("/stream/analyser").handler(ChatBot.Instance::analyser_agent);
        router.post("/stream/decision").handler(ChatBot.Instance::decision_agent);
        router.post("/stream/saas").handler(ChatBot.Instance::saas_agent);
        router.post("/create/newChat").handler(ChatBot.Instance::addNewChatSession);
        router.get("/allChats").handler(ChatBot.Instance::getAllChats);
        server.requestHandler(router).listen(8080);
        System.out.println("MCP Server running at http://localhost:8080");
    }


    public void handleChatStream(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true); 
        response.write("event: result\n");
        response.write("data: " + "working fine" + "\n\n");

    }

    public void handleMessage(RoutingContext ctx) {
        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }

        String method = jsonRpcRequest.getString("method");
        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        String id = jsonRpcRequest.getString("id", UUID.randomUUID().toString());

        HttpServerResponse response = clients.get("clientCursor");
        JsonObject result = handleJsonRpc(method, params);
        result.put("id", id);

//        response.write("event: result\n");
//        response.write("data: " + result.encode() + "\n\n");
        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                        .put("success", true)
                        .put("data", new JsonObject(result.encode()))
                        .encode());
//        Promise<String> job = Promise.promise();
//        jobs.put(id, job);
//
//        final int[] count = {0};
//        final long intervalId = vertx.setPeriodic(100, timerId -> {
//            if (count[0] >= 1000) {
//                vertx.cancelTimer(timerId);
//                job.complete("done");
//                ctx.response().setStatusCode(200).end();
//                return;
//            }
//            if (response != null) {
//                response.write("event: result\n");
//                response.write("data: " + result.encode() + "\n\n");
//            }
//            count[0]++;
//        });
//        jobTimers.put(id, intervalId);

    }

    private void handleSseConnect(RoutingContext ctx) { // this is sending to client
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true);
        response.write("event: connected\n");
        response.write("data: {\"status\":\"connected\"}\n\n");
        clients.put("clientCursor", response);
        ctx.request().connection().closeHandler(v -> {
            clients.remove("clientCursor");
            System.out.println("Client disconnected");
        });
    }

    private void handleCancel(RoutingContext ctx) {
        JsonObject cancelReq = ctx.getBodyAsJson();
        String jobId = cancelReq.getString("jobId");

        Promise<String> job = jobs.get(jobId);
        Long timerId = jobTimers.get(jobId);

        if (job != null && !job.future().isComplete()) {
            job.fail("Cancelled");
            jobs.remove(jobId);
            if (timerId != null) {
                vertx.cancelTimer(timerId);
                jobTimers.remove(jobId);
            }

            // Optionally send SSE cancel event
            HttpServerResponse sse = clients.get("clientCursor");
            if (sse != null) {
                sse.write("event: cancel\n");
                sse.write("data: {\"jobId\":\"" + jobId + "\", \"status\":\"cancelled\"}\n\n");
            }

            ctx.response().end("Cancelled job " + jobId);
        } else {
            ctx.response().end("Job not found or already completed");
        }
    }

    private void handleGmailAuth(RoutingContext ctx) {
        if (gmailService == null) {
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("success", false)
                            .put("error", "Gmail service not initialized. Please check credentials.json configuration.")
                            .encode());
            return;
        }

        JsonObject result = gmailService.authenticate();
        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(result.encode());
    }


    private JsonObject handleJsonRpc(String method, JsonObject params) {
        switch (method) {
            case "feed_query":
                return new JsonObject().put("message",CreateEmbeddings.createEmbedding(new ArrayList<>()));
            case "get_resolution":
                return new JsonObject().put("documents",VectorQuery.getExpenseResolution(params.getString("query"),params.getString("module"),false));
            case "get_answer_from_KnowledgeBase":
                if (knowledgeBasePdfTool == null) {
                    return new JsonObject().put("error", "Knowledge base not initialized");
                }
                String userQuery = params.getString("query");
                String answer = knowledgeBasePdfTool.query(userQuery);
                return new JsonObject().put("answer", answer);
            case "get_tools":
                List<Tool> tools = List.of(
                        new Tool("get_single_expense", "Get a single expense why using expense id", new JsonObject()
                                .put("type", "object")
                                .put("properties", new JsonObject().put("operation", new JsonObject().put("type", "string")))),
                        new Tool("get_expenses", "Get all expense for a user  using user id", new JsonObject()
                                .put("type", "object")
                                .put("properties", new JsonObject())),
                        new Tool("get_single_voucher", "Get a single expense why using voucher id", new JsonObject()
                                .put("type", "object")
                                .put("properties", new JsonObject().put("query", new JsonObject().put("type", "string")))),
                        new Tool("get_today_emails", "Get today's emails from Gmail inbox", new JsonObject()
                                .put("type", "object")
                                .put("properties", new JsonObject())),
                        new Tool("get_services", "Get answer from PDF knowledge base", new JsonObject()
                                .put("type", "object")
                                .put("properties", new JsonObject().put("query", new JsonObject().put("type", "string"))))
                );
                JsonArray toolList = new JsonArray();
                for (Tool tool : tools) {
                    toolList.add(tool.toJson());
                }
                JsonObject object = new JsonObject();
                object.put("tools", toolList);
                return object;
            case "get_files":
                return new JsonObject().put("files",
                        new JsonArray().add("booking.csv").add("invoice.pdf"));
            case "get_team_approvals":
                return new JsonObject().put("approvals",
                        new JsonArray().add("voucher123").add("voucher456"));
            case "get_todays_emails":
                if (gmailService == null) {
                    return new JsonObject()
                            .put("error", "Gmail service not initialized. Please check credentials.json configuration.")
                            .put("authenticated", false);
                }
                return gmailService.getTodaysEmails(params.getString("date"));
            default:
                return new JsonObject().put("error", "Method not found: " + method);
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxMcpSseServer());
    }


    public class Tool {
        private final String name;
        private final String description;
        private final JsonObject inputSchema;

        public Tool(String name, String description, JsonObject inputSchema) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
        }

        public JsonObject toJson() {
            return new JsonObject()
                    .put("name", name)
                    .put("description", description)
                    .put("input_schema", inputSchema);
        }
    }
}
