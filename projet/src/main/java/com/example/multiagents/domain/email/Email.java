package com.example.multiagents.domain.email;

import java.time.Instant;
import java.util.Objects;

/** Represents an email received by the application. */
public record Email(
        String sender,
        String subject,
        String body,
        Instant receivedAt) {

    public Email {
        sender = requireText(sender, "sender");
        subject = requireText(subject, "subject");
        body = requireText(body, "body");
        Objects.requireNonNull(receivedAt, "receivedAt must not be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
