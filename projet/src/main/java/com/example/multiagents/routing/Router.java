package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.agent.AgentRegistry;
import com.example.multiagents.domain.agent.DraftReply;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import java.util.List;

/**
 * Agent routeur : choisit l'agent spécialisé (via une {@link RoutingStrategy}) puis lui délègue
 * la rédaction. Il lit le {@link AgentRegistry} à chaque appel, donc les agents créés ou modifiés
 * dans l'interface sont pris en compte immédiatement.
 */
public class Router {
    private final AgentRegistry registry;
    private final RoutingStrategy strategy;

    public Router(AgentRegistry registry, RoutingStrategy strategy) {
        this.registry = registry;
        this.strategy = strategy;
    }

    public RoutedDraft process(Email email, Conversation conversation) {
        List<Agent> agents = registry.agents();
        if (agents.isEmpty()) {
            throw new IllegalStateException("Aucun agent n'est configuré : créez-en un dans l'onglet Agents.");
        }
        RoutingDecision decision = strategy.select(email, agents);
        DraftReply reply = decision.agent().draftReply(email, conversation);
        return new RoutedDraft(decision, reply);
    }

    /** Résultat complet : qui a été choisi, pourquoi, et quel brouillon a été produit. */
    public record RoutedDraft(RoutingDecision decision, DraftReply reply) {
    }
}
