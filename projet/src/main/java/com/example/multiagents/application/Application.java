package com.example.multiagents.application;

import com.example.multiagents.domain.agent.AgentDefinition;
import com.example.multiagents.domain.agent.AgentFactory;
import com.example.multiagents.domain.agent.AgentRegistry;
import com.example.multiagents.domain.agent.AgentRepository;
import com.example.multiagents.domain.agent.PromptBuilder;
import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.ConversationStore;
import com.example.multiagents.infrastructure.llm.LanguageModelFactory;
import com.example.multiagents.infrastructure.llm.LocalLanguageModel;
import com.example.multiagents.infrastructure.persistence.JsonAgentRepository;
import com.example.multiagents.routing.FallbackRoutingStrategy;
import com.example.multiagents.routing.KeywordRoutingStrategy;
import com.example.multiagents.routing.Router;
import com.example.multiagents.routing.RoutingStrategy;
import com.example.multiagents.ui.MainWindow;
import java.nio.file.Path;
import java.util.List;
import javax.swing.SwingUtilities;

/** Point d'entrée : assemble les dépendances (composition root) puis ouvre la fenêtre. */
public final class Application {
    private Application() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.load(Path.of("config", "application.properties"));

        // Infrastructure : LLM local (interchangeable) et persistance JSON
        LocalLanguageModel languageModel = LanguageModelFactory.create(config);
        AgentRepository repository = new JsonAgentRepository(config.agentsFile());

        // Domaine : fabrique d'agents, registre, mémoire des conversations
        PromptBuilder promptBuilder = new PromptBuilder(config.maxHistoryMessages(), config.maxDocumentChars());
        AgentRegistry registry = new AgentRegistry(repository, new AgentFactory(languageModel, promptBuilder));
        if (registry.definitions().isEmpty()) {
            defaultAgents().forEach(registry::save); // premier lancement : agents d'exemple
        }
        ConversationStore conversations = new ConversationStore();

        // Routage : mots-clés, avec repli sur l'agent générique si la confiance est faible
        RoutingStrategy strategy = new FallbackRoutingStrategy(
                new KeywordRoutingStrategy(), config.fallbackAgentId(), config.routingMinConfidence());
        Router router = new Router(registry, strategy);

        SwingUtilities.invokeLater(() -> new MainWindow(registry, router, conversations).setVisible(true));
    }

    /** Agents proposés au premier lancement ; l'utilisateur peut les modifier ou les supprimer. */
    private static List<AgentDefinition> defaultAgents() {
        return List.of(
                new AgentDefinition("student", "StudentAgent", "Répond aux questions des étudiants",
                        List.of("étudiant", "cours", "devoir", "examen", "note", "tp"),
                        "Tu réponds aux étudiants avec un ton pédagogique, bienveillant et précis. "
                                + "Tu ne divulgues jamais de notes individuelles d'autres étudiants.",
                        new AgentContext("Cours, évaluations et accompagnement étudiant.", List.of(),
                                List.of(new AgentContext.ExampleExchange(
                                        "Bonjour, quand est le rendu du TP 2 ?",
                                        "Bonjour,\nLe TP 2 est à rendre vendredi à 23h59 sur la plateforme du cours.\nCordialement"))),
                new AgentDefinition("administration", "AdministrationAgent", "Répond aux demandes administratives",
                        List.of("administration", "secrétariat", "dossier", "inscription", "convention", "budget"),
                        "Tu réponds de manière formelle et concise aux demandes administratives.",
                        new AgentContext("Procédures administratives universitaires.", List.of(), List.of())),
                new AgentDefinition("generic", "GenericAgent", "Agent de repli pour les autres e-mails",
                        List.of(),
                        "Tu réponds prudemment et poliment ; en cas de doute, tu demandes une précision.",
                        new AgentContext("Contexte général de l'utilisateur.", List.of(), List.of())));
    }
}
