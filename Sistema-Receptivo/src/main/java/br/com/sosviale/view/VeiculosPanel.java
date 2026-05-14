package br.com.sosviale.view;

import br.com.sosviale.model.Veiculo;
import br.com.sosviale.service.VeiculoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VeiculosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color DANGER_RED = new Color(200, 50, 50);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final VeiculoService service = new VeiculoService();
    private DefaultTableModel tableModel;
    private JTable table;

    // Campos do formulário
    private JTextField labelField;
    private JTextField placaField;
    private JTextField capacidadeField;
    private JTextField marcaField;
    private JComboBox<String> comboTipo;

    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;

    public VeiculosPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
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
        gbc.insets = new Insets(0, 0, 10, 0);

        JLabel title = new JLabel("Cadastro de Veículo");
        title.setFont(SECTION_FONT);
        gbc.gridy = 0; form.add(title, gbc);

        // Modelo
        gbc.gridy++; form.add(label("Modelo (ex: Sprinter):"), gbc);
        labelField = field();
        gbc.gridy++; form.add(labelField, gbc);

        // Marca (Adicionado)
        gbc.gridy++; form.add(label("Marca (ex: Toyota, Chery):"), gbc);
        marcaField = field();
        gbc.gridy++; form.add(marcaField, gbc);

        // Placa
        gbc.gridy++; form.add(label("Placa:"), gbc);
        placaField = field();
        gbc.gridy++; form.add(placaField, gbc);

        // Tipo (Adicionado)
        gbc.gridy++; form.add(label("Tipo:"), gbc);
        String[] tipos = {"Van", "SUV", "Sedan", "Hatch", "Micro-ônibus"};
        comboTipo = new JComboBox<>(tipos);
        comboTipo.setBackground(Color.WHITE);
        gbc.gridy++; form.add(comboTipo, gbc);

        // Capacidade
        gbc.gridy++; form.add(label("Capacidade de passageiros:"), gbc);
        capacidadeField = field();
        gbc.gridy++; form.add(capacidadeField, gbc);

        // Botões
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
        form.add(actions, gbc);

        return form;
    }

    private void salvarOuAtualizar() {
        String labelTxt = labelField.getText().trim();
        String placa = placaField.getText().trim().toUpperCase();
        String capStr = capacidadeField.getText().trim();
        String marca = marcaField.getText().trim();
        String tipo = (String) comboTipo.getSelectedItem();

        if (labelTxt.isEmpty() || placa.isEmpty() || capStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os campos obrigatórios!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int capacidade = Integer.parseInt(capStr);

            // Criamos o objeto Veiculo para concentrar os dados
            Veiculo v = new Veiculo(labelTxt, placa, capacidade);
            v.setMarca(marca);
            v.setTipo(tipo);

            if (idSelecionado == null) {
                // Se seu service ainda usa 3 parâmetros, você precisará atualizá-lo
                // para aceitar marca e tipo também!
                service.salvar(v.getLabel(), v.getPlaca(), v.getCapacidade());
                JOptionPane.showMessageDialog(this, "Veículo cadastrado!");
            } else {
                v.setId(idSelecionado);
                service.atualizar(idSelecionado, labelTxt, placa, capacidade);
                JOptionPane.showMessageDialog(this, "Veículo atualizado!");
            }

            limparForm();
            carregarVeiculos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacidade inválida.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void carregarVeiculos() {
        tableModel.setRowCount(0);
        service.listarTodos().forEach(v -> {
            tableModel.addRow(new Object[]{v.getId(), v.getLabel(), v.getPlaca(), v.getCapacidade()});
        });
    }

    private void limparForm() {
        idSelecionado = null;
        labelField.setText("");
        placaField.setText("");
        capacidadeField.setText("");
        marcaField.setText("");
        comboTipo.setSelectedIndex(0);
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    // Helpers para manter o padrão visual
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

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        tableModel = new DefaultTableModel(new String[]{"ID", "Modelo", "Placa", "Capacidade"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                labelField.setText((String) tableModel.getValueAt(row, 1));
                placaField.setText((String) tableModel.getValueAt(row, 2));
                capacidadeField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                // Aqui você também poderia preencher Marca e Tipo se eles estivessem na tabela
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        panel.add(new JLabel("Veículos cadastrados", JLabel.LEFT), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void excluirVeiculo() {
        if (idSelecionado != null) {
            service.excluir(idSelecionado);
            limparForm();
            carregarVeiculos();
        }
    }
}