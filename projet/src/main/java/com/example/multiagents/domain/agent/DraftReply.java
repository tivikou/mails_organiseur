package com.example.multiagents.domain.agent;

public record DraftReply(String content, String agentId, double confidence) {
    public DraftReply {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        }
    }
}
