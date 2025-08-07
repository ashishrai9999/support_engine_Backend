# Cohere Embedding Client

This module provides a self-contained Java client for generating text embeddings using the Cohere API.

## Features
- No dependency on the main MCP server codebase
- Simple, single-class usage
- Example usage included in `main()`

## Setup

1. **Cohere API Key**
   - Sign up at [Cohere](https://cohere.com/) and get your API key.
   - Set your API key as an environment variable:
     ```sh
     export COHERE_API_KEY=your_api_key_here
     ```

2. **Dependencies**
   - Requires the following libraries:
     - `org.json:json:20231013` (or similar)
   - If using Gradle, add to your `build.gradle`:
     ```groovy
     implementation 'org.json:json:20231013'
     ```

3. **Directory Structure**
   ```
   cohereEmbedding/
     ├── CohereEmbeddingClient.java
     └── README.md
   ```

## Usage

```java
String apiKey = System.getenv("COHERE_API_KEY");
CohereEmbeddingClient client = new CohereEmbeddingClient(apiKey);
List<String> texts = List.of("Hello world", "Cohere embeddings are easy!");
List<List<Double>> embeddings = client.getEmbeddings(texts);
System.out.println(embeddings.get(0));
```

Or run the `main()` method in `CohereEmbeddingClient.java` for a demo.

## Notes
- This client is synchronous and for demonstration/testing purposes.
- For production, consider adding retries, async support, and better error handling. 