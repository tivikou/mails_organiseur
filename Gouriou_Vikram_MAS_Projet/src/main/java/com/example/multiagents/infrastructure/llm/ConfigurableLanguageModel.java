package com.example.multiagents.infrastructure.llm;

/**
 * Proxy commutable : les agents gardent toujours la même référence, et l'on change de moteur
 * ou de modèle à chaud (depuis l'onglet « Modèle LLM ») sans recréer les agents.
 */
public class ConfigurableLanguageModel implements LocalLanguageModel {
    private volatile LocalLanguageModel delegate;

    public ConfigurableLanguageModel(LocalLanguageModel initial) {
        this.delegate = initial;
    }

    public void switchTo(LocalLanguageModel next) {
        this.delegate = next;
    }

    @Override
    public String generate(String systemPrompt, String context, String userMessage) {
        return delegate.generate(systemPrompt, context, userMessage);
    }
}
