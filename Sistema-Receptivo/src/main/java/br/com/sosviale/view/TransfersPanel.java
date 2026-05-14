package br.com.sosviale.view;

import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.PontoColetaService;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
    private final PontoColetaService pcService = new PontoColetaService();

    private final List<Passageiro> passageirosSelecionados = new ArrayList<>();
    private DefaultTableModel modelPassageirosTransfer;

    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<PontoColeta> comboOrigem;
    private JComboBox<PontoColeta> comboDestino;
    private JTextField dataField;
    private JTextField horaField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
        gbc.insets = new Insets(0, 0, 10, 0);

        JLabel title = new JLabel("Agendar Transfer");
        title.setFont(SECTION_FONT);
        gbc.gridy = 0; form.add(title, gbc);

        gbc.gridy++; form.add(label("Passageiros no Grupo:"), gbc);

        JPanel pPass = new JPanel(new BorderLayout(5, 0));
        pPass.setOpaque(false);

        modelPassageirosTransfer = new DefaultTableModel(new String[]{"Nome"}, 0);
        JTable miniTable = new JTable(modelPassageirosTransfer);
        JScrollPane scroll = new JScrollPane(miniTable);
        scroll.setPreferredSize(new Dimension(0, 80));

        JButton btnAdd = styledButton("+", PRIMARY_BLUE);
        btnAdd.addActionListener(e -> {
            PassageiroSelectionDialog dialog = new PassageiroSelectionDialog((Frame) SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            Passageiro p = dialog.getSelecionado();
            if (p != null && !passageirosSelecionados.contains(p)) {
                passageirosSelecionados.add(p);
                modelPassageirosTransfer.addRow(new Object[]{p.getNome()});
            }
        });

        pPass.add(scroll, BorderLayout.CENTER);
        pPass.add(btnAdd, BorderLayout.EAST);
        gbc.gridy++; form.add(pPass, gbc);

        // Seleção de Origem
        comboOrigem = new JComboBox<>();
        gbc.gridy++; form.add(label("Origem (Catálogo):"), gbc);
        gbc.gridy++; form.add(comboOrigem, gbc);

        // Seleção de Destino
        comboDestino = new JComboBox<>();
        gbc.gridy++; form.add(label("Destino (Catálogo):"), gbc);
        gbc.gridy++; form.add(comboDestino, gbc);

        // --- DATA E HORA LADO A LADO ---
        JPanel rowDateTime = new JPanel(new GridLayout(1, 2, 10, 0));
        rowDateTime.setOpaque(false);

        JPanel pData = new JPanel(new BorderLayout(0, 4));
        pData.setOpaque(false);
        dataField = field("Ex: 20/05/2026");
        pData.add(label("Data (dd/mm/aaaa):"), BorderLayout.NORTH);
        pData.add(dataField, BorderLayout.CENTER);

        JPanel pHora = new JPanel(new BorderLayout(0, 4));
        pHora.setOpaque(false);
        horaField = field("Ex: 14:30");
        pHora.add(label("Hora (hh:mm):"), BorderLayout.NORTH);
        pHora.add(horaField, BorderLayout.CENTER);

        rowDateTime.add(pData);
        rowDateTime.add(pHora);

        gbc.gridy++;
        form.add(rowDateTime, gbc);

        carregarCombos();

        // Botões
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("Agendar", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = styledButton("Excluir", DANGER_RED);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirTransfer());

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

    private void carregarCombos() {
        List<PontoColeta> locais = pcService.listarTodos();
        comboOrigem.removeAllItems();
        comboDestino.removeAllItems();
        for (PontoColeta p : locais) {
            comboOrigem.addItem(p);
            comboDestino.addItem(p);
        }
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Origem", "Destino", "Data", "Hora", "Status", "Motorista"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                preencherFormParaEdicao();
            }
        });

        panel.add(new JLabel("Transfers Cadastrados", JLabel.LEFT), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void preencherFormParaEdicao() {
        int row = table.getSelectedRow();
        idSelecionado = (Integer) tableModel.getValueAt(row, 0);
        String origemStr = (String) tableModel.getValueAt(row, 1);
        String destinoStr = (String) tableModel.getValueAt(row, 2);

        // Seleciona no Combo baseado na String da tabela
        selecionarNoCombo(comboOrigem, origemStr);
        selecionarNoCombo(comboDestino, destinoStr);

        dataField.setText((String) tableModel.getValueAt(row, 3));
        horaField.setText((String) tableModel.getValueAt(row, 4));
        salvarButton.setText("Salvar alteração");
        excluirButton.setVisible(true);
    }

    private void selecionarNoCombo(JComboBox<PontoColeta> combo, String nome) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getLocalColeta().equals(nome)) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void salvarOuAtualizar() {
        PontoColeta pOrigem = (PontoColeta) comboOrigem.getSelectedItem();
        PontoColeta pDestino = (PontoColeta) comboDestino.getSelectedItem();

        try {
            Transfer t = new Transfer();
            t.setOrigem(pOrigem.getLocalColeta());
            t.setDestino(pDestino.getLocalColeta());
            t.setDataTransfer(LocalDate.parse(dataField.getText(), DATE_FORMATTER));
            t.setHoraTransfer(LocalTime.parse(horaField.getText(), TIME_FORMATTER));
            t.setStatus(StatusTransfer.AGUARDANDO_OS); // ← adicionar essa linha

            if (idSelecionado == null) service.cadastrar(t);
            else { t.setId(idSelecionado); service.atualizar(t); }

            limparForm();
            carregarTransfers();
            JOptionPane.showMessageDialog(this, "Sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void carregarTransfers() {
        tableModel.setRowCount(0);
        List<Transfer> lista = service.listarTodos();
        for (Transfer t : lista) {
            tableModel.addRow(new Object[]{
                    t.getId(), t.getOrigem(), t.getDestino(),
                    t.getDataTransfer().format(DATE_FORMATTER),
                    t.getHoraTransfer().format(TIME_FORMATTER),
                    t.getStatus(), "Sem OS"
            });
        }
    }

    private void limparForm() {
        idSelecionado = null;
        if (comboOrigem.getItemCount() > 0) comboOrigem.setSelectedIndex(0);
        if (comboDestino.getItemCount() > 0) comboDestino.setSelectedIndex(0);
        dataField.setText("");
        horaField.setText("");
        salvarButton.setText("Agendar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    // Helpers de UI
    private JLabel label(String t) { JLabel l = new JLabel(t); l.setFont(BASE_FONT); l.setForeground(MUTED_TEXT); return l; }
    private JTextField field(String p) {
        JTextField f = new JTextField(); f.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        f.setPreferredSize(new Dimension(0, 30)); return f;
    }
    private JButton styledButton(String t, Color bg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); return b;
    }

    private void excluirTransfer() { /* sua lógica de excluir */ }
}