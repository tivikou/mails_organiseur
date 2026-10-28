package com.example.multiagents.ui;

import com.example.multiagents.domain.email.Conversation;
import com.example.multiagents.domain.email.ConversationMessage;
import com.example.multiagents.domain.email.ConversationStore;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.routing.Router;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Onglet « Courrier » : saisie d'un e-mail reçu, affichage de l'agent choisi par le routeur
 * et du brouillon proposé. L'utilisateur valide, modifie ou rejette : rien n'est envoyé.
 */
class MailPanel extends JPanel {
    private final Router router;
    private final ConversationStore conversations;

    private final JTextField sender = new JTextField("etudiant@univ.example");
    private final JTextField subject = new JTextField("Question sur le devoir du cours");
    private final JTextArea body = textArea("Bonjour, pourriez-vous préciser la date du prochain devoir ?");
    private final JLabel routingInfo = new JLabel("Aucun e-mail analysé.");
    private final JTextArea draft = textArea("");
    private final JButton analyze = new JButton("Analyser et proposer une réponse");
    private final JButton accept = new JButton("Valider la réponse");
    private final JButton reject = new JButton("Rejeter");

    /** Fil et e-mail en cours de traitement (pour enregistrer la réponse validée). */
    private Conversation currentConversation;

    MailPanel(Router router, ConversationStore conversations) {
        super(new BorderLayout(10, 10));
        this.router = router;
        this.conversations = conversations;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Haut : expéditeur et sujet
        JPanel header = new JPanel(new GridLayout(2, 2, 5, 5));
        header.add(new JLabel("Expéditeur :"));
        header.add(sender);
        header.add(new JLabel("Sujet :"));
        header.add(subject);

        // Centre : e-mail reçu | brouillon éditable
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.add(titled("E-mail reçu", body));
        center.add(titled("Réponse proposée (modifiable)", draft));

        // Bas : agent sélectionné + actions
        JPanel actions = new JPanel();
        actions.add(analyze);
        actions.add(accept);
        actions.add(reject);
        JPanel south = new JPanel(new BorderLayout());
        south.add(routingInfo, BorderLayout.NORTH);
        south.add(actions, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        accept.setEnabled(false);
        reject.setEnabled(false);
        analyze.addActionListener(event -> analyzeEmail());
        accept.addActionListener(event -> acceptDraft());
        reject.addActionListener(event -> rejectDraft());
    }

    /** Lance le routage + la génération hors du thread graphique (le LLM peut être lent). */
    private void analyzeEmail() {
        final Email email;
        try {
            email = new Email(sender.getText(), subject.getText(), body.getText(), Instant.now());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "E-mail invalide", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Conversation conversation = conversations.forEmail(email);
        analyze.setEnabled(false);
        routingInfo.setText("Analyse en cours...");

        new SwingWorker<Router.RoutedDraft, Void>() {
            @Override
            protected Router.RoutedDraft doInBackground() {
                return router.process(email, conversation);
            }

            @Override
            protected void done() {
                analyze.setEnabled(true);
                try {
                    Router.RoutedDraft result = get();
                    // Mémoire du fil : le message reçu puis le brouillon généré
                    conversation.addMessage(new ConversationMessage(
                            ConversationMessage.Role.SENDER, email.body(), email.receivedAt()));
                    conversation.addMessage(new ConversationMessage(
                            ConversationMessage.Role.ASSISTANT, result.reply().content(), Instant.now()));
                    currentConversation = conversation;
                    routingInfo.setText("Agent sélectionné : " + result.decision().agent().name()
                            + " | confiance " + Math.round(result.decision().confidence() * 100) + " %"
                            + " | " + result.decision().explanation());
                    draft.setText(result.reply().content());
                    accept.setEnabled(true);
                    reject.setEnabled(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    routingInfo.setText("Échec de la génération.");
                    JOptionPane.showMessageDialog(MailPanel.this, e.getCause().getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** La réponse (éventuellement modifiée) est mémorisée comme « réponse envoyée » dans le fil. */
    private void acceptDraft() {
        String finalText = draft.getText().strip();
        if (finalText.isEmpty() || currentConversation == null) {
            return;
        }
        currentConversation.addMessage(new ConversationMessage(
                ConversationMessage.Role.USER, finalText, Instant.now()));
        routingInfo.setText("Réponse validée et mémorisée dans le fil (à copier dans votre messagerie).");
        accept.setEnabled(false);
        reject.setEnabled(false);
    }

    private void rejectDraft() {
        draft.setText("");
        routingInfo.setText("Proposition rejetée.");
        accept.setEnabled(false);
        reject.setEnabled(false);
    }

    private static JTextArea textArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private static JScrollPane titled(String title, JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }
}
