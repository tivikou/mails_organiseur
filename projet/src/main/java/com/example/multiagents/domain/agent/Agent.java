package com.example.multiagents.domain.agent;

import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import java.util.List;

/** Contrat d'un agent : il connaît son rôle et sait proposer un brouillon de réponse. */
public interface Agent {
    String id();

    String name();

    /** Rôle de l'agent en une phrase (affiché dans l'interface). */
    String description();

    /** Mots-clés exploités par les stratégies de routage. */
    List<String> keywords();

    String systemPrompt();

    AgentContext context();

    /** Propose une réponse ; ne l'envoie jamais (validation finale par l'utilisateur). */
    DraftReply draftReply(Email email, Conversation conversation);
}
