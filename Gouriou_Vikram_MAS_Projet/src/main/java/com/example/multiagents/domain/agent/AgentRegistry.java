package com.example.multiagents.domain.agent;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registre des agents disponibles. Point central pour ajouter, modifier ou supprimer un agent.
 * Chaque modification est persistée et notifie les observateurs (ex. l'interface graphique).
 */
public class AgentRegistry {
    private final Map<String, AgentDefinition> definitions = new LinkedHashMap<>();
    private final AgentRepository repository;
    private final AgentFactory factory;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public AgentRegistry(AgentRepository repository, AgentFactory factory) {
        this.repository = repository;
        this.factory = factory;
        repository.loadAll().forEach(definition -> definitions.put(definition.id(), definition));
    }

    public synchronized List<AgentDefinition> definitions() {
        return List.copyOf(definitions.values());
    }

    public synchronized Optional<AgentDefinition> find(String id) {
        return Optional.ofNullable(definitions.get(id));
    }

    /** Instancie les agents à partir des définitions courantes (toujours à jour après édition). */
    public synchronized List<Agent> agents() {
        return definitions.values().stream().map(factory::create).toList();
    }

    /** Ajoute ou remplace (même id) un agent, puis persiste. */
    public synchronized void save(AgentDefinition definition) {
        definitions.put(definition.id(), definition);
        persistAndNotify();
    }

    public synchronized void remove(String id) {
        if (definitions.remove(id) != null) {
            persistAndNotify();
        }
    }

    /** Génère un identifiant unique à partir du nom ("Agent Étudiant" -> "agent-etudiant"). */
    public synchronized String newId(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        base = base.isEmpty() ? "agent" : base;
        String id = base;
        for (int i = 2; definitions.containsKey(id); i++) {
            id = base + "-" + i;
        }
        return id;
    }

    /** Observer : appelé après chaque modification du registre. */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void persistAndNotify() {
        repository.saveAll(new ArrayList<>(definitions.values()));
        listeners.forEach(Runnable::run);
    }
}
