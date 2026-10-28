package com.example.multiagents.ui;

import com.example.multiagents.application.AppConfig;
import com.example.multiagents.application.SettingsService;
import com.example.multiagents.infrastructure.llm.LanguageModelFactory;
import com.example.multiagents.infrastructure.llm.OllamaLanguageModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Onglet « Modèle LLM » : choix du moteur local (Ollama ou démo), de l'URL et du modèle,
 * avec test de connexion et test de rédaction. Les réglages sont appliqués à chaud et sauvegardés.
 */
class SettingsPanel extends JPanel {
    private final SettingsService settings;

    private final JComboBox<String> provider = new JComboBox<>(new String[] {"ollama", "demo"});
    private final JTextField url = new JTextField(30);
    private final JComboBox<String> model = new JComboBox<>();
    private final JTextArea output = new JTextArea();
    private final JButton listModels = new JButton("Lister les modèles installés");
    private final JButton testReply = new JButton("Tester une réponse");
    private final JButton save = new JButton("Enregistrer et appliquer");

    SettingsPanel(SettingsService settings) {
        super(new BorderLayout(10, 10));
        this.settings = settings;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        AppConfig config = settings.current();
        provider.setSelectedItem(config.llmProvider());
        url.setText(config.ollamaUrl());
        model.setEditable(true); // on peut taper un nom de modèle même sans avoir listé les modèles
        model.addItem(config.ollamaModel());
        model.setSelectedItem(config.ollamaModel());

        JPanel form = new JPanel(new GridBagLayout());
        addRow(form, 0, "Moteur", provider);
        addRow(form, 1, "URL du serveur Ollama", url);
        addRow(form, 2, "Modèle", model);

        JPanel buttons = new JPanel();
        buttons.add(listModels);
        buttons.add(testReply);
        buttons.add(save);

        JLabel help = new JLabel("<html>1. Installez <b>Ollama</b> et téléchargez un modèle (ex. <code>ollama pull llama3.2</code>)."
                + "<br>2. Choisissez « ollama », vérifiez l'URL, listez les modèles et sélectionnez-en un."
                + "<br>3. « Tester une réponse » vérifie que le modèle écrit bien, puis « Enregistrer et appliquer ».</html>");

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(output);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Résultat"));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.add(help, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(outputScroll, BorderLayout.CENTER);

        listModels.addActionListener(event -> refreshModels());
        testReply.addActionListener(event -> testGeneration());
        save.addActionListener(event -> saveSettings());
    }

    /** Interroge Ollama (hors thread graphique) et remplit la liste déroulante. */
    private void refreshModels() {
        OllamaLanguageModel client = new OllamaLanguageModel(url.getText(), selectedModel(),
                settings.current().llmTimeout());
        runAsync("Recherche des modèles...", client::listModels, (List<String> names) -> {
            Object previous = model.getSelectedItem();
            model.removeAllItems();
            names.forEach(model::addItem);
            if (previous != null && !previous.toString().isBlank()) {
                model.setSelectedItem(previous);
            }
            output.setText(names.isEmpty()
                    ? "Connexion réussie, mais aucun modèle installé. Lancez : ollama pull llama3.2"
                    : "Connexion réussie. " + names.size() + " modèle(s) installé(s) :\n" + String.join("\n", names));
        });
    }

    /** Demande au modèle choisi de rédiger une réponse type, pour vérifier qu'il écrit correctement. */
    private void testGeneration() {
        AppConfig candidate = settings.current().withLlm(
                provider.getSelectedItem().toString(), url.getText(), selectedModel());
        runAsync("Génération en cours (le premier appel peut être long)...", () ->
                LanguageModelFactory.create(candidate).generate(
                        "Tu rédiges des réponses d'e-mail professionnelles, courtes et polies. "
                                + "Écris uniquement le texte de l'e-mail.",
                        "",
                        "De : etudiant@univ.example\nSujet : Date du rendu\n\n"
                                + "Bonjour, pourriez-vous me rappeler la date du prochain devoir ?"),
                (String reply) -> output.setText("Réponse du modèle :\n\n" + reply));
    }

    private void saveSettings() {
        try {
            settings.updateLlm(provider.getSelectedItem().toString(), url.getText().trim(), selectedModel());
            output.setText("Réglages enregistrés et appliqués : les prochaines réponses utilisent ce modèle.");
        } catch (RuntimeException e) {
            output.setText("Échec : " + e.getMessage());
        }
    }

    private String selectedModel() {
        Object item = model.getSelectedItem();
        return item == null ? "" : item.toString().trim();
    }

    /** Exécute une tâche réseau hors du thread graphique et affiche l'éventuelle erreur dans la zone de résultat. */
    private <T> void runAsync(String message, Callable<T> task, Consumer<T> onSuccess) {
        output.setText(message);
        setButtonsEnabled(false);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    output.setText("Échec : " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        listModels.setEnabled(enabled);
        testReply.setEnabled(enabled);
        save.setEnabled(enabled);
    }

    private static void addRow(JPanel panel, int row, String label, java.awt.Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }
}
