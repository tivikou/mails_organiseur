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
    /**
     * Règles communes à tous les agents, placées avant le system prompt de l'utilisateur :
     * elles imposent une vraie réponse rédigée (et non un commentaire sur l'e-mail).
     */
    static final String WRITING_RULES = """
            Tu rédiges la réponse à l'e-mail reçu, au nom de l'utilisateur.
            - Écris uniquement le texte de l'e-mail à envoyer : formule d'appel, corps, formule de politesse. \
            Aucun commentaire, aucune explication, aucun titre.
            - Réponds dans la langue de l'expéditeur.
            - Reprends le ton et la structure des exemples fournis, quand il y en a.
            - N'invente aucun fait (date, note, décision, chiffre) absent du contexte ; si une information \
            manque, dis-le poliment ou demande une précision.""";

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
        String system = WRITING_RULES + "\n\n" + definition.systemPrompt();
        String response = languageModel.generate(system, contextText, userMessage);
        return new DraftReply(response, definition.id(), 0.80);
    }
}
