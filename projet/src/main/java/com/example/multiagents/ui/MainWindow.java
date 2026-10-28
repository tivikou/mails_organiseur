package com.example.multiagents.ui;

import com.example.multiagents.domain.email.Email;
import com.example.multiagents.routing.Router;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Initial Swing UI; agent management screens will be added incrementally. */
public class MainWindow extends JFrame {
    private final JTextArea proposedReply = new JTextArea();

    public MainWindow(Router router, Email email, Router.RoutedDraft routedDraft) {
        super("Local Email Agents");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.add(new JLabel("Expéditeur : " + email.sender()));
        header.add(new JLabel("Sujet : " + email.subject()));
        header.add(new JLabel("Agent sélectionné : " + routedDraft.decision().agent().name()
                + " (confiance : " + routedDraft.decision().confidence() + ")"));

        JTextArea receivedEmail = new JTextArea(email.body());
        receivedEmail.setLineWrap(true);
        receivedEmail.setWrapStyleWord(true);
        receivedEmail.setEditable(false);

        proposedReply.setText(routedDraft.reply().content());
        proposedReply.setLineWrap(true);
        proposedReply.setWrapStyleWord(true);

        JButton acceptButton = new JButton("Accepter la proposition");
        acceptButton.addActionListener(event -> acceptButton.setText("Proposition acceptée"));
        JButton rejectButton = new JButton("Rejeter");
        rejectButton.addActionListener(event -> proposedReply.setText(""));
        JPanel actions = new JPanel();
        actions.add(acceptButton);
        actions.add(rejectButton);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 0));
        content.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        content.add(new JScrollPane(receivedEmail));
        content.add(new JScrollPane(proposedReply));

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }
}
