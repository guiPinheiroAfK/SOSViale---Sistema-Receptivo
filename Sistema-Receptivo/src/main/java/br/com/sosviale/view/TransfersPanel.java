package br.com.sosviale.view;

import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TransfersPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color DANGER_RED = new Color(200, 50, 50);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final TransferService service = new TransferService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField origemField;
    private JTextField destinoField;
    private JTextField dataHoraField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TransfersPanel() {
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

        JLabel title = new JLabel("Agendar Transfer");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(title, gbc);

        // Origem
        JLabel origemLabel = new JLabel("Origem:");
        origemLabel.setFont(BASE_FONT);
        origemLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(origemLabel, gbc);

        origemField = new JTextField();
        origemField.setFont(BASE_FONT);
        origemField.setPreferredSize(new Dimension(0, 34));
        origemField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(origemField, gbc);

        // Destino
        JLabel destinoLabel = new JLabel("Destino:");
        destinoLabel.setFont(BASE_FONT);
        destinoLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 4, 0);
        form.add(destinoLabel, gbc);

        destinoField = new JTextField();
        destinoField.setFont(BASE_FONT);
        destinoField.setPreferredSize(new Dimension(0, 34));
        destinoField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(destinoField, gbc);

        // Data/Hora
        JLabel dataLabel = new JLabel("Data/Hora (dd/MM/yyyy HH:mm):");
        dataLabel.setFont(BASE_FONT);
        dataLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 4, 0);
        form.add(dataLabel, gbc);

        dataHoraField = new JTextField();
        dataHoraField.setFont(BASE_FONT);
        dataHoraField.setPreferredSize(new Dimension(0, 34));
        dataHoraField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(dataHoraField, gbc);

        // Botões
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = new JButton("Agendar");
        salvarButton.setBackground(PRIMARY_BLUE);
        salvarButton.setForeground(Color.WHITE);
        salvarButton.setFocusPainted(false);
        salvarButton.setOpaque(true);
        salvarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE),
                new EmptyBorder(8, 14, 8, 14)
        ));
        salvarButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = new JButton("Excluir");
        excluirButton.setBackground(DANGER_RED);
        excluirButton.setForeground(Color.WHITE);
        excluirButton.setFocusPainted(false);
        excluirButton.setOpaque(true);
        excluirButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER_RED),
                new EmptyBorder(8, 14, 8, 14)
        ));
        excluirButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirTransfer());

        JButton limpar = new JButton("Limpar");
        limpar.setBackground(PANEL_BACKGROUND);
        limpar.setForeground(TEXT_COLOR);
        limpar.setFocusPainted(false);
        limpar.setOpaque(true);
        limpar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 14, 8, 14)
        ));
        limpar.setFont(BASE_FONT);
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

        JLabel title = new JLabel("Transfers cadastrados");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Origem", "Destino", "Data/Hora", "Status", "Motorista"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 232, 236));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(BASE_FONT);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                origemField.setText((String) tableModel.getValueAt(row, 1));
                destinoField.setText((String) tableModel.getValueAt(row, 2));
                dataHoraField.setText((String) tableModel.getValueAt(row, 3));
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        JLabel dica = new JLabel("💡 Clique em um transfer para editar ou excluir.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarTransfers();
        return panel;
    }

    private void salvarOuAtualizar() {
        String origem = origemField.getText().trim();
        String destino = destinoField.getText().trim();
        String dataHoraStr = dataHoraField.getText().trim();

        if (origem.isEmpty() || destino.isEmpty() || dataHoraStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formato de data inválido!\nUse: dd/MM/yyyy HH:mm",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Transfer t = new Transfer();
            t.setOrigem(origem);
            t.setDestino(destino);
            t.setDataHora(dataHora);

            if (idSelecionado == null) {
                service.cadastrar(t);
                JOptionPane.showMessageDialog(this, "Transfer agendado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                t.setId(idSelecionado);
                service.atualizar(t);
                JOptionPane.showMessageDialog(this, "Transfer atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarTransfers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirTransfer() {
        if (idSelecionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este transfer?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.excluir(Long.valueOf(idSelecionado));
                limparForm();
                carregarTransfers();
                JOptionPane.showMessageDialog(this, "Transfer excluído!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarTransfers() {
        tableModel.setRowCount(0);
        try {
            List<Transfer> lista = service.listarTodos();
            for (Transfer t : lista) {
                String motorista = (t.getOrdemServico() != null && t.getOrdemServico().getMotorista() != null)
                        ? t.getOrdemServico().getMotorista().getNome()
                        : "Sem OS";
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getOrigem(),
                        t.getDestino(),
                        t.getDataHora().format(FORMATTER),
                        t.getStatus(),
                        motorista
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limparForm() {
        idSelecionado = null;
        origemField.setText("");
        destinoField.setText("");
        dataHoraField.setText("");
        salvarButton.setText("Agendar");
        excluirButton.setVisible(false);
        table.clearSelection();
        origemField.requestFocus();
    }
}