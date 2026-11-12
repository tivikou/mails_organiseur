package com.example.multiagents.domain.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.ConversationMessage;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {
    @Test
    void includesExamplesAndKeepsOnlyRecentHistory() {
        Conversation conversation = new Conversation("t");
        for (int i = 1; i <= 5; i++) {
            conversation.addMessage(new ConversationMessage(ConversationMessage.Role.SENDER, "message-" + i, Instant.now()));
        }
        AgentContext context = new AgentContext("consigne", List.of(),
                List.of(new AgentContext.ExampleExchange("mail type", "réponse type")));

        String prompt = new PromptBuilder(2, 100).build(context, conversation);

        assertTrue(prompt.contains("mail type") && prompt.contains("réponse type"));
        assertTrue(prompt.contains("message-5") && prompt.contains("message-4"));
        assertFalse(prompt.contains("message-3")); // fenêtre glissante : anciens messages écartés
    }

    @Test
    void unreadableDocumentDoesNotFail() {
        AgentContext context = new AgentContext("", List.of("/chemin/inexistant.txt"), List.of());
        String prompt = new PromptBuilder(2, 100).build(context, new Conversation("t"));
        assertTrue(prompt.contains("illisible"));
    }
}
