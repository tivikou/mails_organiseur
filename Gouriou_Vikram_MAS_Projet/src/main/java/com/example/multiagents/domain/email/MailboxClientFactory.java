package com.example.multiagents.domain.email;

/** Crée un client de boîte mail à partir des paramètres saisis et du mot de passe (en mémoire seulement). */
@FunctionalInterface
public interface MailboxClientFactory {
    MailboxClient create(MailboxSettings settings, char[] password);
}
