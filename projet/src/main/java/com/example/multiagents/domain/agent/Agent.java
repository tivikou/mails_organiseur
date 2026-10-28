package com.example.multiagents.domain.agent;

import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;

public interface Agent {
    String id();
    String name();
    String systemPrompt();
    AgentContext context();
    DraftReply draftReply(Email email, Conversation conversation);
}
