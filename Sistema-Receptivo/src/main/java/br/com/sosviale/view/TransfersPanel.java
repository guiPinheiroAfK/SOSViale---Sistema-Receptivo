package br.com.sosviale.view;

import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.Moeda;
import br.com.sosviale.service.PontoColetaService;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private JComboBox<Moeda> comboMoeda;
    private JTextField valorField;
    private JLabel labelConversao;
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
        carregarTransfers();
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

        // --- SEÇÃO DE PASSAGEIROS ---
        gbc.gridy++;
        form.add(label("Passageiros no Grupo:"), gbc);

        JPanel pPass = new JPanel(new BorderLayout(10, 0));
        pPass.setOpaque(false);

        modelPassageirosTransfer = new DefaultTableModel(new String[]{"Nome"}, 0);
        JTable miniTable = new JTable(modelPassageirosTransfer);
        JScrollPane scroll = new JScrollPane(miniTable);
        scroll.setPreferredSize(new Dimension(0, 80));

        JButton btnAdd = styledButton("+", PRIMARY_BLUE);
        btnAdd.setPreferredSize(new Dimension(40, 40));
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnAdd.addActionListener(e -> abrirSeletorPassageiros());

        JPanel btnWrapper = new JPanel(new GridBagLayout());
        btnWrapper.setOpaque(false);
        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.anchor = GridBagConstraints.NORTH;
        gbcBtn.weighty = 1.0;
        btnWrapper.add(btnAdd, gbcBtn);

        pPass.add(scroll, BorderLayout.CENTER);
        pPass.add(btnWrapper, BorderLayout.EAST);

        gbc.gridy++; form.add(pPass, gbc);

        // --- CAMPOS DE ORIGEM / DESTINO ---
        comboOrigem = new JComboBox<>();
        gbc.gridy++; form.add(label("Origem:"), gbc);
        gbc.gridy++; form.add(comboOrigem, gbc);

        comboDestino = new JComboBox<>();
        gbc.gridy++; form.add(label("Destino:"), gbc);
        gbc.gridy++; form.add(comboDestino, gbc);

        // --- FINANCEIRO ---
        JPanel rowFinanceiro = new JPanel(new GridBagLayout());
        rowFinanceiro.setOpaque(false);
        GridBagConstraints gbcFin = new GridBagConstraints();
        gbcFin.fill = GridBagConstraints.HORIZONTAL;

        valorField = field("0.00");
        gbcFin.weightx = 0.6; gbcFin.gridx = 0; gbcFin.insets = new Insets(0, 0, 0, 5);
        rowFinanceiro.add(createFieldGroup("Valor:", valorField), gbcFin);

        comboMoeda = new JComboBox<>(Moeda.values());
        gbcFin.weightx = 0.4; gbcFin.gridx = 1; gbcFin.insets = new Insets(0, 5, 0, 0);
        rowFinanceiro.add(createFieldGroup("Moeda:", comboMoeda), gbcFin);

        gbc.gridy++; form.add(rowFinanceiro, gbc);

        labelConversao = new JLabel("Total convertido: R$ 0,00");
        labelConversao.setFont(new Font("SansSerif", Font.ITALIC, 12));
        labelConversao.setForeground(PRIMARY_BLUE);
        gbc.gridy++; form.add(labelConversao, gbc);

        // --- DATA E HORA ---
        JPanel rowDateTime = new JPanel(new GridLayout(1, 2, 10, 0));
        rowDateTime.setOpaque(false);
        dataField = field("dd/mm/aaaa");
        horaField = field("hh:mm");
        rowDateTime.add(createFieldGroup("Data:", dataField));
        rowDateTime.add(createFieldGroup("Hora:", horaField));

        gbc.gridy++; form.add(rowDateTime, gbc);

        adicionarListenersFinanceiros();
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

        gbc.gridy = 99; gbc.weighty = 1; gbc.anchor = GridBagConstraints.SOUTHWEST;
        form.add(actions, gbc);

        return form;
    }

    private void abrirSeletorPassageiros() {
        // Aqui você deve chamar o seu JDialog/Tela de seleção de passageiros
        // Exemplo hipotético de como você adicionaria à lista:
        // Passageiro p = Seletor.show();
        // if(p != null) { passageirosSelecionados.add(p); atualizarTabelaPassageiros(); }
        JOptionPane.showMessageDialog(this, "Integrar com SeletorPassageiroDialog");
    }

    private void atualizarTabelaPassageiros() {
        modelPassageirosTransfer.setRowCount(0);
        for (Passageiro p : passageirosSelecionados) {
            modelPassageirosTransfer.addRow(new Object[]{p.getNome()});
        }
    }

    private void salvarOuAtualizar() {
        try {
            Transfer t = (idSelecionado == null) ? new Transfer() : service.buscarPorId(idSelecionado);
            t.setOrigem(((PontoColeta) comboOrigem.getSelectedItem()).getLocalColeta());
            t.setDestino(((PontoColeta) comboDestino.getSelectedItem()).getLocalColeta());
            t.setDataTransfer(LocalDate.parse(dataField.getText(), DATE_FORMATTER));
            t.setHoraTransfer(LocalTime.parse(horaField.getText(), TIME_FORMATTER));
            t.setValorOriginal(new BigDecimal(valorField.getText().replace(",", ".")));
            t.setMoedaOrigem((Moeda) comboMoeda.getSelectedItem());
            t.setPassageiros(new ArrayList<>(passageirosSelecionados)); // Vincula os passageiros
            t.setStatus(StatusTransfer.AGUARDANDO_OS);

            if (idSelecionado == null) service.cadastrar(t);
            else service.atualizar(t);

            limparForm();
            carregarTransfers();
            JOptionPane.showMessageDialog(this, "Sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void preencherFormParaEdicao() {
        int row = table.getSelectedRow();
        Transfer t = service.buscarPorId((Integer) tableModel.getValueAt(row, 0));
        idSelecionado = t.getId();

        // Preenche campos básicos
        selecionarNoCombo(comboOrigem, t.getOrigem());
        selecionarNoCombo(comboDestino, t.getDestino());
        valorField.setText(t.getValorOriginal().toString());
        comboMoeda.setSelectedItem(t.getMoedaOrigem());
        dataField.setText(t.getDataTransfer().format(DATE_FORMATTER));
        horaField.setText(t.getHoraTransfer().format(TIME_FORMATTER));

        // Preenche passageiros
        passageirosSelecionados.clear();
        passageirosSelecionados.addAll(t.getPassageiros());
        atualizarTabelaPassageiros();

        salvarButton.setText("Salvar alteração");
        excluirButton.setVisible(true);
        atualizarPreview();
    }

    private void limparForm() {
        idSelecionado = null;
        passageirosSelecionados.clear();
        atualizarTabelaPassageiros();
        valorField.setText("");
        comboMoeda.setSelectedIndex(0);
        dataField.setText("");
        horaField.setText("");
        salvarButton.setText("Agendar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    // --- MÉTODOS AUXILIARES (IGUAIS AOS ANTERIORES) ---

    private void adicionarListenersFinanceiros() {
        valorField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarPreview(); }
            public void removeUpdate(DocumentEvent e) { atualizarPreview(); }
            public void changedUpdate(DocumentEvent e) { atualizarPreview(); }
        });
        comboMoeda.addActionListener(e -> atualizarPreview());
    }

    private void atualizarPreview() {
        try {
            String texto = valorField.getText().replace(",", ".");
            if (texto.isEmpty()) { labelConversao.setText("Total convertido: R$ 0,00"); return; }
            BigDecimal valor = new BigDecimal(texto);
            Moeda moeda = (Moeda) comboMoeda.getSelectedItem();

            // Lógica de cálculo (mesma do Service)
            BigDecimal cambio = switch (moeda) {
                case USD -> new BigDecimal("5.00");
                case PYG -> new BigDecimal("0.00068");
                default -> BigDecimal.ONE;
            };
            BigDecimal base = valor.multiply(cambio);
            if (moeda == Moeda.USD) base = base.multiply(new BigDecimal("1.12"));
            if (moeda == Moeda.PYG) base = base.multiply(new BigDecimal("1.10"));

            labelConversao.setText(String.format("Total convertido: R$ %.2f", base.setScale(2, RoundingMode.HALF_UP)));
        } catch (Exception e) { labelConversao.setText("Valor inválido"); }
    }

    private JComponent createFieldGroup(String lbl, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(label(lbl), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void carregarTransfers() {
        tableModel.setRowCount(0);
        List<Transfer> lista = service.listarTodos();
        for (Transfer t : lista) {
            tableModel.addRow(new Object[]{
                    t.getId(), t.getOrigem(), t.getDestino(),
                    t.getDataTransfer().format(DATE_FORMATTER),
                    t.getHoraTransfer().format(TIME_FORMATTER),
                    String.format("R$ %.2f", t.getValorBase()),
                    t.getStatus()
            });
        }
    }

    private void carregarCombos() {
        List<PontoColeta> locais = pcService.listarTodos();
        comboOrigem.removeAllItems(); comboDestino.removeAllItems();
        for (PontoColeta p : locais) {
            comboOrigem.addItem(p); comboDestino.addItem(p);
        }
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(14, 14, 14, 14)));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Origem", "Destino", "Data", "Hora", "Valor (R$)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) preencherFormParaEdicao();
        });

        panel.add(new JLabel("Transfers Cadastrados", JLabel.LEFT), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void selecionarNoCombo(JComboBox<PontoColeta> combo, String nome) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getLocalColeta().equals(nome)) {
                combo.setSelectedIndex(i); break;
            }
        }
    }

    private JLabel label(String t) { JLabel l = new JLabel(t); l.setFont(BASE_FONT); l.setForeground(MUTED_TEXT); return l; }
    private JTextField field(String p) {
        JTextField f = new JTextField(); f.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        f.setPreferredSize(new Dimension(0, 30)); return f;
    }
    private JButton styledButton(String t, Color bg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); return b;
    }

    private void excluirTransfer() {
        if (idSelecionado != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Excluir transfer?");
            if (confirm == JOptionPane.YES_OPTION) {
                service.excluir(idSelecionado);
                limparForm(); carregarTransfers();
            }
        }
    }
}