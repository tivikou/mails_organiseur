package com.example.multiagents.routing;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.email.Email;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Stratégie de base, transparente : score = occurrences des mots-clés de l'agent.
 * Un mot-clé dans le sujet ou l'adresse de l'expéditeur pèse plus que dans le corps.
 * Peut être remplacée plus tard par un routeur basé sur le LLM sans changer le {@link Router}.
 */
public class KeywordRoutingStrategy implements RoutingStrategy {
    private static final int WEIGHT_SUBJECT_OR_SENDER = 2;
    private static final int WEIGHT_BODY = 1;

    @Override
    public RoutingDecision select(Email email, List<Agent> availableAgents) {
        Agent best = availableAgents.stream()
                .max(Comparator.comparingInt(agent -> score(agent, email)))
                .orElseThrow(() -> new IllegalStateException("No agent is configured"));
        int score = score(best, email);
        // Plusieurs agents à égalité de score : cas ambigu, signalé dans l'explication.
        long ties = availableAgents.stream().filter(agent -> score(agent, email) == score).count();

        double confidence = score == 0 ? 0.0 : Math.min(0.95, 0.40 + score * 0.10);
        if (ties > 1 && score > 0) {
            confidence = Math.max(0.0, confidence - 0.20);
        }
        String explanation = score == 0
                ? "Aucun mot-clé reconnu."
                : "Score " + score + " sur les mots-clés de l'agent" + (ties > 1 ? " (ex æquo avec d'autres agents)." : ".");
        return new RoutingDecision(best, confidence, explanation);
    }

    private int score(Agent agent, Email email) {
        String sender = email.sender().toLowerCase(Locale.ROOT);
        String subject = email.subject().toLowerCase(Locale.ROOT);
        String body = email.body().toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : agent.keywords()) {
            String k = keyword.toLowerCase(Locale.ROOT).trim();
            if (k.isEmpty()) {
                continue;
            }
            if (subject.contains(k) || sender.contains(k)) {
                score += WEIGHT_SUBJECT_OR_SENDER;
            }
            if (body.contains(k)) {
                score += WEIGHT_BODY;
            }
        }
        return score;
    }
}
