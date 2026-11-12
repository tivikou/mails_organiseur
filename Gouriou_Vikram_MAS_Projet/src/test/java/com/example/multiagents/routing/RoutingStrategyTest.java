package com.example.multiagents.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.agent.AgentDefinition;
import com.example.multiagents.domain.agent.AgentFactory;
import com.example.multiagents.domain.agent.PromptBuilder;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.infrastructure.llm.DemoLocalLanguageModel;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoutingStrategyTest {
    private final AgentFactory factory = new AgentFactory(new DemoLocalLanguageModel(), new PromptBuilder(5, 1000));
    private final List<Agent> agents = List.of(
            factory.create(new AgentDefinition("student", "StudentAgent", "", List.of("cours", "devoir"), "p", null)),
            factory.create(new AgentDefinition("admin", "AdminAgent", "", List.of("dossier", "inscription"), "p", null)),
            factory.create(new AgentDefinition("generic", "GenericAgent", "", List.of(), "p", null)));

    private Email email(String subject, String body) {
        return new Email("someone@univ.example", subject, body, Instant.now());
    }

    @Test
    void keywordsSelectTheMatchingAgent() {
        RoutingDecision d = new KeywordRoutingStrategy().select(email("Question sur le devoir", "Le cours est-il annulé ?"), agents);
        assertEquals("student", d.agent().id());
    }

    @Test
    void lowConfidenceFallsBackToGenericAgent() {
        RoutingStrategy strategy = new FallbackRoutingStrategy(new KeywordRoutingStrategy(), "generic", 0.5);
        RoutingDecision d = strategy.select(email("Bonjour", "Une question sans rapport."), agents);
        assertEquals("generic", d.agent().id());
    }

    @Test
    void ambiguousEmailLowersConfidence() {
        RoutingDecision d = new KeywordRoutingStrategy().select(email("cours et dossier", "x"), agents);
        // deux agents à égalité : la confiance doit rester sous celle d'un cas non ambigu (0.60)
        assertEquals(true, d.confidence() < 0.60);
    }
}
