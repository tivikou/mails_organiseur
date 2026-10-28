package com.example.multiagents.ui;

import com.example.multiagents.application.SettingsService;
import com.example.multiagents.domain.agent.AgentRegistry;
import com.example.multiagents.domain.email.ConversationStore;
import com.example.multiagents.domain.email.MailboxClientFactory;
import com.example.multiagents.routing.Router;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/** Fenêtre principale : Courrier, Synchronisation (boîte mail), Agents et Modèle LLM. */
public class MainWindow extends JFrame {
    private static final int MAIL_TAB = 0;

    public MainWindow(AgentRegistry registry, Router router, ConversationStore conversations,
                      SettingsService settings, MailboxClientFactory mailboxClientFactory) {
        super("Local Email Agents");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        MailPanel mailPanel = new MailPanel(router, conversations);
        tabs.addTab("Courrier", mailPanel);
        // Un e-mail synchronisé est envoyé à l'onglet Courrier, qui lance aussitôt l'analyse
        tabs.addTab("Synchronisation", new SyncPanel(settings, mailboxClientFactory, email -> {
            tabs.setSelectedIndex(MAIL_TAB);
            mailPanel.loadEmail(email);
        }));
        tabs.addTab("Agents", new AgentsPanel(this, registry));
        tabs.addTab("Modèle LLM", new SettingsPanel(settings));
        add(tabs);
    }
}
