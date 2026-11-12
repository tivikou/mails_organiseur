package com.example.multiagents.ui;

import com.example.multiagents.domain.agent.AgentDefinition;
import com.example.multiagents.domain.agent.AgentRegistry;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

/** Onglet « Agents » : liste, création, modification et suppression des agents. */
class AgentsPanel extends JPanel {
    private final Frame owner;
    private final AgentRegistry registry;
    private final DefaultListModel<AgentDefinition> listModel = new DefaultListModel<>();
    private final JList<AgentDefinition> list = new JList<>(listModel);
    private final JTextArea preview = new JTextArea();

    AgentsPanel(Frame owner, AgentRegistry registry) {
        super(new BorderLayout(10, 10));
        this.owner = owner;
        this.registry = registry;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer((l, def, index, selected, focus) -> {
            javax.swing.JLabel label = new javax.swing.JLabel(def.name() + "  —  " + def.description());
            label.setOpaque(true);
            label.setBackground(selected ? l.getSelectionBackground() : l.getBackground());
            label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            return label;
        });
        list.addListSelectionListener(event -> showPreview(list.getSelectedValue()));

        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        JScrollPane previewScroll = new JScrollPane(preview);
        previewScroll.setBorder(BorderFactory.createTitledBorder("Détails de l'agent"));

        JButton create = new JButton("Nouvel agent");
        JButton edit = new JButton("Modifier");
        JButton delete = new JButton("Supprimer");
        create.addActionListener(event -> openEditor(null));
        edit.addActionListener(event -> {
            if (list.getSelectedValue() != null) {
                openEditor(list.getSelectedValue());
            }
        });
        delete.addActionListener(event -> deleteSelected());
        JPanel buttons = new JPanel();
        buttons.add(create);
        buttons.add(edit);
        buttons.add(delete);

        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new java.awt.Dimension(380, 0));
        add(listScroll, BorderLayout.WEST);
        add(previewScroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        // Observer : la liste se rafraîchit à chaque modification du registre
        registry.addListener(this::refresh);
        refresh();
    }

    private void refresh() {
        AgentDefinition selected = list.getSelectedValue();
        listModel.clear();
        registry.definitions().forEach(listModel::addElement);
        if (selected != null) {
            // on retrouve la version à jour de l'agent précédemment sélectionné
            registry.find(selected.id()).ifPresent(updated -> list.setSelectedValue(updated, true));
        }
    }

    private void showPreview(AgentDefinition def) {
        if (def == null) {
            preview.setText("");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append("Mots-clés : ").append(String.join(", ", def.keywords())).append("\n\n");
        text.append("System prompt :\n").append(def.systemPrompt()).append("\n\n");
        text.append("Instructions :\n").append(def.context().instructions()).append("\n\n");
        text.append("Fichiers de référence : ").append(def.context().referenceDocuments().size()).append('\n');
        text.append("Exemples mail/réponse : ").append(def.context().examples().size());
        preview.setText(text.toString());
        preview.setCaretPosition(0);
    }

    /** {@code existing == null} : création d'un nouvel agent. */
    private void openEditor(AgentDefinition existing) {
        Optional<AgentDefinition> result = new AgentEditorDialog(owner, registry, existing).showDialog();
        result.ifPresent(registry::save);
    }

    private void deleteSelected() {
        AgentDefinition selected = list.getSelectedValue();
        if (selected == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Supprimer l'agent « " + selected.name() + " » ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            registry.remove(selected.id());
        }
    }
}
