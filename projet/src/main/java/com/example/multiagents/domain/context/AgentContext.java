package com.example.multiagents.domain.context;

import java.util.List;

/** Context owned by one specialized agent. */
public record AgentContext(
        String instructions,
        List<String> referenceDocuments,
        List<ExampleExchange> examples) {

    public AgentContext {
        instructions = instructions == null ? "" : instructions;
        referenceDocuments = List.copyOf(referenceDocuments == null ? List.of() : referenceDocuments);
        examples = List.copyOf(examples == null ? List.of() : examples);
    }

    public record ExampleExchange(String email, String response) {
    }
}
