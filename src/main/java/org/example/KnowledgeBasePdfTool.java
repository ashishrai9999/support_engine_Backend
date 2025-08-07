package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KnowledgeBasePdfTool {
    private static final int CHUNK_SIZE = 500; // characters per chunk
    private final List<String> chunks = new ArrayList<>();
    private final List<float[]> embeddings = new ArrayList<>();
    private final GeminiEmbeddingClient embeddingClient;

    public KnowledgeBasePdfTool(GeminiEmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    public void loadAndEmbedPdf(String pdfPath) throws IOException {
        chunks.clear();
        embeddings.clear();
        String text = extractTextFromPdf(pdfPath);
        System.out.println("text is " + text);
        List<String> chunkList = chunkText(text, CHUNK_SIZE);
        chunks.addAll(chunkList);
        for (String chunk : chunkList) {
            float[] emb = embedText(chunk);
            System.out.println("Chunks areeeeeee: " + chunk);
            embeddings.add(emb);
        }
    }

    public String query(String userQuery) {
        float[] queryEmbedding = embedText(userQuery);
        int bestIdx = -1;
        double bestScore = -1;
        for (int i = 0; i < embeddings.size(); i++) {
            double score = cosineSimilarity(queryEmbedding, embeddings.get(i));
            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }
        if (bestIdx >= 0) {
            return chunks.get(bestIdx);
        } else {
            return "No relevant answer found.";
        }
    }

    private String extractTextFromPdf(String pdfPath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private List<String> chunkText(String text, int chunkSize) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            result.add(text.substring(start, end));
            start = end;
        }
        return result;
    }

    private float[] embedText(String text) {
        final float[][] resultHolder = new float[1][1];
        final boolean[] done = {false};
        embeddingClient.generateEmbedding(text, ar -> {
            if (ar.succeeded()) {
                Float[] arr = ar.result();
                float[] fArr = new float[arr.length];
                for (int i = 0; i < arr.length; i++) fArr[i] = arr[i];
                resultHolder[0] = fArr;
            } else {
                resultHolder[0] = new float[0];
            }
            done[0] = true;
        });
        int tries = 0;
        while (!done[0] && tries < 100) {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            tries++;
        }
        return resultHolder[0];
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return -1;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-8);
    }
} 