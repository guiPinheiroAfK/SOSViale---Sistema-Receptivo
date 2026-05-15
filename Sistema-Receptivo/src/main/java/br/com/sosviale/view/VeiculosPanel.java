package br.com.sosviale.view;

import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.service.VeiculoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VeiculosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);

    private final VeiculoService service = new VeiculoService();
    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField labelField;
    private JTextField marcaField;
    private JTextField placaField;
    private JComboBox<String> comboTipo;
    private JTextField capacidadeField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;
    private JLabel formTitleLabel;
    private JLabel tableTitleLabel;

    public VeiculosPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        I18nRegistry.register(this::refreshTexts);
    }

    private void refreshTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        if (formTitleLabel != null) formTitleLabel.setText(lm.translate("vehicles.form.title"));
        if (tableTitleLabel != null) tableTitleLabel.setText(lm.translate("vehicles.list.title"));
        if (salvarButton != null) salvarButton.setText(lm.translate("common.save"));
        if (excluirButton != null) excluirButton.setText(lm.translate("common.delete"));
        if (tableModel != null) {
            tableModel.setColumnIdentifiers(new String[]{
                    lm.translate("vehicles.table.id"),
                    lm.translate("vehicles.table.model"),
                    lm.translate("vehicles.table.plate"),
                    lm.translate("vehicles.table.capacity")
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

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        form.add(label("Modelo (ex: Sprinter):"), gbc);
        labelField = field();
        gbc.gridy++;
        form.add(labelField, gbc);

        gbc.gridy++;
        form.add(label("Marca (ex: Toyota, Chery):"), gbc);
        marcaField = field();
        gbc.gridy++;
        form.add(marcaField, gbc);

        gbc.gridy++;
        form.add(label("Placa:"), gbc);
        placaField = field();
        gbc.gridy++;
        form.add(placaField, gbc);

        gbc.gridy++;
        form.add(label("Tipo:"), gbc);
        comboTipo = new JComboBox<>(new String[]{"Van", "SUV", "Sedan", "Hatch", "Micro-ônibus"});
        comboTipo.setFont(BASE_FONT);
        comboTipo.setBackground(Color.WHITE);
        comboTipo.setPreferredSize(new Dimension(0, 34));
        gbc.gridy++;
        form.add(comboTipo, gbc);

        gbc.gridy++;
        form.add(label("Capacidade de passageiros:"), gbc);
        capacidadeField = field();
        gbc.gridy++;
        form.add(capacidadeField, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("Adicionar", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = styledButton("Excluir", DANGER_RED);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirVeiculo());

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

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Modelo", "Marca", "Placa", "Tipo", "Cap."}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                labelField.setText(str(tableModel.getValueAt(row, 1)));
                marcaField.setText(str(tableModel.getValueAt(row, 2)));
                placaField.setText(str(tableModel.getValueAt(row, 3)));
                String tipo = str(tableModel.getValueAt(row, 4));
                comboTipo.setSelectedItem(tipo.isEmpty() ? comboTipo.getItemAt(0) : tipo);
                capacidadeField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        JLabel dica = new JLabel("💡 Clique em um veículo para editar ou excluir.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarVeiculos();
        return panel;
    }

    private void salvarOuAtualizar() {
        String labelTxt = labelField.getText().trim();
        String placa = placaField.getText().trim();
        String capStr = capacidadeField.getText().trim();
        String marca = marcaField.getText().trim();
        String tipo = (String) comboTipo.getSelectedItem();

        if (labelTxt.isEmpty() || placa.isEmpty() || capStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int capacidade = Integer.parseInt(capStr);
            if (idSelecionado == null) {
                service.salvar(labelTxt, placa, capacidade, marca, tipo);
                JOptionPane.showMessageDialog(this, "Veículo cadastrado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.atualizar(idSelecionado, labelTxt, placa, capacidade, marca, tipo);
                JOptionPane.showMessageDialog(this, "Veículo atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarVeiculos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacidade inválida.", "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro crítico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirVeiculo() {
        if (idSelecionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este veículo?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.excluir(idSelecionado);
            limparForm();
            carregarVeiculos();
            JOptionPane.showMessageDialog(this, "Veículo excluído!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarVeiculos() {
        tableModel.setRowCount(0);
        service.listarTodos().forEach(v -> tableModel.addRow(new Object[]{
                v.getId(),
                v.getLabel(),
                v.getMarca() != null ? v.getMarca() : "",
                v.getPlaca(),
                v.getTipo() != null ? v.getTipo() : "",
                v.getCapacidade()
        }));
    }

    private void limparForm() {
        idSelecionado = null;
        labelField.setText("");
        marcaField.setText("");
        placaField.setText("");
        capacidadeField.setText("");
        comboTipo.setSelectedIndex(0);
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
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
