package com.example.multiagents.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuration de l'application, lue dans {@code config/application.properties} (optionnel).
 * Chaque clé a une valeur par défaut : l'application démarre sans aucun fichier.
 */
public record AppConfig(
        String llmProvider,          // "demo" ou "ollama"
        String ollamaUrl,
        String ollamaModel,
        Duration llmTimeout,
        Path agentsFile,
        double routingMinConfidence,
        String fallbackAgentId,
        int maxHistoryMessages,
        int maxDocumentChars) {

    public static AppConfig load(Path file) {
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException("Lecture impossible de " + file, e);
            }
        }
        return new AppConfig(
                p.getProperty("llm.provider", "demo"),
                p.getProperty("llm.ollama.url", "http://localhost:11434"),
                p.getProperty("llm.ollama.model", "llama3.2"),
                Duration.ofSeconds(Long.parseLong(p.getProperty("llm.timeoutSeconds", "120"))),
                Path.of(p.getProperty("agents.file", "data/agents.json")),
                Double.parseDouble(p.getProperty("routing.minConfidence", "0.5")),
                p.getProperty("routing.fallbackAgentId", "generic"),
                Integer.parseInt(p.getProperty("memory.maxHistoryMessages", "6")),
                Integer.parseInt(p.getProperty("memory.maxDocumentChars", "4000")));
    }
}
