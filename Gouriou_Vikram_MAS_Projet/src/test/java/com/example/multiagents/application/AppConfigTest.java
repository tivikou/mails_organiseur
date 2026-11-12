package com.example.multiagents.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.multiagents.domain.email.MailboxSettings;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {
    @Test
    void savedLlmAndMailboxSettingsAreReloaded(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("config/application.properties");
        AppConfig config = AppConfig.load(file) // fichier absent : valeurs par défaut
                .withLlm("ollama", "http://localhost:11434", "mistral-é")
                .withMailbox(new MailboxSettings("imap.example.org", 993, "moi@example.org", true, "INBOX"));

        config.save(file);
        AppConfig reloaded = AppConfig.load(file);

        assertEquals("mistral-é", reloaded.ollamaModel()); // accents conservés (UTF-8)
        assertEquals(config.mailbox(), reloaded.mailbox());
        assertFalse(Files.readString(file).toLowerCase().contains("password")); // aucun mot de passe écrit
    }
}
