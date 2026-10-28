package com.example.multiagents.infrastructure.llm;

public interface LocalLanguageModel {
    String generate(String systemPrompt, String context, String userMessage);
}
