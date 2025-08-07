package org.example.cohereEmbedding;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class CohereEmbeddingClient {
    private final String apiKey;
    private final String endpoint;

    public CohereEmbeddingClient(String apiKey) {
        this.apiKey = apiKey;
        this.endpoint = "https://api.cohere.ai/v1/embed";
    }

    /**
     * Generates embeddings for a list of texts using Cohere's API.
     * @param texts List of input texts
     * @return List of embeddings (each embedding is a List<Double>)
     * @throws IOException if network or API error occurs
     */
    public  List<List<Double>> getEmbeddings(List<String> texts) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("texts", new JSONArray(texts));
        payload.put("model", "embed-v4.0"); // You can change model as needed

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = new Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A").next();
            throw new IOException("Cohere API error: " + error);
        }

        String response = new Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A").next();
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray embeddingsArray = jsonResponse.getJSONArray("embeddings");
        List<List<Double>> embeddings = new ArrayList<>();
        for (int i = 0; i < embeddingsArray.length(); i++) {
            JSONArray arr = embeddingsArray.getJSONArray(i);
            List<Double> emb = new ArrayList<>();
            for (int j = 0; j < arr.length(); j++) {
                emb.add(arr.getDouble(j));
            }
            embeddings.add(emb);
        }
        return embeddings;
    }

    // Example usage
    public static void main(String[] args) throws IOException {
        String apiKey ="0P2TxtY7PB22EzYRBv1HR7ooOKPIXgnRRJk3Jkzg";// System.getenv("COHERE_API_KEY"); // Set your Cohere API key as an environment variable
        if (apiKey == null) {
            System.err.println("Please set the COHERE_API_KEY environment variable.");
            return;
        }
        CohereEmbeddingClient client = new CohereEmbeddingClient(apiKey);
        List<String> texts = List.of(
                "The quick brown fox jumps over the lazy dog.",
                "Retrieval-augmented generation is powerful."
        );
        List<List<Double>> embeddings = client.getEmbeddings(texts);
        for (int i = 0; i < texts.size(); i++) {
            System.out.println("Text: " + texts.get(i));
            System.out.println("Embedding: " + embeddings.get(i).subList(0, 5) + " ..."); // Print first 5 dims
        }
    }
} 