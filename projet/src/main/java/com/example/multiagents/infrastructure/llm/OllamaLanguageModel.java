package com.example.multiagents.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter vers un serveur Ollama local (API /api/chat). Aucune donnée ne quitte la machine
 * tant que l'URL pointe sur localhost.
 */
public class OllamaLanguageModel implements LocalLanguageModel {
    /** Température basse : réponses d'e-mail plus sobres et reproductibles. */
    private static final double TEMPERATURE = 0.3;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final String model;
    private final Duration timeout;

    public OllamaLanguageModel(String baseUrl, String model, Duration timeout) {
        this.baseUrl = baseUrl.trim().replaceAll("/+$", "");
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
                "options", Map.of("temperature", TEMPERATURE),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", userMessage)));
        JsonNode json = send(HttpRequest.newBuilder(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload))));
        JsonNode content = json.path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new LanguageModelException("Réponse vide du modèle '" + model + "'.");
        }
        return content.asText().strip();
    }

    /** Liste les modèles installés sur le serveur (ceux visibles avec « ollama list »). */
    public List<String> listModels() {
        JsonNode json = send(HttpRequest.newBuilder(URI.create(baseUrl + "/api/tags")).GET());
        List<String> names = new ArrayList<>();
        json.path("models").forEach(node -> names.add(node.path("name").asText()));
        return names;
    }

    /** Envoie la requête et renvoie le JSON de la réponse ; toute erreur devient une LanguageModelException. */
    private JsonNode send(HttpRequest.Builder request) {
        try {
            HttpResponse<String> response = http.send(request.timeout(timeout).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new LanguageModelException("Ollama a répondu " + response.statusCode() + " : " + response.body());
            }
            return mapper.readTree(response.body());
        } catch (IOException e) {
            throw new LanguageModelException("Ollama injoignable sur " + baseUrl + " : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LanguageModelException("Appel au modèle interrompu.", e);
        }
    }

    private String toJson(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (IOException e) {
            throw new LanguageModelException("Requête impossible à sérialiser.", e);
        }
    }
}
