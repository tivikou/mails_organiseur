package com.example.multiagents.application;

import com.example.multiagents.domain.email.MailboxSettings;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuration de l'application, lue et écrite dans {@code config/application.properties}.
 * Chaque clé a une valeur par défaut : l'application démarre sans aucun fichier.
 * Le mot de passe de la boîte mail n'est jamais stocké ici.
 */
public record AppConfig(
        String llmProvider,          // "ollama" (défaut) ou "demo"
        String ollamaUrl,
        String ollamaModel,
        Duration llmTimeout,
        Path agentsFile,
        double routingMinConfidence,
        String fallbackAgentId,
        int maxHistoryMessages,
        int maxDocumentChars,
        MailboxSettings mailbox) {

    public static AppConfig load(Path file) {
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                p.load(in);
            } catch (IOException e) {
                throw new UncheckedIOException("Lecture impossible de " + file, e);
            }
        }
        MailboxSettings mailbox = new MailboxSettings(
                p.getProperty("mail.imap.host", ""),
                Integer.parseInt(p.getProperty("mail.imap.port", "993")),
                p.getProperty("mail.imap.username", ""),
                Boolean.parseBoolean(p.getProperty("mail.imap.ssl", "true")),
                p.getProperty("mail.imap.folder", "INBOX"));
        return new AppConfig(
                p.getProperty("llm.provider", "ollama"),
                p.getProperty("llm.ollama.url", "http://localhost:11434"),
                p.getProperty("llm.ollama.model", "llama3.2"),
                Duration.ofSeconds(Long.parseLong(p.getProperty("llm.timeoutSeconds", "120"))),
                Path.of(p.getProperty("agents.file", "data/agents.json")),
                Double.parseDouble(p.getProperty("routing.minConfidence", "0.5")),
                p.getProperty("routing.fallbackAgentId", "generic"),
                Integer.parseInt(p.getProperty("memory.maxHistoryMessages", "6")),
                Integer.parseInt(p.getProperty("memory.maxDocumentChars", "4000")),
                mailbox);
    }

    /** Copie avec un autre réglage du LLM (la configuration est immuable). */
    public AppConfig withLlm(String provider, String url, String model) {
        return new AppConfig(provider, url, model, llmTimeout, agentsFile, routingMinConfidence,
                fallbackAgentId, maxHistoryMessages, maxDocumentChars, mailbox);
    }

    /** Copie avec d'autres paramètres de boîte mail. */
    public AppConfig withMailbox(MailboxSettings newMailbox) {
        return new AppConfig(llmProvider, ollamaUrl, ollamaModel, llmTimeout, agentsFile, routingMinConfidence,
                fallbackAgentId, maxHistoryMessages, maxDocumentChars, newMailbox);
    }

    /** Écrit la configuration complète (UTF-8). */
    public void save(Path file) {
        Properties p = new Properties();
        p.setProperty("llm.provider", llmProvider);
        p.setProperty("llm.ollama.url", ollamaUrl);
        p.setProperty("llm.ollama.model", ollamaModel);
        p.setProperty("llm.timeoutSeconds", String.valueOf(llmTimeout.toSeconds()));
        p.setProperty("agents.file", agentsFile.toString());
        p.setProperty("routing.minConfidence", String.valueOf(routingMinConfidence));
        p.setProperty("routing.fallbackAgentId", fallbackAgentId);
        p.setProperty("memory.maxHistoryMessages", String.valueOf(maxHistoryMessages));
        p.setProperty("memory.maxDocumentChars", String.valueOf(maxDocumentChars));
        p.setProperty("mail.imap.host", mailbox.host());
        p.setProperty("mail.imap.port", String.valueOf(mailbox.port()));
        p.setProperty("mail.imap.username", mailbox.username());
        p.setProperty("mail.imap.ssl", String.valueOf(mailbox.ssl()));
        p.setProperty("mail.imap.folder", mailbox.folder());
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                p.store(out, "Local Email Agents - configuration (aucun mot de passe stocké)");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture impossible de " + file, e);
        }
    }
}
