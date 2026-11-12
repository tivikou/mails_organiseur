package com.example.multiagents.domain.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.infrastructure.llm.ConfigurableLanguageModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpecializedAgentTest {
    @Test
    void agentSendsWritingRulesAndUserPromptToTheCurrentModel() {
        List<String> receivedSystemPrompts = new ArrayList<>();
        ConfigurableLanguageModel model = new ConfigurableLanguageModel((system, context, user) -> {
            receivedSystemPrompts.add(system);
            return "premier modèle";
        });
        Agent agent = new AgentFactory(model, new PromptBuilder(5, 100))
                .create(new AgentDefinition("a", "A", "", List.of(), "Ton chaleureux.", null));
        Email email = new Email("x@u.fr", "Sujet", "Corps", Instant.now());

        assertEquals("premier modèle", agent.draftReply(email, new Conversation("t")).content());
        assertTrue(receivedSystemPrompts.get(0).contains(SpecializedAgent.WRITING_RULES));
        assertTrue(receivedSystemPrompts.get(0).endsWith("Ton chaleureux."));

        // changement de modèle à chaud : le même agent utilise aussitôt le nouveau
        model.switchTo((system, context, user) -> "second modèle");
        assertEquals("second modèle", agent.draftReply(email, new Conversation("t")).content());
    }
}
