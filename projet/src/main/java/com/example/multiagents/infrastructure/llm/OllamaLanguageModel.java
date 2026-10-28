package com.example.multiagents.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Adapter vers un serveur Ollama local (API /api/chat). Aucune donnée ne quitte la machine
 * tant que l'URL pointe sur localhost.
 */
public class OllamaLanguageModel implements LocalLanguageModel {
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI chatUri;
    private final String model;
    private final Duration timeout;

    public OllamaLanguageModel(String baseUrl, String model, Duration timeout) {
        this.chatUri = URI.create(baseUrl.replaceAll("/+$", "") + "/api/chat");
        this.model = model;
        this.timeout = timeout;
    }

    @Override
    public String generate(String systemPrompt, String context, String userMessage) {
        // Le contexte est joint au system prompt ; l'e-mail est le message utilisateur.
        String system = context.isBlank() ? systemPrompt : systemPrompt + "\n\n" + context;
        Map<String, Object> payload = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", userMessage)));
        try {
            HttpRequest request = HttpRequest.newBuilder(chatUri)
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new LanguageModelException("Ollama a répondu " + response.statusCode() + " : " + response.body());
            }
            JsonNode content = mapper.readTree(response.body()).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new LanguageModelException("Réponse vide du modèle '" + model + "'.");
            }
            return content.asText().strip();
        } catch (IOException e) {
            throw new LanguageModelException("Ollama injoignable sur " + chatUri + " : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LanguageModelException("Appel au modèle interrompu.", e);
        }
    }
}
