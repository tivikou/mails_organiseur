package com.example.multiagents.infrastructure.llm;

/**
 * Abstraction d'un LLM exécuté localement. Le reste de l'application ne dépend que de cette
 * interface : changer de modèle ou de moteur = fournir une autre implémentation.
 */
public interface LocalLanguageModel {
    /**
     * @param systemPrompt consignes de l'agent
     * @param context      contexte assemblé (instructions, exemples, historique)
     * @param userMessage  e-mail à traiter
     * @throws LanguageModelException si le moteur local est injoignable ou répond en erreur
     */
    String generate(String systemPrompt, String context, String userMessage);
}
