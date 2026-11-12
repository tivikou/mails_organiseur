package com.example.multiagents.domain.email;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Mémoire des fils de discussion (en mémoire pour la v1 ; remplaçable par une version persistante). */
public class ConversationStore {
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    /** Retourne le fil de l'e-mail, en le créant au besoin. */
    public Conversation forEmail(Email email) {
        return conversations.computeIfAbsent(threadIdOf(email), Conversation::new);
    }

    /**
     * Identifiant de fil = expéditeur + sujet sans préfixes "Re:" / "Fwd:".
     * Heuristique simple, suffisante sans en-têtes Message-ID/In-Reply-To.
     */
    static String threadIdOf(Email email) {
        String subject = email.subject().toLowerCase(Locale.ROOT)
                .replaceAll("^(\\s*(re|fwd?|tr)\\s*:\\s*)+", "").trim();
        return email.sender().toLowerCase(Locale.ROOT) + "|" + subject;
    }
}
