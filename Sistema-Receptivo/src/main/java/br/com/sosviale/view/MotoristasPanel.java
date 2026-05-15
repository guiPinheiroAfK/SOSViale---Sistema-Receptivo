package br.com.sosviale.view;

import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.Motorista;
import br.com.sosviale.service.MotoristaService;
import br.com.sosviale.util.OfflineReadGuard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MotoristasPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);

    private final MotoristaService service = new MotoristaService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nomeField;
    private JTextField cnhField;
    private JTextField telefoneField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;
    private JLabel formTitleLabel;
    private JLabel tableTitleLabel;

    public MotoristasPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        I18nRegistry.register(this::refreshTexts);
    }

    private void refreshTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        if (formTitleLabel != null) formTitleLabel.setText(lm.translate("drivers.form.title"));
        if (tableTitleLabel != null) tableTitleLabel.setText(lm.translate("drivers.list.title"));
        if (salvarButton != null) salvarButton.setText(lm.translate("common.save"));
        if (excluirButton != null) excluirButton.setText(lm.translate("common.delete"));
        if (tableModel != null) {
            tableModel.setColumnIdentifiers(new String[]{
                    lm.translate("drivers.table.id"),
                    lm.translate("drivers.table.name"),
                    lm.translate("drivers.table.license"),
                    lm.translate("drivers.table.phone")
            });
        }
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BACKGROUND);
        form.setPreferredSize(new Dimension(345, 0));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        formTitleLabel = new JLabel();
        formTitleLabel.setFont(SECTION_FONT);
        formTitleLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(formTitleLabel, gbc);

        gbc.gridy++;
        form.add(label("Nome completo:"), gbc);
        nomeField = field();
        gbc.gridy++;
        form.add(nomeField, gbc);

        gbc.gridy++;
        form.add(label("CNH:"), gbc);
        cnhField = field();
        gbc.gridy++;
        form.add(cnhField, gbc);

        gbc.gridy++;
        form.add(label("Telefone:"), gbc);
        telefoneField = field();
        gbc.gridy++;
        form.add(telefoneField, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("Adicionar", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = styledButton("Excluir", DANGER_RED);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirMotorista());

        JButton limpar = styledButton("Limpar", Color.LIGHT_GRAY);
        limpar.setForeground(TEXT_COLOR);
        limpar.addActionListener(e -> limparForm());

        actions.add(salvarButton);
        actions.add(excluirButton);
        actions.add(limpar);

        gbc.gridy = 99;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(18, 0, 0, 0);
        form.add(actions, gbc);

        return form;
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        tableTitleLabel = new JLabel();
        tableTitleLabel.setFont(SECTION_FONT);
        tableTitleLabel.setForeground(TEXT_COLOR);
        panel.add(tableTitleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "CNH", "Telefone"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                nomeField.setText((String) tableModel.getValueAt(row, 1));
                cnhField.setText((String) tableModel.getValueAt(row, 2));
                Object tel = tableModel.getValueAt(row, 3);
                telefoneField.setText(tel != null ? tel.toString() : "");
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        JLabel dica = new JLabel("💡 Clique em um motorista para editar ou excluir.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarMotoristas();
        return panel;
    }

    private void salvarOuAtualizar() {
        String nome = nomeField.getText().trim();
        String cnh = cnhField.getText().trim();
        String telefone = telefoneField.getText().trim();

        try {
            if (idSelecionado == null) {
                service.salvar(nome, cnh, telefone);
                JOptionPane.showMessageDialog(this, "Motorista cadastrado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.atualizar(idSelecionado, nome, cnh, telefone);
                JOptionPane.showMessageDialog(this, "Motorista atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarMotoristas();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro crítico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirMotorista() {
        if (idSelecionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este motorista?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.excluir(idSelecionado);
            limparForm();
            carregarMotoristas();
            JOptionPane.showMessageDialog(this, "Motorista excluído!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarMotoristas() {
        if (OfflineReadGuard.shouldSkipDatabaseReads()) return;
        tableModel.setRowCount(0);
        service.listarTodos().forEach(m -> tableModel.addRow(new Object[]{
                m.getId(),
                m.getNome(),
                m.getCnh(),
                m.getTelefone() != null ? m.getTelefone() : ""
        }));
    }

    private void limparForm() {
        idSelecionado = null;
        nomeField.setText("");
        cnhField.setText("");
        telefoneField.setText("");
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(BASE_FONT);
        f.setPreferredSize(new Dimension(0, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        return f;
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }
}
