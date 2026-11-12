package com.example.multiagents.infrastructure.llm;

/** Erreur lors de l'appel au LLM local (serveur arrêté, modèle absent, délai dépassé...). */
public class LanguageModelException extends RuntimeException {
    public LanguageModelException(String message, Throwable cause) {
        super(message, cause);
    }

    public LanguageModelException(String message) {
        super(message);
    }
}
