package com.example.multiagents.domain.agent;

import com.example.multiagents.infrastructure.llm.LocalLanguageModel;

/** Fabrique d'agents : injecte le LLM et le constructeur de prompt dans chaque agent créé. */
public class AgentFactory {
    private final LocalLanguageModel languageModel;
    private final PromptBuilder promptBuilder;

    public AgentFactory(LocalLanguageModel languageModel, PromptBuilder promptBuilder) {
        this.languageModel = languageModel;
        this.promptBuilder = promptBuilder;
    }

    public Agent create(AgentDefinition definition) {
        return new SpecializedAgent(definition, languageModel, promptBuilder);
    }
}
