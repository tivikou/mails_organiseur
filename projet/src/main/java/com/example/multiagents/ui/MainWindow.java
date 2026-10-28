package com.example.multiagents.ui;

import com.example.multiagents.domain.agent.AgentRegistry;
import com.example.multiagents.domain.email.ConversationStore;
import com.example.multiagents.routing.Router;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/** Fenêtre principale : un onglet pour traiter les e-mails, un onglet pour gérer les agents. */
public class MainWindow extends JFrame {

    public MainWindow(AgentRegistry registry, Router router, ConversationStore conversations) {
        super("Local Email Agents");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Courrier", new MailPanel(router, conversations));
        tabs.addTab("Agents", new AgentsPanel(this, registry));
        add(tabs);
    }
}
