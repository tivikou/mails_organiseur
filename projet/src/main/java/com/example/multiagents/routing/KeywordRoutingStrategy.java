package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.email.Email;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Transparent baseline strategy; it can later be replaced by an LLM router. */
public class KeywordRoutingStrategy implements RoutingStrategy {
    @Override
    public RoutingDecision select(Email email, List<Agent> availableAgents) {
        String text = (email.subject() + " " + email.body()).toLowerCase(Locale.ROOT);
        Agent selected = availableAgents.stream()
                .max(Comparator.comparingInt(agent -> score(agent, text)))
                .orElseThrow(() -> new IllegalStateException("No agent is configured"));
        int score = score(selected, text);
        double confidence = score == 0 ? 0.25 : Math.min(0.95, 0.50 + score * 0.10);
        return new RoutingDecision(selected, confidence,
                "Sélection fondée sur les mots-clés du sujet et du contenu.");
    }

    private int score(Agent agent, String text) {
        String searchableAgent = (agent.name() + " " + agent.systemPrompt()).toLowerCase(Locale.ROOT);
        return java.util.Arrays.stream(searchableAgent.split("\\W+"))
                .filter(token -> token.length() > 3 && text.contains(token))
                .distinct()
                .mapToInt(token -> 1)
                .sum();
    }
}
