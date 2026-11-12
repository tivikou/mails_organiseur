package com.example.multiagents.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.agent.DraftReply;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.ConversationStore;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.routing.RoutingDecision;
import com.example.multiagents.routing.Router;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MailPanelTest {
    @Test
    void loadingSameQueueDoesNotEmptyIt() throws Exception {
        Router router = new Router(null, null) {
            @Override
            public RoutedDraft process(Email email, Conversation conversation) {
                Agent agent = new Agent() {
                    @Override public String id() { return "agent-test"; }
                    @Override public String name() { return "Agent test"; }
                    @Override public String description() { return "Test"; }
                    @Override public List<String> keywords() { return List.of(); }
                    @Override public String systemPrompt() { return ""; }
                    @Override public com.example.multiagents.domain.context.AgentContext context() { return null; }
                    @Override public DraftReply draftReply(Email mail, Conversation conv) { return new DraftReply("Réponse test", id(), 1.0); }
                };
                return new RoutedDraft(new RoutingDecision(agent, 1.0, "test"), new DraftReply("Réponse test", agent.id(), 1.0));
            }
        };

        MailPanel panel = new MailPanel(router, new ConversationStore());
        List<Email> queue = new ArrayList<>(List.of(
                new Email("a@test.com", "Sujet 1", "Body 1", Instant.now()),
                new Email("b@test.com", "Sujet 2", "Body 2", Instant.now())
        ));

        setField(panel, "currentMailQueue", queue);
        setField(panel, "currentMailIndex", 0);

        panel.loadEmail(queue.get(0), null, null, queue, 0);

        assertEquals(2, queue.size());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
