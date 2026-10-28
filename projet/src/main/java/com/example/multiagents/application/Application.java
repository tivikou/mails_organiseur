package com.example.multiagents.application;

import com.example.multiagents.domain.agent.Agent;
import com.example.multiagents.domain.agent.SpecializedAgent;
import com.example.multiagents.domain.context.AgentContext;
import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.infrastructure.llm.DemoLocalLanguageModel;
import com.example.multiagents.infrastructure.llm.LocalLanguageModel;
import com.example.multiagents.routing.KeywordRoutingStrategy;
import com.example.multiagents.routing.Router;
import com.example.multiagents.ui.MainWindow;
import java.util.List;
import javax.swing.SwingUtilities;

public final class Application {
    private Application() {
    }

    public static void main(String[] args) {
        LocalLanguageModel languageModel = new DemoLocalLanguageModel();
        List<Agent> agents = List.of(
                new SpecializedAgent("student", "StudentAgent",
                        "Répondre aux étudiants avec un ton pédagogique et précis.",
                        new AgentContext("Cours, évaluations et accompagnement étudiant.", List.of(), List.of()), languageModel),
                new SpecializedAgent("administration", "AdministrationAgent",
                        "Répondre aux demandes administratives de manière formelle.",
                        new AgentContext("Procédures administratives universitaires.", List.of(), List.of()), languageModel),
                new SpecializedAgent("generic", "GenericAgent",
                        "Répondre aux demandes générales avec prudence.",
                        new AgentContext("Contexte général de l'utilisateur.", List.of(), List.of()), languageModel)
        );
        Router router = new Router(agents, new KeywordRoutingStrategy());
        Email demoEmail = new Email("student@example.org", "Question sur le cours",
                "Bonjour, pourriez-vous préciser la date du prochain devoir ?", java.time.Instant.now());
        Router.RoutedDraft result = router.process(demoEmail, new Conversation("demo-thread"));
        SwingUtilities.invokeLater(() -> new MainWindow(router, demoEmail, result).setVisible(true));
    }
}
