package com.example.multiagents.domain.agent;

import java.util.List;

/** Port de persistance des définitions d'agents (implémenté dans la couche infrastructure). */
public interface AgentRepository {
    List<AgentDefinition> loadAll();

    void saveAll(List<AgentDefinition> definitions);
}
