package com.example.multiagents.domain.agent;

import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.infrastructure.llm.LocalLanguageModel;
import java.util.stream.Collectors;

/** Reusable agent implementation configured by prompt and context. */
public class SpecializedAgent implements Agent {
    private final String id;
    private final String name;
    private final String systemPrompt;
    private final AgentContext context;
    private final LocalLanguageModel languageModel;

    public SpecializedAgent(String id, String name, String systemPrompt,
                            AgentContext context, LocalLanguageModel languageModel) {
        this.id = id;
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.context = context;
        this.languageModel = languageModel;
    }

    @Override
    public String id() { return id; }

    @Override
    public String name() { return name; }

    @Override
    public String systemPrompt() { return systemPrompt; }

    @Override
    public AgentContext context() { return context; }

    @Override
    public DraftReply draftReply(Email email, Conversation conversation) {
        String history = conversation.messages().stream()
                .map(message -> message.role() + ": " + message.content())
                .collect(Collectors.joining("\n"));
        String contextText = context.instructions() + "\nHistorique:\n" + history;
        String response = languageModel.generate(systemPrompt, contextText, email.body());
        return new DraftReply(response, id, 0.80);
    }
}
