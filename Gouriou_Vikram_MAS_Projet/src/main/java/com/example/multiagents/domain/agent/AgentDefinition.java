package com.example.multiagents.domain.agent;

import com.example.multiagents.domain.context.AgentContext;
import java.util.List;

/**
 * Description <b>persistable</b> d'un agent : tout ce que l'utilisateur configure dans l'interface.
 * Séparer la définition (données) du comportement ({@link SpecializedAgent}) permet de créer ou
 * modifier un agent sans toucher au code.
 *
 * @param id           identifiant technique stable (ex. "student")
 * @param name         nom affiché (ex. "StudentAgent")
 * @param description  rôle de l'agent en une phrase
 * @param keywords     mots-clés utilisés par le routage
 * @param systemPrompt rôle, ton, structure de réponse, interdits...
 * @param context      instructions, fichiers de référence et exemples mail/réponse
 */
public record AgentDefinition(
        String id,
        String name,
        String description,
        List<String> keywords,
        String systemPrompt,
        AgentContext context) {

    public AgentDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        description = description == null ? "" : description;
        keywords = List.copyOf(keywords == null ? List.of() : keywords);
        systemPrompt = systemPrompt == null ? "" : systemPrompt;
        context = context == null ? new AgentContext("", List.of(), List.of()) : context;
    }
}
