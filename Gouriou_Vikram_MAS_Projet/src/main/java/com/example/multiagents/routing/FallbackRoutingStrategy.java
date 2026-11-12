package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.email.Email;
import java.util.List;

/**
 * Décorateur : si la stratégie enveloppée n'est pas assez sûre, on bascule sur l'agent de repli
 * (typiquement l'agent générique). Répond au cas « aucun agent suffisamment adapté ».
 */
public class FallbackRoutingStrategy implements RoutingStrategy {
    private final RoutingStrategy delegate;
    private final String fallbackAgentId;
    private final double minConfidence;

    public FallbackRoutingStrategy(RoutingStrategy delegate, String fallbackAgentId, double minConfidence) {
        this.delegate = delegate;
        this.fallbackAgentId = fallbackAgentId;
        this.minConfidence = minConfidence;
    }

    @Override
    public RoutingDecision select(Email email, List<Agent> availableAgents) {
        RoutingDecision decision = delegate.select(email, availableAgents);
        if (decision.confidence() >= minConfidence) {
            return decision;
        }
        return availableAgents.stream()
                .filter(agent -> agent.id().equals(fallbackAgentId))
                .findFirst()
                .map(agent -> new RoutingDecision(agent, decision.confidence(),
                        "Confiance trop faible (" + decision.explanation() + ") : repli sur " + agent.name() + "."))
                .orElse(decision); // pas d'agent de repli : on garde le meilleur candidat
    }
}
