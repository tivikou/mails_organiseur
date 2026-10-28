package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.agent.DraftReply;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import java.util.List;

public class Router {
    private final List<Agent> agents;
    private final RoutingStrategy strategy;

    public Router(List<Agent> agents, RoutingStrategy strategy) {
        if (agents == null || agents.isEmpty()) {
            throw new IllegalArgumentException("At least one specialized agent is required");
        }
        this.agents = List.copyOf(agents);
        this.strategy = strategy;
    }

    public RoutedDraft process(Email email, Conversation conversation) {
        RoutingDecision decision = strategy.select(email, agents);
        DraftReply reply = decision.agent().draftReply(email, conversation);
        return new RoutedDraft(decision, reply);
    }

    public record RoutedDraft(RoutingDecision decision, DraftReply reply) {
    }
}
