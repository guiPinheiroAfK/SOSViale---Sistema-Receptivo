package br.com.sosviale.view;

import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.Moeda;
import br.com.sosviale.service.PassageiroService;
import br.com.sosviale.service.PontoColetaService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color DANGER_RED = new Color(200, 50, 50);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final TransferService service = new TransferService();
    private final PontoColetaService pcService = new PontoColetaService();
    private final PassageiroService passageiroService = new PassageiroService();
    private final List<Passageiro> passageirosSelecionados = new ArrayList<>();

    private DefaultTableModel tableModel, modelPassageirosTransfer;
    private JTable table, tableMiniPassageiros;
    private JComboBox<PontoColeta> comboOrigem, comboDestino;
    private JComboBox<Moeda> comboMoeda;
    private JTextField valorField, dataField, horaField;
    private JLabel labelConversao, labelPorPessoa;
    private JButton salvarButton, excluirButton, btnAdd; // btnAdd agora é atributo
    private Integer idSelecionado = null;
    private JLabel formTitleLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TransfersPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        carregarTransfers();
        I18nRegistry.register(this::refreshTexts);
    }

    private void refreshTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        if (formTitleLabel != null) formTitleLabel.setText(lm.translate("transfers.title"));
        if (salvarButton != null) {
            salvarButton.setText(idSelecionado == null
                    ? lm.translate("transfers.button.schedule")
                    : lm.translate("transfers.button.edit"));
        }
        if (btnAdd != null) btnAdd.setText(lm.translate("transfers.button.add.passenger"));
        if (tableModel != null) {
            tableModel.setColumnIdentifiers(new String[]{
                    lm.translate("transfers.table.id"),
                    lm.translate("transfers.table.origin"),
                    lm.translate("transfers.table.destiny"),
                    lm.translate("transfers.table.date"),
                    lm.translate("transfers.table.time"),
                    lm.translate("transfers.table.value"),
                    lm.translate("transfers.table.status")
            });
            carregarTransfers();
        }
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BACKGROUND);
        form.setPreferredSize(new Dimension(360, 0));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridx = 0; gbc.gridy = 0;

        formTitleLabel = new JLabel();
        formTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        form.add(formTitleLabel, gbc);

        // --- PAINEL DE PASSAGEIROS NO GRUPO ---
        gbc.gridy++;
        JPanel pPass = new JPanel(new BorderLayout(5, 5));
        pPass.setBackground(PANEL_BACKGROUND);
        pPass.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                " Passageiros no Grupo ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)));

        modelPassageirosTransfer = new DefaultTableModel(new String[]{"Nome"}, 0);
        tableMiniPassageiros = new JTable(modelPassageirosTransfer);
        tableMiniPassageiros.setRowHeight(22);
        tableMiniPassageiros.setShowGrid(false);
        tableMiniPassageiros.setToolTipText("Clique duplo para remover");

        tableMiniPassageiros.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tableMiniPassageiros.getSelectedRow() != -1) {
                    passageirosSelecionados.remove(tableMiniPassageiros.getSelectedRow());
                    atualizarTabelaPassageiros();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableMiniPassageiros);
        scroll.setPreferredSize(new Dimension(0, 95));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        pPass.add(scroll, BorderLayout.CENTER);

        // Botão de Adicionar - Agora abre o menu lateral
        btnAdd = new JButton("+ Adicionar Passageiro");
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAdd.setBackground(new Color(245, 245, 245));
        btnAdd.setForeground(PRIMARY_BLUE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> abrirSeletorPassageiros());
        pPass.add(btnAdd, BorderLayout.SOUTH);

        form.add(pPass, gbc);

        // --- CAMPOS DE DESTINO ---
        gbc.gridy++; form.add(createFieldGroup("Origem:", comboOrigem = new JComboBox<>()), gbc);
        gbc.gridy++; form.add(createFieldGroup("Destino:", comboDestino = new JComboBox<>()), gbc);

        // --- VALORES ---
        gbc.gridy++;
        JPanel rowFin = new JPanel(new GridLayout(1, 2, 10, 0));
        rowFin.setOpaque(false);
        valorField = new JTextField();
        comboMoeda = new JComboBox<>(Moeda.values());
        rowFin.add(createFieldGroup("Valor:", valorField));
        rowFin.add(createFieldGroup("Moeda:", comboMoeda));
        form.add(rowFin, gbc);

        gbc.gridy++;
        JPanel pCalculos = new JPanel(new GridLayout(2, 1, 2, 0));
        pCalculos.setOpaque(false);
        labelConversao = new JLabel("Total convertido: R$ 0,00");
        labelConversao.setForeground(PRIMARY_BLUE);
        labelConversao.setFont(new Font("SansSerif", Font.BOLD, 12));
        labelPorPessoa = new JLabel("P/ Pessoa: R$ 0,00");
        labelPorPessoa.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelPorPessoa.setForeground(Color.GRAY);
        pCalculos.add(labelConversao);
        pCalculos.add(labelPorPessoa);
        form.add(pCalculos, gbc);

        gbc.gridy++;
        JPanel rowDate = new JPanel(new GridLayout(1, 2, 10, 0));
        rowDate.setOpaque(false);
        dataField = new JTextField();
        horaField = new JTextField();
        rowDate.add(createFieldGroup("Data:", dataField));
        rowDate.add(createFieldGroup("Hora:", horaField));
        form.add(rowDate, gbc);

        adicionarListenersFinanceiros();
        carregarCombos();

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("Salvar alteração", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());
        excluirButton = styledButton("Excluir", DANGER_RED);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirTransfer());

        JButton limparBtn = styledButton("Limpar", Color.LIGHT_GRAY);
        limparBtn.setForeground(Color.BLACK);
        limparBtn.addActionListener(e -> limparForm());

        actions.add(salvarButton);
        actions.add(excluirButton);
        actions.add(limparBtn);
        form.add(actions, gbc);

        return form;
    }

    private void abrirSeletorPassageiros() {
        List<Passageiro> todos = passageiroService.listarTodos();

        if (todos == null || todos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum passageiro cadastrado.");
            return;
        }

        // Configuração da Lista do Pop-up
        JList<Passageiro> lista = new JList<>(todos.toArray(new Passageiro[0]));
        lista.setFont(BASE_FONT);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setFixedCellHeight(28);
        lista.setSelectionBackground(PRIMARY_BLUE);
        lista.setSelectionForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(lista);
        scrollPane.setPreferredSize(new Dimension(280, 200));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.add(scrollPane);

        // Ação de Seleção
        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Passageiro selecionado = lista.getSelectedValue();
                if (selecionado != null) {
                    if (passageirosSelecionados.stream().noneMatch(p -> p.getId().equals(selecionado.getId()))) {
                        passageirosSelecionados.add(selecionado);
                        atualizarTabelaPassageiros();
                    }
                    popup.setVisible(false);
                }
            }
        });

        // Posiciona o menu ao lado do botão
        popup.show(btnAdd, btnAdd.getWidth() + 10, -150);
    }

    private void atualizarTabelaPassageiros() {
        modelPassageirosTransfer.setRowCount(0);
        for (Passageiro p : passageirosSelecionados) {
            modelPassageirosTransfer.addRow(new Object[]{p.getNome()});
        }
        atualizarPreview();
    }

    private void atualizarPreview() {
        try {
            String txt = valorField.getText().replace(",", ".");
            if (txt.isEmpty()) {
                labelConversao.setText("Total convertido: R$ 0,00");
                labelPorPessoa.setText("P/ Pessoa: R$ 0,00");
                return;
            }

            BigDecimal val = new BigDecimal(txt);
            Moeda m = (Moeda) comboMoeda.getSelectedItem();

            BigDecimal c = (m == Moeda.USD) ? new BigDecimal("5.00") : (m == Moeda.PYG) ? new BigDecimal("0.00068") : BigDecimal.ONE;
            BigDecimal res = val.multiply(c);

            // Taxas fictícias SOS Viale
            if (m == Moeda.USD) res = res.multiply(new BigDecimal("1.12"));
            if (m == Moeda.PYG) res = res.multiply(new BigDecimal("1.10"));

            BigDecimal totalBRL = res.setScale(2, RoundingMode.HALF_UP);
            labelConversao.setText(String.format("Total convertido: R$ %.2f", totalBRL));

            int qtd = passageirosSelecionados.size();
            if (qtd > 0) {
                BigDecimal porPessoa = totalBRL.divide(BigDecimal.valueOf(qtd), 2, RoundingMode.HALF_UP);
                labelPorPessoa.setText(String.format("P/ Pessoa (%d): R$ %.2f", qtd, porPessoa));
            } else {
                labelPorPessoa.setText("P/ Pessoa: R$ " + totalBRL);
            }
        } catch (Exception e) {
            labelConversao.setText("Valor inválido");
            labelPorPessoa.setText("");
        }
    }

    private void salvarOuAtualizar() {
        try {
            PontoColeta o = (PontoColeta) comboOrigem.getSelectedItem();
            PontoColeta d = (PontoColeta) comboDestino.getSelectedItem();

            if (o == null || d == null) {
                JOptionPane.showMessageDialog(this, "Selecione origem e destino.");
                return;
            }

            Transfer t = (idSelecionado == null) ? new Transfer() : service.buscarPorId(idSelecionado);
            t.setOrigem(o.getLocalColeta());
            t.setDestino(d.getLocalColeta());
            t.setDataTransfer(LocalDate.parse(dataField.getText(), DATE_FORMATTER));
            t.setHoraTransfer(LocalTime.parse(horaField.getText(), TIME_FORMATTER));
            t.setValorOriginal(new BigDecimal(valorField.getText().replace(",", ".")));
            t.setMoedaOrigem((Moeda) comboMoeda.getSelectedItem());
            t.setStatus(StatusTransfer.AGUARDANDO_OS);
            t.setPassageiros(new ArrayList<>(passageirosSelecionados));

            if (idSelecionado == null) service.cadastrar(t);
            else service.atualizar(t);

            limparForm();
            carregarTransfers();
            JOptionPane.showMessageDialog(this, "Agendamento realizado!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void preencherFormParaEdicao() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        Transfer t = service.buscarPorId((Integer) tableModel.getValueAt(row, 0));
        idSelecionado = t.getId();

        selecionarNoCombo(comboOrigem, t.getOrigem());
        selecionarNoCombo(comboDestino, t.getDestino());
        valorField.setText(t.getValorOriginal().toString());
        comboMoeda.setSelectedItem(t.getMoedaOrigem());
        dataField.setText(t.getDataTransfer().format(DATE_FORMATTER));
        horaField.setText(t.getHoraTransfer().format(TIME_FORMATTER));

        passageirosSelecionados.clear();
        if (t.getPassageiros() != null) passageirosSelecionados.addAll(t.getPassageiros());
        atualizarTabelaPassageiros();

        salvarButton.setText("Salvar alteração");
        excluirButton.setVisible(true);
    }

    private void limparForm() {
        idSelecionado = null;
        passageirosSelecionados.clear();
        atualizarTabelaPassageiros();
        valorField.setText("");
        dataField.setText("");
        horaField.setText("");
        salvarButton.setText("Agendar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    private void adicionarListenersFinanceiros() {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarPreview(); }
            public void removeUpdate(DocumentEvent e) { atualizarPreview(); }
            public void changedUpdate(DocumentEvent e) { atualizarPreview(); }
        };
        valorField.getDocument().addDocumentListener(dl);
        comboMoeda.addActionListener(e -> atualizarPreview());
    }

    private JPanel createFieldGroup(String text, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void carregarCombos() {
        List<PontoColeta> locais = pcService.listarTodos();
        comboOrigem.removeAllItems(); comboDestino.removeAllItems();
        for (PontoColeta p : locais) { comboOrigem.addItem(p); comboDestino.addItem(p); }
    }

    private void carregarTransfers() {
        tableModel.setRowCount(0);
        for (Transfer t : service.listarTodos()) {
            tableModel.addRow(new Object[]{t.getId(), t.getOrigem(), t.getDestino(),
                    t.getDataTransfer().format(DATE_FORMATTER), t.getHoraTransfer().format(TIME_FORMATTER),
                    "R$ " + t.getValorBase(),
                    LanguageManager.getInstance().translateStatus(t.getStatus())});
        }
    }

    private JComponent buildTable() {
        tableModel = new DefaultTableModel(new String[]{"ID", "Origem", "Destino", "Data", "Hora", "Valor (R$)", "Status"}, 0);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) preencherFormParaEdicao();
        });
        return new JScrollPane(table);
    }

    private JButton styledButton(String t, Color bg) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(130, 35));
        b.setFocusPainted(false); b.setBorder(BorderFactory.createEmptyBorder());
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void selecionarNoCombo(JComboBox<PontoColeta> combo, String nome) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            PontoColeta p = combo.getItemAt(i);
            if (p != null && p.getLocalColeta().equals(nome)) { combo.setSelectedIndex(i); break; }
        }
    }

    private void excluirTransfer() {
        if (idSelecionado != null && JOptionPane.showConfirmDialog(this, "Excluir este agendamento?") == 0) {
            service.excluir(idSelecionado); limparForm(); carregarTransfers();
        }
    }
}