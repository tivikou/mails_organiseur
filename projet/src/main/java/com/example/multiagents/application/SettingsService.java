package com.example.multiagents.application;

import com.example.multiagents.domain.email.MailboxSettings;
import com.example.multiagents.infrastructure.llm.ConfigurableLanguageModel;
import com.example.multiagents.infrastructure.llm.LanguageModelFactory;
import java.nio.file.Path;

/**
 * Applique et persiste les réglages modifiés depuis l'interface : le LLM est changé à chaud,
 * la configuration est réécrite dans le fichier.
 */
public class SettingsService {
    private final Path file;
    private final ConfigurableLanguageModel languageModel;
    private AppConfig config;

    public SettingsService(Path file, AppConfig config, ConfigurableLanguageModel languageModel) {
        this.file = file;
        this.config = config;
        this.languageModel = languageModel;
    }

    public synchronized AppConfig current() {
        return config;
    }

    /** Enregistre puis applique immédiatement le nouveau moteur / modèle. */
    public synchronized void updateLlm(String provider, String url, String model) {
        AppConfig updated = config.withLlm(provider, url, model);
        languageModel.switchTo(LanguageModelFactory.create(updated)); // échoue avant toute écriture si invalide
        config = updated;
        config.save(file);
    }

    public synchronized void updateMailbox(MailboxSettings mailbox) {
        config = config.withMailbox(mailbox);
        config.save(file);
    }
}
