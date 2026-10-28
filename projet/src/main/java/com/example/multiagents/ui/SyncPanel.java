package com.example.multiagents.ui;

import com.example.multiagents.application.SettingsService;
import com.example.multiagents.domain.email.Email;
import com.example.multiagents.domain.email.MailboxClientFactory;
import com.example.multiagents.domain.email.MailboxSettings;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Onglet « Synchronisation » : connexion IMAP en lecture seule et liste des derniers e-mails.
 * « Traiter » envoie l'e-mail sélectionné à l'onglet Courrier pour obtenir un brouillon.
 * Le mot de passe reste en mémoire : il n'est jamais enregistré.
 */
class SyncPanel extends JPanel {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final SettingsService settings;
    private final MailboxClientFactory clientFactory;
    private final Consumer<MailSelection> onProcess;

    private final JComboBox<String> provider = new JComboBox<>(new String[] {"Gmail", "Outlook", "Yahoo"});
    private final JSpinner port = new JSpinner(new SpinnerNumberModel(993, 1, 65535, 1));
    private final JTextField username = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);
    private final JTextField folder = new JTextField("INBOX", 12);
    private final JCheckBox ssl = new JCheckBox("SSL/TLS", true);
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(20, 1, 200, 5));
    private final JButton fetch = new JButton("Se connecter et récupérer");
    private final JButton next = new JButton("Mail suivant");
    private final JButton process = new JButton("Traiter l'e-mail sélectionné");
    private final JLabel status = new JLabel("Non connecté.");

    private final DefaultTableModel tableModel = new DefaultTableModel(new String[] {"De", "Sujet", "Reçu le"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final List<Email> emails = new ArrayList<>();

    record MailSelection(List<Email> emails, int index, Email email, MailboxSettings mailbox, char[] password) {}

    SyncPanel(SettingsService settings, MailboxClientFactory clientFactory, Consumer<MailSelection> onProcess) {
        super(new BorderLayout(10, 10));
        this.settings = settings;
        this.clientFactory = clientFactory;
        this.onProcess = onProcess;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        MailboxSettings saved = settings.current().mailbox();
        provider.setSelectedItem(resolveProvider(saved.host()));
        port.setValue(saved.port());
        username.setText(saved.username());
        folder.setText(saved.folder());
        ssl.setSelected(saved.ssl());
        provider.addActionListener(event -> applyProviderDefaults());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Boîte mail (IMAP, lecture seule)"));
        addRow(form, 0, "Serveur IMAP", provider, "Port", port);
        addRow(form, 1, "Identifiant", username, "Mot de passe", password);
        addRow(form, 2, "Dossier", folder, "Nombre de mails", count);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        form.add(ssl, c);
        c.gridy = 4;
        form.add(new JLabel("<html><i>Gmail, Outlook, iCloud : utilisez un « mot de passe d'application » "
                + "(le mot de passe habituel est refusé).</i></html>"), c);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(450);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        JPanel buttons = new JPanel();
        buttons.add(fetch);
        buttons.add(next);
        buttons.add(process);
        process.setEnabled(false);
        next.setEnabled(false);
        JPanel south = new JPanel(new BorderLayout());
        south.add(status, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        fetch.addActionListener(event -> fetchEmails());
        next.addActionListener(event -> nextEmail());
        process.addActionListener(event -> processSelected());
        table.getSelectionModel().addListSelectionListener(
                event -> {
                    boolean hasSelection = table.getSelectedRow() >= 0;
                    process.setEnabled(hasSelection);
                    next.setEnabled(emails.size() > 1);
                });
    }

    private void applyProviderDefaults() {
        String selected = (String) provider.getSelectedItem();
        if (selected == null) {
            return;
        }
        switch (selected) {
            case "Gmail" -> {
                port.setValue(993);
                ssl.setSelected(true);
            }
            case "Outlook" -> {
                port.setValue(993);
                ssl.setSelected(true);
            }
            case "Yahoo" -> {
                port.setValue(993);
                ssl.setSelected(true);
            }
            default -> {
                port.setValue(993);
                ssl.setSelected(true);
            }
        }
    }

    private String resolveProvider(String host) {
        if (host == null || host.isBlank()) {
            return "Gmail";
        }
        if (host.equals("imap.gmail.com") || host.equals("imap.googlemail.com")) {
            return "Gmail";
        }
        if (host.equals("imap-mail.outlook.com") || host.equals("outlook.office365.com")) {
            return "Outlook";
        }
        if (host.equals("imap.mail.yahoo.com")) {
            return "Yahoo";
        }
        return "Gmail";
    }

    private MailboxSettings buildMailboxSettings() {
        String selected = (String) provider.getSelectedItem();
        String hostValue = switch (selected) {
            case "Outlook" -> "imap-mail.outlook.com";
            case "Yahoo" -> "imap.mail.yahoo.com";
            default -> "imap.gmail.com";
        };
        return new MailboxSettings(hostValue, (Integer) port.getValue(), username.getText(), ssl.isSelected(), folder.getText());
    }

    /** Sauvegarde les paramètres (sans mot de passe) puis lit la boîte hors du thread graphique. */
    private void fetchEmails() {
        MailboxSettings mailbox = buildMailboxSettings();
        char[] pwd = password.getPassword();
        if (!mailbox.isConfigured() || pwd.length == 0) {
            status.setText("Renseignez le serveur, l'identifiant et le mot de passe.");
            return;
        }
        settings.updateMailbox(mailbox);
        int max = (Integer) count.getValue();
        fetch.setEnabled(false);
        status.setText("Connexion à " + mailbox.host() + "...");

        new SwingWorker<List<Email>, Void>() {
            @Override
            protected List<Email> doInBackground() {
                try {
                    return clientFactory.create(mailbox, pwd).fetchRecent(max);
                } finally {
                    Arrays.fill(pwd, '\0'); // le mot de passe ne reste pas en mémoire plus que nécessaire
                }
            }

            @Override
            protected void done() {
                fetch.setEnabled(true);
                try {
                    show(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    status.setText("Échec : " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void show(List<Email> fetched) {
        emails.clear();
        emails.addAll(fetched);
        tableModel.setRowCount(0);
        for (Email email : fetched) {
            tableModel.addRow(new Object[] {email.sender(), email.subject(), DATE.format(email.receivedAt())});
        }
        status.setText(fetched.size() + " e-mail(s) récupéré(s). Sélectionnez-en un puis « Traiter ».");
    }

    private void nextEmail() {
        if (emails.isEmpty()) {
            return;
        }
        int current = table.getSelectedRow();
        int nextIndex = current >= 0 ? current + 1 : 0;
        if (nextIndex >= emails.size()) {
            nextIndex = 0;
        }
        table.setRowSelectionInterval(nextIndex, nextIndex);
        processSelected();
    }

    private void processSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            MailboxSettings mailbox = buildMailboxSettings();
            char[] pwd = password.getPassword();
            onProcess.accept(new MailSelection(List.copyOf(emails), row, emails.get(row), mailbox, Arrays.copyOf(pwd, pwd.length)));
            Arrays.fill(pwd, '\0');
        }
    }

    private static void addRow(JPanel panel, int row, String label1, java.awt.Component field1,
                               String label2, java.awt.Component field2) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(label1), c);
        c.gridx = 1;
        panel.add(field1, c);
        c.gridx = 2;
        panel.add(new JLabel(label2), c);
        c.gridx = 3;
        panel.add(field2, c);
    }
}
