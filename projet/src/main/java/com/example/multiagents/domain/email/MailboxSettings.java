package com.example.multiagents.domain.email;

/**
 * Paramètres de connexion à une boîte mail. Le mot de passe n'en fait volontairement pas partie :
 * il n'est jamais écrit sur disque (saisi à chaque session).
 *
 * @param host     serveur IMAP (ex. imap.gmail.com)
 * @param port     port IMAP (993 en SSL)
 * @param username identifiant, en général l'adresse e-mail
 * @param ssl      connexion chiffrée
 * @param folder   dossier à lire (ex. INBOX)
 */
public record MailboxSettings(String host, int port, String username, boolean ssl, String folder) {

    public MailboxSettings {
        host = host == null ? "" : host.trim();
        username = username == null ? "" : username.trim();
        folder = folder == null || folder.isBlank() ? "INBOX" : folder.trim();
    }

    /** Valeurs proposées tant que l'utilisateur n'a rien configuré. */
    public static MailboxSettings empty() {
        return new MailboxSettings("", 993, "", true, "INBOX");
    }

    public boolean isConfigured() {
        return !host.isEmpty() && !username.isEmpty();
    }
}
