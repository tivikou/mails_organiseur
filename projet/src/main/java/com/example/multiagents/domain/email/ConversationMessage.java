package com.example.multiagents.domain.email;

import java.time.Instant;
import java.util.Objects;

public record ConversationMessage(Role role, String content, Instant createdAt) {

    public ConversationMessage {
        Objects.requireNonNull(role, "role must not be null");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public enum Role {
        USER,
        SENDER,
        ASSISTANT
    }
}
