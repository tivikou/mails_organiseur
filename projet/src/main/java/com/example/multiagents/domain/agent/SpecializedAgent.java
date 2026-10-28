package com.example.multiagents.domain.agent;

import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.infrastructure.llm.LocalLanguageModel;
import java.util.List;

/**
 * Agent générique : un seul comportement, configuré par son {@link AgentDefinition}.
 * Aucune sous-classe n'est nécessaire pour ajouter un nouvel agent.
 */
public class SpecializedAgent implements Agent {
    private final AgentDefinition definition;
    private final LocalLanguageModel languageModel;
    private final PromptBuilder promptBuilder;

    public SpecializedAgent(AgentDefinition definition, LocalLanguageModel languageModel,
                            PromptBuilder promptBuilder) {
        this.definition = definition;
        this.languageModel = languageModel;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public String id() { return definition.id(); }

    @Override
    public String name() { return definition.name(); }

    @Override
    public String description() { return definition.description(); }

    @Override
    public List<String> keywords() { return definition.keywords(); }

    @Override
    public String systemPrompt() { return definition.systemPrompt(); }

    @Override
    public AgentContext context() { return definition.context(); }

    @Override
    public DraftReply draftReply(Email email, Conversation conversation) {
        // 1. contexte propre à l'agent + historique réduit ; 2. appel au LLM local
        String contextText = promptBuilder.build(definition.context(), conversation);
        String userMessage = "De : " + email.sender() + "\nSujet : " + email.subject() + "\n\n" + email.body();
        String response = languageModel.generate(definition.systemPrompt(), contextText, userMessage);
        return new DraftReply(response, definition.id(), 0.80);
    }
}
