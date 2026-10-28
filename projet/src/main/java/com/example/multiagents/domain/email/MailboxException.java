package com.example.multiagents.domain.email;

/** Erreur d'accès à la boîte mail (serveur injoignable, identifiants refusés, dossier absent...). */
public class MailboxException extends RuntimeException {
    public MailboxException(String message, Throwable cause) {
        super(message, cause);
    }
}
