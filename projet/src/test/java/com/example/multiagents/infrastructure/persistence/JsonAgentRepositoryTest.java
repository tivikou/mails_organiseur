package com.example.multiagents.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.multiagents.domain.agent.AgentDefinition;
import com.example.multiagents.domain.context.AgentContext;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonAgentRepositoryTest {
    @Test
    void savedAgentsAreReloadedIdentically(@TempDir Path dir) {
        JsonAgentRepository repository = new JsonAgentRepository(dir.resolve("agents.json"));
        AgentDefinition agent = new AgentDefinition("student", "StudentAgent", "desc", List.of("cours"), "prompt",
                new AgentContext("instr", List.of("/tmp/a.txt"),
                        List.of(new AgentContext.ExampleExchange("mail", "réponse"))));

        repository.saveAll(List.of(agent));

        assertEquals(List.of(agent), repository.loadAll());
    }
}
