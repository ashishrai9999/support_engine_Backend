package org.example;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.example.Mongo.VectorQuery;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;

public enum ChatBot {
    Instance;
    private long tokenCount = 0;

    public void login_issuer_agent(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true);

        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            response.setStatusCode(400).end("Invalid JSON body");
            return;
        }
        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        String userQuery = params.getString("query");
        String module = params.getString("module");
        String context = VectorQuery.getLoginResolution(userQuery, module, false).toString();
        String sessionId = params.getString("sessionId", UUID.randomUUID().toString());

        // Get or create chat session
        ChatSession chatSession = ChatManager.Instance.getChatBySessionId(sessionId);
        if (chatSession == null) {
            // Create new chat session if it doesn't exist
            chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setCreatedAt(System.currentTimeMillis());
            chatSession.setClientId(params.getString("clientId", "unknown"));
            chatSession.setEmployeeId(params.getString("employeeId", "unknown"));
            chatSession.setModule(module);
            chatSession.setUpdatedAt(System.currentTimeMillis());
            try {
                ChatManager.Instance.saveChat(chatSession);
            } catch (Exception e) {
                e.printStackTrace();
                response.write("event: error\ndata: " + e.getMessage() + "\n\n");
                response.end();
                return;
            }
        }

        // Add user message to session
        ChatSession.Message userMsg = new ChatSession.Message();
        userMsg.setRole("user");
        userMsg.setContent(userQuery);
        userMsg.setTimestamp(System.currentTimeMillis());
        chatSession.getMessages().add(userMsg);

        String prompt = """
                ## ROLE AND GOAL
                You are an expert login assistant. Your goal is to provide users with clear, step-by-step instructions for their login-related problems by interpreting a given context. The context contains a potential answer and a confidence score.
                
                ## INSTRUCTIONS
                
                **1. Evaluate Confidence:**
                   - **IF the confidence score is 0.82 or higher**, proceed to Step 2 to generate a helpful guide.
                   - **IF the confidence score is below 0.82 OR no answer is found in the context**, you must stop and respond with only the following exact message:
                     "Sorry, I can't help you. Please contact the support team."
                
                **2. Generate a Helpful Guide:**
                   a. **Identify the User's Problem:** First, analyze the `userQuery` to pinpoint the user's specific login issue (e.g., forgotten password, locked account, two-factor authentication trouble).
                
                   b. **Construct a Step-by-Step Solution:** Extract the necessary actions from the `context`. Synthesize this information into a clear, numbered list that guides the user through the solution. **Do not just output a paragraph of text.** The solution should be a direct and actionable guide.
                
                   c. **Adhere to Quality Standards:**
                      - **Clarity and Tone:** Use simple, professional, and reassuring language. Each step should be a clear action for the user to take.
                      - **Format:** You **must** use a numbered list for sequential steps (e.g., 1., 2., 3.). Each item in the list must be a complete, grammatically correct sentence.
                      - **Precision:** Ensure the guide directly solves the user's specific problem as identified in their query.
                
                --- BEGIN CONTEXT ---
                %s
                --- END CONTEXT ---
                
                --- BEGIN QUESTION ---
                %s
                --- END QUESTION ---
                """.formatted(context, userQuery);

        // Use the session-aware streaming method
        handleChatStreamWithSession(ctx, response, prompt, chatSession, sessionId);
    }

    public void query_assistant_agent(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true);

        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            response.setStatusCode(400).end("Invalid JSON body");
            return;
        }
        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        String userQuery = params.getString("query");
        String module = params.getString("module");
        String context = VectorQuery.getExpenseResolution(userQuery, module, false).toString();
        String sessionId = params.getString("sessionId", UUID.randomUUID().toString());

        // Get or create chat session
        ChatSession chatSession = ChatManager.Instance.getChatBySessionId(sessionId);
        if (chatSession == null) {
            // Create new chat session if it doesn't exist
            chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setCreatedAt(System.currentTimeMillis());
            chatSession.setClientId(params.getString("clientId", "unknown"));
            chatSession.setEmployeeId(params.getString("employeeId", "unknown"));
            chatSession.setModule(module);
            chatSession.setUpdatedAt(System.currentTimeMillis());
            try {
                ChatManager.Instance.saveChat(chatSession);
            } catch (Exception e) {
                e.printStackTrace();
                response.write("event: error\ndata: " + e.getMessage() + "\n\n");
                response.end();
                return;
            }
        }

        // Add user message to session
        ChatSession.Message userMsg = new ChatSession.Message();
        userMsg.setRole("user");
        userMsg.setContent(userQuery);
        userMsg.setTimestamp(System.currentTimeMillis());
        chatSession.getMessages().add(userMsg);

        String prompt = """
                ## ROLE AND GOAL
                You are a highly capable AI assistant. Your primary function is to provide clear and accurate answers to user questions by interpreting a given context. The context contains a potential answer and a confidence score that you must use to guide your response.
                
                ## INSTRUCTIONS
                
                **1. Analyze the Confidence Score:**
                   - **IF the confidence score is 0.85 or higher**, proceed to Step 2.
                   - **IF the confidence score is below 0.85 OR if no answer is found in the context**, you must respond with the following exact message and stop:
                     "Sorry, I can't help you. Please contact the support team."
                
                **2. High-Confidence Response Generation:**
                   a. **Understand User Intent:** Deeply analyze the `userQuery` to understand the user's underlying goal, not just the literal words.
                
                   b. **Synthesize the Answer:** Based on the user's intent, extract the relevant information from the `context`. Synthesize a new, well-structured response. **Do not simply copy or slightly rephrase the provided answer.**
                
                   c. **Apply Formatting and Tone:**
                      - **Professional Tone:** The response must be clear, concise, and professionally worded.
                      - **Structured Format:** If the information involves a process, multiple items, or a sequence of actions, present it as a step-by-step guide or a bulleted list for maximum clarity. Avoid dense paragraphs where a list would be more effective.
                      - **Grammatical Accuracy:** Ensure the final output is grammatically perfect and written in complete sentences.
                
                --- BEGIN CONTEXT ---
                %s
                --- END CONTEXT ---
                
                --- BEGIN QUESTION ---
                %s
                --- END QUESTION ---
                """.formatted(context, userQuery);

        // Use the session-aware streaming method
        handleChatStreamWithSession(ctx, response, prompt, chatSession, sessionId);
    }


    public void analyser_agent(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true);

        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            response.setStatusCode(400).end("Invalid JSON body");
            return;
        }
        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        String userQuery = params.getString("query");
        String module = params.getString("module");
        String sessionId = params.getString("sessionId", UUID.randomUUID().toString());

        // Get or create chat session
        ChatSession chatSession = ChatManager.Instance.getChatBySessionId(sessionId);
        if (chatSession == null) {
            // Create new chat session if it doesn't exist
            chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setCreatedAt(System.currentTimeMillis());
            chatSession.setClientId(params.getString("clientId", "unknown"));
            chatSession.setEmployeeId(params.getString("employeeId", "unknown"));
            chatSession.setModule(module);
            chatSession.setUpdatedAt(System.currentTimeMillis());
            try {
                ChatManager.Instance.saveChat(chatSession);
            } catch (Exception e) {
                e.printStackTrace();
                response.write("event: error\ndata: " + e.getMessage() + "\n\n");
                response.end();
                return;
            }
        }

        // Add user message to session
        ChatSession.Message userMsg = new ChatSession.Message();
        userMsg.setRole("user");
        userMsg.setContent(userQuery);
        userMsg.setTimestamp(System.currentTimeMillis());
        chatSession.getMessages().add(userMsg);

        String prompt = """ 
                                        You are a specialized Analyser Agent. Your single-minded purpose is to analyze a user's query about the EXPENSE system and determine the specific data submodule and metadata required to answer it.\\n" +
                                        "\\n" +
                                        "Your output is critical as it will be passed directly to an SQL Agent to query a database. Therefore, your response must be precise, structured, and strictly follow the specified format. You must use the detailed analysis guide below to map user intent to the correct output.\\n" +
                                        "\\n" +
                                        "## Instructions\\n" +
                                        "Analyze the Query: Carefully read the user's query to understand its core intent.\\n" +
                                        "\\n" +
                                        "Use the Guide: Refer to the Keyword & Intent Analysis Guide below to map user keywords and intent to the correct Submodule and Metadata. This is your primary logic map.\\n" +
                                        "\\n" +
                                        "Extract IDs: If the user provides a specific identifier like an Expense ID or Voucher ID (e.g., \\"EX-98765\\"), you must extract it and include it in the metadata.\\n" +
                                        "\\n" +
                                        "Format Output: Respond ONLY with a single line in the specified format. Do not add any explanations or conversational text.\\n" +
                                        "\\n" +
                                        "## Keyword & Intent Analysis Guide\\n" +
                                        "Use this guide to determine the correct Submodule and Metadata.\\n" +
                                        "\\n" +
                                        "If the User's Intent is to...\\tAnd the Query Contains Keywords Like...\\tChoose Submodule\\tAnd Use Metadata\\n" +
                                        "Check Status or Workflow\\tpending, approver, approved, status, declined, finance team, manager, workflow, who, where, why not approved, wrong approver, manager not mapped\\tApprovals\\tneeded details of approvals or workflow info, need voucher status\\n" +
                                        "Question a Payment Amount\\tless amount, refund, reimbursement, claim as per policy, deviation approval, \\"why I got less amount\\"\\tPayments\\tneeded payment related query\\n" +
                                        "Troubleshoot Voucher Submission\\tsubmit voucher, violation, can't find transaction, unable to submit, error\\tVouchers\\tneeded information related to expenses (to check against rules)\\n" +
                                        "Manage a Specific Expense\\twithdraw expense, delete expense (referencing a specific one)\\tExpenses\\tneed extracted Expense ID, needed information related to expenses\\n" +
                                        "Ask a General or \\"How-To\\" Question\\thow do I, what is, can you explain, walk me through, services not visible, expense heads not visible, can I create\\tGeneral\\tNo database lookup required\\n" +
                                        "\\n" +
                                        "Export to Sheets\\n" +
                                        "## Output Format\\n" +
                                        "Respond ONLY with a single line in the following format:\\n" +
                                        "\\n" +
                                        "Submodule: [Identified Submodule], Metadata: [List of required metadata tags, including any extracted IDs]\\n" +
                                        "\\n" +
                                        "## Examples\\n" +
                                        "Example 1: Status Query\\n" +
                                        "\\n" +
                                        "User Query: \\"Why is my voucher still pending with my manager?\\"\\n" +
                                        "\\n" +
                                        "Your Output: Submodule: Approvals, Metadata: needed details of approvals or workflow info, need voucher status\\n" +
                                        "\\n" +
                                        "Example 2: Query with an ID\\n" +
                                        "\\n" +
                                        "User Query: \\"Why has my voucher for expense EX-98765 been declined?\\"\\n" +
                                        "\\n" +
                                        "Your Output: Submodule: Approvals, Metadata: need extracted Expense ID: EX-98765, needed details of approvals or workflow info\\n" +
                                        "\\n" +
                                        "Example 3: Payment Query\\n" +
                                        "\\n" +
                                        "User Query: \\"My expense was for $100 but I only got $80 back. I think I had to 'claim as per policy'.\\"\\n" +
                                        "\\n" +
                                        "Your Output: Submodule: Payments, Metadata: needed payment related query\\n" +
                                        "\\n" +
                                        "Example 4: General \\"How-To\\" Query\\n" +
                                        "\\n" +
                                        "User Query: \\"Can you walk me through the steps to create an expense?\\"\\n" +
                                        "\\n" +
                                        "Your Output: Submodule: General, Metadata: No database lookup required\\n" +
                                        "\\n" +
                                        "Example 5: System Configuration Query\\n" +
                                        "\\n" +
                                        "User Query: \\"The service I need to file for is not visible in the list. What should I do?\\"\\n" +
                                        "\\n" +
                                        "Your Output: Submodule: General, Metadata: No database lookup required\\n" +
                                        "\\n" +
                                        "Example 6: Voucher Violation Query\\n" +
                                        "\\n" +
                                        "User Query: \\"I am unable to submit my voucher due to violations.\\"\\n" +
                                        "\\n" +
                                        "Your Output: Submodule: Vouchers, Metadata: needed information related to expenses"
                
                QUERY: %s
                """.formatted(userQuery);

        // Use the session-aware streaming method
        handleChatStreamWithSession(ctx, response, prompt, chatSession, sessionId);
    }


    public void decision_agent(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true);

        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            response.setStatusCode(400).end("Invalid JSON body");
            return;
        }
        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        String userQuery = params.getString("query");
        String module = params.getString("module");
        String context = VectorQuery.getExpenseResolution(userQuery, module, false).toString();
        String sessionId = params.getString("sessionId", UUID.randomUUID().toString());

        // Get or create chat session
        ChatSession chatSession = ChatManager.Instance.getChatBySessionId(sessionId);
        if (chatSession == null) {
            // Create new chat session if it doesn't exist
            chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setCreatedAt(System.currentTimeMillis());
            chatSession.setClientId(params.getString("clientId", "unknown"));
            chatSession.setEmployeeId(params.getString("employeeId", "unknown"));
            chatSession.setModule(module);
            chatSession.setUpdatedAt(System.currentTimeMillis());
            try {
                ChatManager.Instance.saveChat(chatSession);
            } catch (Exception e) {
                e.printStackTrace();
                response.write("event: error\ndata: " + e.getMessage() + "\n\n");
                response.end();
                return;
            }
        }

        // Add user message to session
        ChatSession.Message userMsg = new ChatSession.Message();
        userMsg.setRole("user");
        userMsg.setContent(userQuery);
        userMsg.setTimestamp(System.currentTimeMillis());
        chatSession.getMessages().add(userMsg);

        String prompt = """
                You are the **Analyser Agent**. Your role is to efficiently process user queries and decide the next step. Your primary goal is to determine if a query requires fetching specific data from a database or if it can be answered immediately using the provided knowledge base context.
                
                Analyze the provided CONTEXT (which contains results from a vector database) and the user's Question based on the rules below.
                
                ### **Decision Instructions**
                
                You **must** delegate the query to the **Action Agent** if **any** of the following conditions are true. Otherwise, you can respond directly.
                
                1.  **Check for Specific IDs 🆔 in the Question**
                    * Examine the Question for an **Expense ID** (e.g., "EXP-12345", "expense 9876") or a **Voucher ID** (e.g., "VCH-54321", "voucher 2345").
                    * If an ID is present, the query requires a specific database lookup. **Decision: DELEGATE.**
                
                2.  **Evaluate Confidence in the CONTEXT 🤔**
                    * The CONTEXT contains a `confidence_score`.
                    * If this confidence score is low (e.g., less than `0.80`), the retrieved information is not a reliable match. To avoid providing an incorrect answer, you must get more information. **Decision: DELEGATE.**
                
                --- BEGIN CONTEXT ---
                %s
                --- END CONTEXT ---
                
                Question: %s
                
                ---
                
                ### **Required Output Format**
                
                Based on your analysis of the context and question, provide your final output. Your response MUST be only the decision and the reason.
                
                **If delegating to the Action Agent:**
                * **Decision:** `DELEGATE`
                * **Reason:** State which condition was met (e.g., "Expense ID found in query," or "Vector DB confidence score is low.").
                
                **If responding directly:**
                * **Decision:** `RESPOND`
                * **Reason:** "High confidence result found in the knowledge base and no specific IDs were mentioned in the query."
                """.formatted(context, userQuery);

        handleChatStreamWithSession(ctx, response, prompt, chatSession, sessionId);
    }

    private void handleChatStreamWithSession(RoutingContext ctx, HttpServerResponse response, String prompt, final ChatSession chatSession, String sessionId) {
        final StringBuilder assistantResponse = new StringBuilder();
        GeminiService geminiService = new GeminiService(ctx.vertx());
        geminiService.streamChatResponse(
                prompt,
                tokenChunk -> {
                    try {
                        response.write("data: " + new JsonObject().put("token", tokenChunk).put("tokenCount", tokenCount).encode() + "\n\n");
                        assistantResponse.append(tokenChunk);
                        tokenCount++;
                    } catch (Exception e) {
                        response.write("event: error\ndata: " + e.getMessage() + "\n\n");
                    }
                },
                error -> {
                    error.printStackTrace();
                    response.write("event: error\ndata: " + error.getMessage() + "\n\n");
                    response.end();
                },
                v -> {
                    try {
                        ChatSession.Message assistantMsg = new ChatSession.Message();
                        assistantMsg.setRole("assistant");
                        assistantMsg.setContent(assistantResponse.toString());
                        assistantMsg.setTimestamp(System.currentTimeMillis());
                        chatSession.getMessages().add(assistantMsg);
                        chatSession.setUpdatedAt(System.currentTimeMillis());
                        ChatManager.Instance.updateChatSession(chatSession);
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.write("event: error\ndata: Failed to save chat session: " + e.getMessage() + "\n\n");
                        response.end();
                    }
                }
        );
        System.out.println("Doneeeeee");
        tokenCount = 0;
    }


    private void handleChatStream(RoutingContext ctx, HttpServerResponse response, String prompt) {
        final StringBuilder assistantResponse = new StringBuilder();
        GeminiService geminiService = new GeminiService(ctx.vertx());
        geminiService.streamChatResponse(
                prompt,
                tokenChunk -> {
                    try {
                        System.out.println("token chunk is " + tokenChunk);
                        response.write("data: " + new JsonObject().put("token", tokenChunk).put("tokenCount", tokenCount).encode() + "\n\n");
                        assistantResponse.append(tokenChunk);
                        tokenCount++;
                    } catch (Exception e) {
                        response.write("event: error\ndata: " + e.getMessage() + "\n\n");
                    }
                },
                error -> {
                    error.printStackTrace();
                    response.write("event: error\ndata: " + error.getMessage() + "\n\n");
                    response.end();
                },
                v -> {
                    try {
                        response.end();
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.write("event: error\ndata: Failed to save chat session: " + e.getMessage() + "\n\n");
                        response.end();
                    }
                }
        );
        System.out.println("Doneeeeee");
        tokenCount = 0;
    }

    private void handleChatStreamWithSessionSaarAgent(RoutingContext ctx, HttpServerResponse response, String prompt, final ChatSession chatSession) {
        // --- KEY IMPROVEMENT ---
        // Each variable is now local to this method call to ensure thread safety.
        // If tokenCount were a class field, concurrent requests would interfere with each other.
        final StringBuilder assistantResponse = new StringBuilder();
        final int[] tokenCount = {0}; // Using an array to be modifiable within a lambda

        GeminiService geminiService = new GeminiService(ctx.vertx());

        geminiService.streamChatResponse(
                prompt,
                // 1. onChunk: Called for each piece of the response from the LLM
                tokenChunk -> {
                    try {
                        // Append the chunk to our full response buffer
                        assistantResponse.append(tokenChunk);

                        // Stream the chunk immediately to the client
                        JsonObject sseData = new JsonObject()
                                .put("token", tokenChunk)
                                .put("tokenCount", tokenCount[0]++); // Increment and send count

                        response.write("data: " + sseData.encode() + "\n\n");

                    } catch (IllegalStateException e) {
                        // This can happen if the client closes the connection prematurely.
                        System.err.println("Error writing to response, client may have disconnected: " + e.getMessage());
                        // We can't send an error to the client, so we just log it.
                        // The stream in GeminiService should ideally be closed here.
                    }
                },
                // 2. onError: Called if there is an error during the streaming process
                error -> {
                    System.err.println("An error occurred during streaming to the client.");
                    error.printStackTrace();
                    if (!response.ended()) {
                        response.write("event: error\ndata: " + error.getMessage() + "\n\n");
                        response.end();
                    }
                },
                // 3. onComplete: Called when the LLM has finished sending all chunks
                (unused) -> {
                    try {
                        System.out.println("Stream completed. Saving assistant response to session.");

                        // Create the message object for the assistant's full response
                        ChatSession.Message assistantMsg =  new ChatSession.Message();
                        assistantMsg.setRole("assistant");
                        assistantMsg.setContent(assistantResponse.toString());
                        assistantMsg.setTimestamp(System.currentTimeMillis());

                        // Add the message and update the session
                        chatSession.getMessages().add(assistantMsg);
                        chatSession.setUpdatedAt(System.currentTimeMillis());
                        ChatManager.Instance.updateChatSession(chatSession); // Or saveChat(chatSession)

                        System.out.println("Session " + chatSession.getSessionId() + " updated successfully.");

                    } catch (Exception e) {
                        System.err.println("Failed to save the final chat session.");
                        e.printStackTrace();
                        if (!response.ended()) {
                            response.write("event: error\ndata: Failed to save chat session: " + e.getMessage() + "\n\n");
                        }
                    } finally {
                        // Ensure the connection is always closed properly on completion
                        if (!response.ended()) {
                            response.end();
                        }
                    }
                }
        );
    }
    public static String analyser_agent_prompt = String.join("\n",
            "You are the **Analyser Agent**. Your role is to efficiently process user queries and decide the next step. Your primary goal is to determine if a query requires fetching specific data from a database or if it can be answered immediately using the provided knowledge base context.",
            "",
            "You will receive the following inputs:",
            "* `user_query`: The exact question or statement from the user.",
            "* `vector_db_results`: A list of relevant results from the FAQ knowledge base, each with a `confidence_score`.",
            "",
            "---",
            "",
            "### **Decision Instructions**",
            "",
            "You **must** delegate the query to the **Action Agent** if **any** of the following conditions are true. Otherwise, you can respond directly.",
            "",
            "1.  **Check for Specific IDs **",
            "    * Examine the `user_query` for an **Expense ID** (e.g., \"EXP-12345\", \"expense 9876\") or a **Voucher ID** (e.g., \"VCH-54321\", \"voucher 2345\").",
            "    * If an ID is present, the query requires a specific database lookup. **Decision: DELEGATE.**",
            "",
            "2.  **Evaluate Vector DB Confidence **",
            "    * Look at the `confidence_score` of the top result provided in `vector_db_results`.",
            "    * If the confidence score is low (e.g., less than `0.80`), the retrieved information is not a reliable match. To avoid providing an incorrect answer, you must get more information. **Decision: DELEGATE.**",
            "",
            "---",
            "",
            "### **Output Format**",
            "",
            "Your final output must be a decision, along with a clear reason.",
            "",
            "**If delegating to the Action Agent:**",
            "* **Decision:** `DELEGATE`",
            "* **Reason:** State which condition was met (e.g., \"Expense ID found in query,\" or \"Vector DB confidence score is low.\").",
            "",
            "**If responding directly:**",
            "* **Decision:** `RESPOND`",
            "* **Reason:** \"High confidence result found in the knowledge base and no specific IDs were mentioned in the query.\""
    ) + "\n";

    public void addNewChatSession(RoutingContext ctx) {
        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            ctx.response().setStatusCode(400).end("Invalid JSON body");
            return;
        }

        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(UUID.randomUUID().toString());
        chatSession.setCreatedAt(System.currentTimeMillis());
        chatSession.setClientId(params.getString("clientId", "unknown"));
        chatSession.setEmployeeId(params.getString("employeeId", "unknown"));
        chatSession.setModule(params.getString("module", "Travel"));
        chatSession.setUpdatedAt(System.currentTimeMillis());

        try {
            ChatManager.Instance.saveChat(chatSession);
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("success", true)
                            .put("data", new JsonObject(chatSession.toDocument().toJson()))
                            .encode());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("success", false)
                            .put("error", "Failed to create chat session: " + e.getMessage())
                            .encode());
        }
    }

    public void getAllChats(RoutingContext ctx) {
        try {
            String clientId = ctx.request().getParam("clientId");
            String employeeId = ctx.request().getParam("employeeId");
            List<ChatSession> chatSessions = ChatManager.Instance.getAllChats(clientId, employeeId);

            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("success", true)
                            .put("data", chatSessions)
                            .encode());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject()
                            .put("success", false)
                            .put("error", "Failed to retrieve chat sessions: " + e.getMessage())
                            .encode());
        }
    }

    /**
     * SaaS Agent that integrates with external MCP server
     * Flow: User Query + Tools Definitions -> LLM decides tools -> Call tools -> LLM generates final response
     * Chat session storage is used to maintain conversation context
     */
    public void saas_agent(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.putHeader("Content-Type", "text/event-stream");
        response.putHeader("Cache-Control", "no-cache");
        response.putHeader("Connection", "keep-alive");
        response.setChunked(true);

        JsonObject jsonRpcRequest = ctx.getBodyAsJson();
        if (jsonRpcRequest == null) {
            response.setStatusCode(400).end("Invalid JSON body");
            return;
        }

        JsonObject params = jsonRpcRequest.getJsonObject("params", new JsonObject());
        String userQuery = params.getString("query");
        String diceAppId = params.getString("diceAppId", "INDIAN");
        String clientId = "03ca45af9";
        String clientSecret = params.getString("clientSecret", "d99c6259a4964f5ea7e575b6c0adfe4b");
        String sessionId = params.getString("sessionId", UUID.randomUUID().toString());

        // Get or create chat session (your existing logic is good)
        ChatSession chatSession = ChatManager.Instance.getChatBySessionId(sessionId);
        if (chatSession == null) {
            chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setModule("SaaS");
        }
        chatSession.setUpdatedAt(System.currentTimeMillis());

        ChatSession.Message userMsg = new ChatSession.Message();
        userMsg.setRole("assistant");
        userMsg.setContent(userQuery);
        userMsg.setTimestamp(System.currentTimeMillis());
        chatSession.getMessages().add(userMsg);

        // Persist the user message immediately
        try {
            ChatManager.Instance.saveChat(chatSession);
        } catch (Exception e) {
            e.printStackTrace();
            response.write("event: error\ndata: Failed to save chat session: " + e.getMessage() + "\n\n");
            response.end();
            return;
        }

        // Final reference for use in lambdas
        final ChatSession finalChatSession = chatSession;

        System.out.println("Starting SaaS agent for query: " + userQuery);
        SaasMcpServer saasServer = new SaasMcpServer(ctx.vertx());
        JsonObject toolDefinition = saasServer.getToolViaRetrofit();

        // NEW: Build chat history to provide context to the LLM
        String chatHistory = buildChatHistory(finalChatSession);

        String toolDecisionPrompt = """
                You are a SaaS Agent that analyzes user queries and decides which tools to call. You must consider the conversation history for context.
                
                ## Conversation History:
                %s

                ## Available Tools:
                %s
                
                ## Latest User Query:
                %s
                
                ## Instructions:
                1.  Analyze the latest user query in the context of the conversation history.
                2.  Review the available tools and decide which single tool is most appropriate.
                3.  Extract any necessary arguments from the user query or conversation history.
                4.  If no tool matches the user's request, respond with :
                {
                    "tool_name": "NO_TOOL_NEEDED",
                    "arguments": {},
                    "reason": "No suitable tool found for this query"
                }.
                
                ## Output Format:
                Respond with a JSON object in this exact format, without any surrounding text or markdown fences:
                {
                    "tool_name": "name_of_tool_to_call",
                    "arguments": {
                        "param1": "value1",
                        "param2": "value2"
                    },
                    "reason": "Brief explanation of why this tool was chosen based on the query and history."
                }
                """.formatted(chatHistory, toolDefinition, userQuery);

        callLLMForToolDecisionNew(ctx, toolDecisionPrompt)
                .onSuccess(llmResponseText -> {
                    try {
                        // IMPROVED: Use robust regex-based JSON extraction
                        String jsonString = extractJsonBlock(llmResponseText);
                        if (jsonString == null) {
                            throw new Exception("LLM did not return a valid JSON object.");
                        }

                        JsonObject decision = new JsonObject(jsonString);
                        String toolName = decision.getString("tool_name");
                        System.out.println("Tool Name ---------> " + toolName);

                        if ("NO_TOOL_NEEDED".equals(toolName)) {
                            // No tool needed, generate direct response
                            String directResponsePrompt = """
                                    You are a helpful SaaS assistant. The user has asked a question that doesn't require any tools.
                                    Use the conversation history for context.
                                    
                                    ## Conversation History:
                                    %s

                                    ## User Query: %s
                                    
                                    Please provide a helpful and informative response.
                                    """.formatted(chatHistory, userQuery);

                            handleChatStreamWithSessionSaarAgent(ctx, response, directResponsePrompt, finalChatSession);
                        } else {
                            // Call the selected tool
                            JsonObject arguments = decision.getJsonObject("arguments", new JsonObject());
                            System.out.println("Arguments are "+ arguments);
                            // NEW: Provide immediate feedback to the user before the tool call
                            JsonObject toolCallEvent = new JsonObject()
                                    .put("tool_name", toolName)
                                    .put("arguments", arguments)
                                    .put("status", "pending")
                                    .put("message", "Calling tool: " + toolName + "...");
                            response.write("event: tool_call\n");
                            response.write("data: " + toolCallEvent.encode() + "\n\n");


                            JsonObject toolResult = saasServer.callToolViaRetrofit(toolName, arguments, diceAppId, clientId, clientSecret);

                            String finalResponsePrompt = """
                                    You are a SaaS Agent that has called a tool and received results. Generate a helpful, professional response for the user, considering the full conversation.
                                    
                                    ## Conversation History:
                                    %s

                                    ## Tool Called:
                                    %s
                                    
                                    ## Tool Arguments:
                                    %s
                                    
                                    ## Tool Response (JSON):
                                    %s
                                    
                                    ## Instructions:
                                    1.  Start with a brief, friendly sentence confirming you have the information.
                                    2.  Present the main details of the transaction in a two-column Markdown table. The left column should be "Detail" and the right column "Information".
                                    3.  Make sure any URLs, like the receipt link, are presented as clickable Markdown links.
                                    4.  Conclude with a helpful closing, asking if the user needs anything else.
                                    5.  If the tool response indicates an error or has no data, explain this to the user clearly and professionally instead of showing a table.
                                    """.formatted(chatHistory, toolName, arguments.encodePrettily(), toolResult.encodePrettily());

                            // IMPROVED: This handler should also save the agent's response to the session
                            handleChatStreamWithSessionSaarAgent(ctx, response, finalResponsePrompt, finalChatSession);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse tool decision: " + e.getMessage());
                        response.write("event: error\ndata: Failed to process agent logic: " + e.getMessage() + "\n\n");
                        response.end();
                    }
                })
                .onFailure(error -> {
                    System.err.println("Failed to get tool decision: " + error.getMessage());
                    response.write("event: error\ndata: Failed to get tool decision from LLM: " + error.getMessage() + "\n\n");
                    response.end();
                });
    }

    /**
     * Call LLM to decide which tool to use
     */
    private Future<String> callLLMForToolDecision(RoutingContext ctx, String prompt) {
        Promise<String> promise = Promise.promise();
        StringBuilder response = new StringBuilder();

        GeminiService geminiService = new GeminiService(ctx.vertx());
        geminiService.streamChatResponse(
                prompt,
                tokenChunk -> {
                    response.append(tokenChunk);
                },
                error -> {
                    promise.fail("LLM error: " + error.getMessage());
                },
                v -> {
                    promise.complete(response.toString());
                }
        );

        return promise.future();
    }

    private Future<String> callLLMForToolDecisionNew(RoutingContext ctx, String prompt) {
        Promise<String> promise = Promise.promise();
        StringBuilder response = new StringBuilder();

        GeminiService geminiService = new GeminiService(ctx.vertx());
        geminiService.streamChatResponse(
                prompt,
                tokenChunk -> {
                    response.append(tokenChunk);
                },
                error -> {
                    System.err.println("LLM error: " + error.getMessage());
                    promise.fail("LLM error: " + error.getMessage());
                },
                v -> {
                    promise.complete(response.toString());
                }
        );

        return promise.future();
    }

    /**
     * Call LLM to generate final response
     */
    private Future<String> callLLMForFinalResponse(RoutingContext ctx, String prompt) {
        Promise<String> promise = Promise.promise();
        StringBuilder response = new StringBuilder();

        GeminiService geminiService = new GeminiService(ctx.vertx());
        geminiService.streamChatResponse(
                prompt,
                tokenChunk -> {
                    response.append(tokenChunk);
                },
                error -> {
                    promise.fail("LLM error: " + error.getMessage());
                },
                v -> {
                    promise.complete(response.toString());
                }
        );

        return promise.future();
    }


    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");

    private String extractJsonBlock(String text) {
        if (text == null) return null;
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // Fallback for when the LLM returns raw JSON without fences
        if (text.trim().startsWith("{") && text.trim().endsWith("}")) {
            return text.trim();
        }
        return text; // Return original if no block is found, allowing parsing to attempt it
    }

    // NEW: Helper method to build a string representation of the chat history.
    private String buildChatHistory(ChatSession session) {
        if (session.getMessages().isEmpty()) {
            return "No previous conversation history.";
        }
        return session.getMessages().stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));
    }

}
