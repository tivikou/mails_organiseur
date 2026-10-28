package com.example.multiagents.infrastructure.llm;

import com.example.multiagents.application.AppConfig;

/** Choisit l'implémentation du LLM selon la configuration (point d'extension pour d'autres moteurs). */
public final class LanguageModelFactory {
    private LanguageModelFactory() {
    }

    public static LocalLanguageModel create(AppConfig config) {
        return switch (config.llmProvider().toLowerCase()) {
            case "ollama" -> new OllamaLanguageModel(config.ollamaUrl(), config.ollamaModel(), config.llmTimeout());
            case "demo" -> new DemoLocalLanguageModel();
            default -> throw new IllegalArgumentException("llm.provider inconnu : " + config.llmProvider());
        };
    }
}
