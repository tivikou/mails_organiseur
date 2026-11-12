package com.example.multiagents.domain.agent;

import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.ConversationMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Assemble le contexte envoyé au LLM à partir du contexte de l'agent et de l'historique du fil.
 *
 * <p>Pour ne pas saturer un modèle local, on limite : le nombre de messages d'historique
 * (les plus récents) et la taille de chaque document de référence.
 */
public class PromptBuilder {
    private final int maxHistoryMessages;
    private final int maxDocumentChars;

    public PromptBuilder(int maxHistoryMessages, int maxDocumentChars) {
        if (maxHistoryMessages < 0 || maxDocumentChars < 0) {
            throw new IllegalArgumentException("limits must be positive");
        }
        this.maxHistoryMessages = maxHistoryMessages;
        this.maxDocumentChars = maxDocumentChars;
    }

    /** Construit le bloc de contexte : instructions, documents, exemples (few-shot), historique. */
    public String build(AgentContext context, Conversation conversation) {
        StringBuilder out = new StringBuilder();

        if (!context.instructions().isBlank()) {
            out.append("## Instructions\n").append(context.instructions()).append("\n\n");
        }
        for (String path : context.referenceDocuments()) {
            out.append("## Document de référence : ").append(path).append('\n')
                    .append(readTruncated(path)).append("\n\n");
        }
        if (!context.examples().isEmpty()) {
            out.append("## Exemples de réponses attendues\n");
            int i = 1;
            for (AgentContext.ExampleExchange example : context.examples()) {
                out.append("### Exemple ").append(i++).append("\nE-mail reçu :\n").append(example.email())
                        .append("\nRéponse attendue :\n").append(example.response()).append("\n\n");
            }
        }
        appendHistory(out, conversation);
        return out.toString().strip();
    }

    /** N'ajoute que les derniers messages du fil (fenêtre glissante). */
    private void appendHistory(StringBuilder out, Conversation conversation) {
        List<ConversationMessage> messages = conversation.messages();
        if (messages.isEmpty() || maxHistoryMessages == 0) {
            return;
        }
        int from = Math.max(0, messages.size() - maxHistoryMessages);
        out.append("## Historique du fil\n");
        for (ConversationMessage message : messages.subList(from, messages.size())) {
            out.append(message.role()).append(" : ").append(message.content()).append('\n');
        }
    }

    /** Lit un fichier texte local ; une erreur de lecture ne doit pas bloquer la génération. */
    private String readTruncated(String path) {
        try {
            String text = Files.readString(Path.of(path));
            return text.length() <= maxDocumentChars ? text : text.substring(0, maxDocumentChars) + "\n[...tronqué]";
        } catch (IOException | RuntimeException e) {
            return "[Document illisible : " + e.getMessage() + "]";
        }
    }
}
