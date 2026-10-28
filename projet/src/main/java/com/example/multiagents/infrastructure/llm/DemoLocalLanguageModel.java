package com.example.multiagents.infrastructure.llm;

/** Deterministic implementation used to run the application without a server. */
public class DemoLocalLanguageModel implements LocalLanguageModel {
    @Override
    public String generate(String systemPrompt, String context, String userMessage) {
        return "[Mode demonstration] Merci pour votre message. "
                + "Une réponse adaptée sera générée ici par le modèle local configuré.\n\n"
                + "Message reçu : " + userMessage;
    }
}
