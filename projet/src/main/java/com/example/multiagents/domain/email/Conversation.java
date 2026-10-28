package com.example.multiagents.domain.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores the relevant history of one email thread. */
public class Conversation {
    private final String id;
    private final List<ConversationMessage> messages = new ArrayList<>();

    public Conversation(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.id = id;
    }

    public String id() {
        return id;
    }

    public void addMessage(ConversationMessage message) {
        messages.add(message);
    }

    public List<ConversationMessage> messages() {
        return Collections.unmodifiableList(messages);
    }
}
