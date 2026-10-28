package com.example.multiagents.infrastructure.persistence;

import com.example.multiagents.domain.agent.AgentDefinition;
import com.example.multiagents.domain.agent.AgentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Persiste les agents dans un fichier JSON local, lisible et modifiable à la main. */
public class JsonAgentRepository implements AgentRepository {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path file;

    public JsonAgentRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<AgentDefinition> loadAll() {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return mapper.readValue(file.toFile(), new TypeReference<List<AgentDefinition>>() { });
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture impossible de " + file, e);
        }
    }

    @Override
    public void saveAll(List<AgentDefinition> definitions) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            mapper.writeValue(file.toFile(), definitions);
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture impossible de " + file, e);
        }
    }
}
