package com.example.multiagents.ui;

import com.example.multiagents.domain.agent.AgentDefinition;
import com.example.multiagents.domain.agent.AgentRegistry;
import com.example.multiagents.domain.context.AgentContext;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Formulaire de création / modification d'un agent : identité, mots-clés, system prompt, et
 * contexte (instructions, fichiers de référence, exemples d'e-mails avec la réponse attendue).
 */
class AgentEditorDialog extends JDialog {
    private final AgentRegistry registry;
    private final AgentDefinition existing;

    private final JTextField name = new JTextField();
    private final JTextField description = new JTextField();
    private final JTextField keywords = new JTextField();
    private final JTextArea systemPrompt = area(5);
    private final JTextArea instructions = area(4);
    private final DefaultListModel<String> filesModel = new DefaultListModel<>();
    private final DefaultListModel<AgentContext.ExampleExchange> examplesModel = new DefaultListModel<>();

    private AgentDefinition result;

    AgentEditorDialog(Frame owner, AgentRegistry registry, AgentDefinition existing) {
        super(owner, existing == null ? "Nouvel agent" : "Modifier l'agent", true);
        this.registry = registry;
        this.existing = existing;
        buildForm();
        if (existing != null) {
            fillFrom(existing);
        }
        setSize(720, 780);
        setLocationRelativeTo(owner);
    }

    /** Affiche le dialogue (bloquant) ; vide si l'utilisateur annule. */
    Optional<AgentDefinition> showDialog() {
        setVisible(true);
        return Optional.ofNullable(result);
    }

    private void buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(labeled("Nom", name));
        form.add(labeled("Description (rôle en une phrase)", description));
        form.add(labeled("Mots-clés de routage (séparés par des virgules)", keywords));
        form.add(labeled("System prompt (rôle, ton, structure, interdits)", new JScrollPane(systemPrompt)));
        form.add(labeled("Instructions de contexte", new JScrollPane(instructions)));
        form.add(filesSection());
        form.add(examplesSection());

        JButton save = new JButton("Enregistrer");
        JButton cancel = new JButton("Annuler");
        save.addActionListener(event -> save());
        cancel.addActionListener(event -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(save);
        buttons.add(cancel);

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    /** Fichiers de référence : on mémorise leurs chemins, le contenu est lu à la génération. */
    private JPanel filesSection() {
        JList<String> list = new JList<>(filesModel);
        JButton add = new JButton("Ajouter un fichier...");
        JButton remove = new JButton("Retirer");
        add.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filesModel.addElement(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        remove.addActionListener(event -> {
            if (list.getSelectedIndex() >= 0) {
                filesModel.remove(list.getSelectedIndex());
            }
        });
        return listSection("Fichiers de référence", list, add, remove);
    }

    /** Exemples few-shot : un e-mail reçu type + la réponse que l'utilisateur aurait envoyée. */
    private JPanel examplesSection() {
        JList<AgentContext.ExampleExchange> list = new JList<>(examplesModel);
        list.setCellRenderer((l, example, index, selected, focus) -> {
            String firstLine = example.email().lines().findFirst().orElse("");
            JLabel label = new JLabel("Exemple " + (index + 1) + " : " + firstLine);
            label.setOpaque(true);
            label.setBackground(selected ? l.getSelectionBackground() : l.getBackground());
            return label;
        });
        JButton add = new JButton("Ajouter un exemple...");
        JButton remove = new JButton("Retirer");
        add.addActionListener(event -> askExample().ifPresent(examplesModel::addElement));
        remove.addActionListener(event -> {
            if (list.getSelectedIndex() >= 0) {
                examplesModel.remove(list.getSelectedIndex());
            }
        });
        return listSection("Exemples d'e-mails et de réponses", list, add, remove);
    }

    private Optional<AgentContext.ExampleExchange> askExample() {
        JTextArea email = area(6);
        JTextArea response = area(6);
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 5));
        panel.add(new JLabel("E-mail reçu :"));
        panel.add(new JScrollPane(email));
        panel.add(new JLabel("Réponse attendue :"));
        panel.add(new JScrollPane(response));
        panel.setPreferredSize(new java.awt.Dimension(500, 400));
        int answer = JOptionPane.showConfirmDialog(this, panel, "Nouvel exemple",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION || email.getText().isBlank() || response.getText().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new AgentContext.ExampleExchange(email.getText().strip(), response.getText().strip()));
    }

    /** Valide les champs, puis construit la définition (nouvel id si création). */
    private void save() {
        if (name.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Le nom est obligatoire.", "Champ manquant", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<String> keywordList = Arrays.stream(keywords.getText().split(","))
                .map(String::trim).filter(k -> !k.isEmpty()).toList();
        AgentContext context = new AgentContext(
                instructions.getText().strip(),
                new ArrayList<>(java.util.Collections.list(filesModel.elements())),
                new ArrayList<>(java.util.Collections.list(examplesModel.elements())));
        String id = existing != null ? existing.id() : registry.newId(name.getText());
        result = new AgentDefinition(id, name.getText().strip(), description.getText().strip(),
                keywordList, systemPrompt.getText().strip(), context);
        dispose();
    }

    private void fillFrom(AgentDefinition def) {
        name.setText(def.name());
        description.setText(def.description());
        keywords.setText(String.join(", ", def.keywords()));
        systemPrompt.setText(def.systemPrompt());
        instructions.setText(def.context().instructions());
        def.context().referenceDocuments().forEach(filesModel::addElement);
        def.context().examples().forEach(examplesModel::addElement);
    }

    // --- petits utilitaires de mise en page ---

    private static JTextArea area(int rows) {
        JTextArea area = new JTextArea(rows, 40);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private static JPanel labeled(String label, java.awt.Component field) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel listSection(String title, JList<?> list, JButton add, JButton remove) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new java.awt.Dimension(400, 90));
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(add);
        buttons.add(Box.createVerticalStrut(5));
        buttons.add(remove);
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }
}
