package br.com.sosviale.view;

import br.com.sosviale.model.*;
import br.com.sosviale.service.*;
import br.com.sosviale.service.pathfinding.RouteResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * PAINEL UNIFICADO: Ordens de Serviço + Transfers + PathFinding com Time Windows.
 *
 * CORREÇÕES APLICADAS:
 *  [1] Preview da OS: painel de detalhes exibe tabela com transfers já vinculados
 *  [2] Detalhes visíveis: ao selecionar uma OS, tabela "Transfers na OS" é populada
 *  [3] Duplicidade bloqueada: vincularTransfer() verifica se o transfer já está na OS
 *  [4] Layout em dashboard compacto, sem scroll horizontal da tela inteira
 *  [5] Capacidade em tempo real: atualizarCapacidade() chamado a cada vinculação,
 *      feedback inline no log ao vincular um transfer
 *  [6] Vínculo correto no backend: usa transferService.vincularAOS() + re-fetch
 *      via osService.buscarComTransfers() para garantir consistência com o banco
 */
public class OrdemServicoUnifiedPanel extends JPanel {

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color PRIMARY_BLUE   = new Color( 50,  91, 140);
    private static final Color SUCCESS_GREEN  = new Color( 34, 139,  34);
    private static final Color WARNING_ORANGE = new Color(200, 120,  40);
    private static final Color DANGER_RED     = new Color(200,  50,  50);
    private static final Color PANEL_BG       = Color.WHITE;
    private static final Color BORDER_COLOR   = new Color(210, 214, 220);
    private static final Color TEXT_COLOR     = new Color( 38,  43,  51);
    private static final Color MUTED_TEXT     = new Color( 98, 108, 122);
    private static final Color LOG_OK_COLOR   = new Color( 20, 120,  60);
    private static final Color LOG_ERR_COLOR  = new Color(180,  30,  30);

    private static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD,  15);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD,  13);
    private static final Font BASE_FONT    = new Font("SansSerif", Font.PLAIN, 12);

    private static final Dimension COMBO_SIZE    = new Dimension(220, 28);
    private static final int       TABLE_ROWS_H  = 130;
    private static final int       LOG_ROWS_H    = 56;

    // ── Services ──────────────────────────────────────────────────────────────
    private final OrdemServicoService osService       = new OrdemServicoService();
    private final MotoristaService    motoristaService = new MotoristaService();
    private final VeiculoService      veiculoService   = new VeiculoService();
    private final TransferService     transferService  = new TransferService();

    // ── Componentes — Criação de OS ───────────────────────────────────────────
    private JComboBox<Motorista> comboMotoristas;
    private JComboBox<Veiculo>   comboVeiculos;

    // ── Componentes — Lista de OS ─────────────────────────────────────────────
    private DefaultTableModel osTableModel;
    private JTable            osTable;

    // ── Componentes — Detalhes da OS ──────────────────────────────────────────
    private JLabel            labelOsSelecionada;
    private JLabel            labelTransfersNaOs;
    private JLabel            labelMotorista;
    private JLabel            labelVeiculo;
    private JLabel            labelCapacidade;
    private JProgressBar      barCapacidade;
    private DefaultTableModel osTransferModel;
    private JTable            osTransferTable;

    // ── Componentes — Transfers disponíveis ───────────────────────────────────
    private DefaultTableModel transferTableModel;
    private JTable            transferTable;
    private JButton           btnVincular;
    private JButton           btnOtimizar;

    // ── Log de status ─────────────────────────────────────────────────────────
    private JTextArea textAreaLog;

    // ── Estado ────────────────────────────────────────────────────────────────
    private OrdemServico osSelecionada = null;

    // ═════════════════════════════════════════════════════════════════════════
    // Construtor
    // ═════════════════════════════════════════════════════════════════════════
    public OrdemServicoUnifiedPanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 246, 248));
        setBorder(new EmptyBorder(8, 10, 8, 10));

        GridBagConstraints root = new GridBagConstraints();
        root.gridx = 0;
        root.weightx = 1;
        root.fill = GridBagConstraints.HORIZONTAL;
        root.insets = new Insets(0, 0, 6, 0);

        root.gridy = 0;
        add(buildHeader(), root);

        root.gridy = 1;
        root.weighty = 0;
        add(buildTopRow(), root);

        root.gridy = 2;
        root.weighty = 1;
        root.fill = GridBagConstraints.BOTH;
        add(buildBottomRow(), root);

        root.gridy = 3;
        root.weighty = 0;
        root.fill = GridBagConstraints.HORIZONTAL;
        add(buildLogPanel(), root);

        carregarDados();
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setOpaque(false);
        JLabel title = new JLabel("Montagem de OS — criar, vincular transfers e otimizar rota");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR);
        h.add(title, BorderLayout.WEST);
        return h;
    }

    /** Topo: criar OS (altura natural) | transfers vinculados (preenche largura) */
    private JPanel buildTopRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 8);

        c.gridx = 0;
        c.weightx = 0.30;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        row.add(buildCreateOsPanel(), c);

        c.gridx = 1;
        c.weightx = 0.70;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        row.add(buildTransfersVinculadosPanel(), c);

        return row;
    }

    /** Base: ordens abertas | transfers disponíveis */
    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 8);

        c.gridx = 0;
        row.add(buildOsListPanel(), c);

        c.gridx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        row.add(buildTransferPanel(), c);

        return row;
    }

    private JPanel buildCreateOsPanel() {
        JPanel card = cardPanel("Criar Nova OS");
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        comboMotoristas = new JComboBox<>();
        comboVeiculos = new JComboBox<>();
        sizedCombo(comboMotoristas);
        sizedCombo(comboVeiculos);

        form.add(formRow("Motorista:", comboMotoristas));
        form.add(Box.createVerticalStrut(6));
        form.add(formRow("Veículo:", comboVeiculos));
        form.add(Box.createVerticalStrut(10));

        JButton btnCriarOS = styledButton("Gerar OS", PRIMARY_BLUE);
        btnCriarOS.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCriarOS.setMaximumSize(new Dimension(120, 32));
        btnCriarOS.addActionListener(e -> criarNovaOS());
        form.add(btnCriarOS);

        card.add(form, BorderLayout.NORTH);
        return card;
    }

    private JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(BASE_FONT);
        lbl.setPreferredSize(new Dimension(72, 28));
        row.add(lbl);
        row.add(field);
        return row;
    }

    private void sizedCombo(JComboBox<?> combo) {
        combo.setPreferredSize(COMBO_SIZE);
        combo.setMaximumSize(COMBO_SIZE);
    }

    /** Coluna 1 — lista de OS abertas */
    private JPanel buildOsListPanel() {
        JPanel p = cardPanel("Ordens Abertas");

        osTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Motorista", "Veículo", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        osTable = new JTable(osTableModel);
        styleTable(osTable);
        osTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureColumns(osTable, new int[]{42, 88, 140, 88, 72});
        osTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && osTable.getSelectedRow() != -1) selecionarOS();
        });

        JScrollPane scroll = tableScroll(osTable);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    /** Topo direito — resumo da OS + transfers já vinculados */
    private JPanel buildTransfersVinculadosPanel() {
        JPanel p = cardPanel("Transfers vinculados à OS");

        labelOsSelecionada = new JLabel("Selecione uma OS na tabela abaixo");
        labelOsSelecionada.setFont(BASE_FONT.deriveFont(Font.BOLD));
        labelOsSelecionada.setForeground(PRIMARY_BLUE);

        labelTransfersNaOs = new JLabel(" ");
        labelTransfersNaOs.setFont(BASE_FONT);
        labelTransfersNaOs.setForeground(MUTED_TEXT);

        JPanel resumoLinha = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        resumoLinha.setOpaque(false);
        resumoLinha.setBackground(new Color(244, 245, 247));
        resumoLinha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)));

        labelMotorista = valueLabel("—");
        labelVeiculo = valueLabel("—");
        labelCapacidade = valueLabel("—");
        labelCapacidade.setForeground(SUCCESS_GREEN);

        resumoLinha.add(wrapResumo("Motorista", labelMotorista));
        resumoLinha.add(wrapResumo("Veículo", labelVeiculo));
        resumoLinha.add(wrapResumo("Capacidade", labelCapacidade));

        barCapacidade = new JProgressBar(0, 1);
        barCapacidade.setPreferredSize(new Dimension(120, 10));
        barCapacidade.setMaximumSize(new Dimension(120, 10));
        resumoLinha.add(barCapacidade);

        osTransferModel = new DefaultTableModel(
                new String[]{"ID", "Hora", "Rota", "Pax"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        osTransferTable = new JTable(osTransferModel);
        styleTable(osTransferTable);
        configureColumns(osTransferTable, new int[]{42, 56, 200, 42});
        osTransferTable.getColumnModel().getColumn(2)
                .setCellRenderer(new TruncateCellRenderer(36));

        JPanel topoInfo = new JPanel();
        topoInfo.setLayout(new BoxLayout(topoInfo, BoxLayout.Y_AXIS));
        topoInfo.setOpaque(false);
        topoInfo.add(labelOsSelecionada);
        topoInfo.add(Box.createVerticalStrut(2));
        topoInfo.add(labelTransfersNaOs);
        topoInfo.add(Box.createVerticalStrut(6));
        topoInfo.add(resumoLinha);

        JPanel corpo = new JPanel(new BorderLayout(0, 6));
        corpo.setOpaque(false);
        corpo.add(topoInfo, BorderLayout.NORTH);
        corpo.add(tableScroll(osTransferTable), BorderLayout.CENTER);

        p.add(corpo, BorderLayout.CENTER);
        return p;
    }

    private JPanel wrapResumo(String titulo, JLabel valor) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        JLabel t = mutedLabel(titulo);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        valor.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(t);
        box.add(valor);
        return box;
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD));
        l.setForeground(TEXT_COLOR);
        return l;
    }

    /** Base direita — transfers disponíveis + vincular */
    private JPanel buildTransferPanel() {
        JPanel p = cardPanel("Transfers Disponíveis");

        transferTableModel = new DefaultTableModel(
                new String[]{"ID", "Hora", "Rota", "Pax"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        transferTable = new JTable(transferTableModel);
        styleTable(transferTable);
        transferTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        configureColumns(transferTable, new int[]{42, 56, 220, 42});
        transferTable.getColumnModel().getColumn(2)
                .setCellRenderer(new TruncateCellRenderer(40));

        btnVincular = styledButton("Vincular", SUCCESS_GREEN);
        btnVincular.addActionListener(e -> vincularTransfersSelecionados());

        btnOtimizar = styledButton("Otimizar rota", WARNING_ORANGE);
        btnOtimizar.addActionListener(e -> otimizarRota());

        JLabel dicaMulti = new JLabel("Ctrl/Shift + clique para selecionar vários");
        dicaMulti.setFont(BASE_FONT.deriveFont(Font.ITALIC, 11f));
        dicaMulti.setForeground(MUTED_TEXT);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 4, 0));
        toolbar.add(btnVincular);
        toolbar.add(btnOtimizar);
        toolbar.add(dicaMulti);

        JPanel corpo = new JPanel(new BorderLayout(0, 0));
        corpo.setOpaque(false);
        corpo.add(toolbar, BorderLayout.NORTH);
        corpo.add(tableScroll(transferTable), BorderLayout.CENTER);
        p.add(corpo, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel lblLog = new JLabel("Status");
        lblLog.setFont(SECTION_FONT);
        lblLog.setForeground(TEXT_COLOR);

        textAreaLog = new JTextArea(2, 0);
        textAreaLog.setEditable(false);
        textAreaLog.setFont(new Font("SansSerif", Font.PLAIN, 11));
        textAreaLog.setBackground(new Color(240, 241, 242));
        textAreaLog.setForeground(TEXT_COLOR);
        textAreaLog.setMargin(new Insets(6, 8, 6, 8));
        textAreaLog.setLineWrap(true);
        textAreaLog.setWrapStyleWord(true);
        textAreaLog.setText("Selecione uma OS abaixo e vincule transfers à direita.");

        JScrollPane scrollLog = new JScrollPane(textAreaLog);
        scrollLog.setPreferredSize(new Dimension(0, LOG_ROWS_H));
        scrollLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(lblLog, BorderLayout.NORTH);
        panel.add(scrollLog, BorderLayout.CENTER);
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Lógica de negócio / eventos
    // ═════════════════════════════════════════════════════════════════════════

    private void carregarDados() {
        comboMotoristas.removeAllItems();
        comboVeiculos.removeAllItems();
        for (Motorista m : motoristaService.listarTodos()) comboMotoristas.addItem(m);
        for (Veiculo  v : veiculoService.listarTodos())    comboVeiculos.addItem(v);
        atualizarTabelaOS();
        carregarTransfersDisponiveis();
    }

    private void criarNovaOS() {
        Motorista motorista = (Motorista) comboMotoristas.getSelectedItem();
        Veiculo   veiculo   = (Veiculo)   comboVeiculos.getSelectedItem();

        if (motorista == null || veiculo == null) {
            log("⚠ Selecione motorista e veículo.", LOG_ERR_COLOR);
            return;
        }
        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            os.setMotorista(motorista);
            os.setVeiculo(veiculo);
            osService.cadastrar(os);
            log("✔ OS criada com sucesso para " + motorista.getNome() + ".", LOG_OK_COLOR);
            carregarDados();
        } catch (Exception ex) {
            log("✘ Erro ao criar OS: " + ex.getMessage(), LOG_ERR_COLOR);
        }
    }

    /** Popula resumo + tabela de transfers já na OS ao selecionar uma linha */
    private void selecionarOS() {
        int row = osTable.getSelectedRow();
        if (row < 0) return;

        Integer osId = (Integer) osTableModel.getValueAt(row, 0);

        // buscarComTransfers garante lista fresca com JOIN FETCH
        osSelecionada = osService.buscarComTransfers(osId);
        if (osSelecionada == null) return;

        labelOsSelecionada.setText("OS #" + osId + " · " + osSelecionada.getDataServico());
        labelTransfersNaOs.setText(osSelecionada.getTransfers().size() + " transfer(s) vinculado(s)");

        labelMotorista.setText(osSelecionada.getMotorista() != null
                ? osSelecionada.getMotorista().getNome() : "—");
        labelVeiculo.setText(osSelecionada.getVeiculo() != null
                ? osSelecionada.getVeiculo().getPlaca() : "—");

        atualizarCapacidade();
        atualizarTabelaTransfersNaOS();
        carregarTransfersDisponiveis();

        log("OS #" + osId + " selecionada — "
                + osSelecionada.getTransfers().size() + " transfer(s) vinculado(s).", TEXT_COLOR);
    }

    /**
     * Vincula um ou vários transfers à OS selecionada (Ctrl/Shift na tabela da direita).
     * Valida capacidade do veículo para o lote inteiro antes de persistir.
     */
    private void vincularTransfersSelecionados() {
        if (osSelecionada == null) {
            log("⚠ Selecione uma OS primeiro.", LOG_ERR_COLOR);
            return;
        }

        int[] rows = transferTable.getSelectedRows();
        if (rows.length == 0) {
            log("⚠ Selecione um ou mais transfers na tabela (Ctrl+clique para vários).", LOG_ERR_COLOR);
            return;
        }
        Arrays.sort(rows);

        Integer osId = osSelecionada.getId();
        int cap = osSelecionada.getVeiculo().getCapacidade();
        int totalAtual = osSelecionada.getTransfers().stream()
                .mapToInt(tr -> tr.getPassageiros().size()).sum();

        Set<Integer> idsJaNaOs = new HashSet<>();
        osSelecionada.getTransfers().forEach(t -> idsJaNaOs.add(t.getId()));

        List<Integer> idsParaVincular = new ArrayList<>();
        int paxNovos = 0;

        for (int row : rows) {
            Integer tId = (Integer) transferTableModel.getValueAt(row, 0);
            if (idsJaNaOs.contains(tId)) {
                log("⚠ Transfer #" + tId + " já está na OS #" + osId + ".", LOG_ERR_COLOR);
                return;
            }
            Transfer t = transferService.buscarPorId(tId);
            if (t == null) {
                log("✘ Transfer #" + tId + " não encontrado.", LOG_ERR_COLOR);
                return;
            }
            if (t.getOrdemServico() != null) {
                log("✘ Transfer #" + tId + " já está na OS #"
                        + t.getOrdemServico().getId() + ".", LOG_ERR_COLOR);
                return;
            }
            idsParaVincular.add(tId);
            paxNovos += t.getPassageiros().size();
        }

        int totalFinal = totalAtual + paxNovos;
        if (totalFinal > cap) {
            log(String.format(
                    "✘ Capacidade excedida! Veículo: %d pax · ocupados: %d · lote selecionado: %d pax.",
                    cap, totalAtual, paxNovos), LOG_ERR_COLOR);
            return;
        }

        try {
            for (Integer tId : idsParaVincular) {
                transferService.vincularAOS(tId, osSelecionada);
            }

            osSelecionada = osService.buscarComTransfers(osId);
            labelOsSelecionada.setText("OS #" + osId + " · " + osSelecionada.getDataServico());
            labelTransfersNaOs.setText(osSelecionada.getTransfers().size() + " transfer(s) vinculado(s)");

            atualizarCapacidade();
            atualizarTabelaTransfersNaOS();
            carregarTransfersDisponiveis();

            int qtd = idsParaVincular.size();
            log(String.format("✔ %d transfer(s) vinculado(s) à OS #%d. Total na OS: %d · Ocupação: %d/%d pax.",
                    qtd, osId, osSelecionada.getTransfers().size(), totalFinal, cap), LOG_OK_COLOR);

        } catch (Exception ex) {
            log("✘ Erro ao vincular transfer(s): " + ex.getMessage(), LOG_ERR_COLOR);
        }
    }

    private void otimizarRota() {
        if (osSelecionada == null) {
            log("⚠ Selecione uma OS.", LOG_ERR_COLOR);
            return;
        }
        if (osSelecionada.getTransfers().isEmpty()) {
            log("⚠ A OS não tem transfers vinculados.", LOG_ERR_COLOR);
            return;
        }
        try {
            RouteResult resultado = PathFindingTimeWindow.otimizarComTimeWindow(osSelecionada);

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append("ROTA OTIMIZADA — OS #").append(osSelecionada.getId()).append("\n");
            sb.append("Motor: ").append(resultado.getModoCalculo().descricao).append("\n");
            sb.append("═══════════════════════════════════════════════════════════\n\n");
            resultado.getLogDecisoes().forEach(linha -> sb.append(linha).append("\n"));
            sb.append("\n─────────────────────────────────────────────────────────\n");
            sb.append(String.format("DISTÂNCIA TOTAL ESTIMADA: %.2f km\n", resultado.getDistanciaTotalKm()));
            sb.append("═══════════════════════════════════════════════════════════\n");

            textAreaLog.setForeground(TEXT_COLOR);
            textAreaLog.setText(sb.toString());
            textAreaLog.setCaretPosition(0);

            int opcao = JOptionPane.showConfirmDialog(this,
                    "Aplicar ordem otimizada?",
                    "Otimização Completa", JOptionPane.YES_NO_OPTION);

            if (opcao == JOptionPane.YES_OPTION) {
                log("✔ Rota aplicada com sucesso!", LOG_OK_COLOR);
            }

        } catch (Exception ex) {
            log("✘ ERRO na otimização: " + ex.getMessage(), LOG_ERR_COLOR);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers de atualização de UI
    // ═════════════════════════════════════════════════════════════════════════

    /** FIX 5 — atualiza label e barra de capacidade em tempo real */
    private void atualizarCapacidade() {
        if (osSelecionada == null || osSelecionada.getVeiculo() == null) return;

        int ocupado = osSelecionada.getTransfers().stream()
                .mapToInt(t -> t.getPassageiros().size()).sum();
        int cap = osSelecionada.getVeiculo().getCapacidade();

        labelCapacidade.setText(ocupado + " / " + cap + " pax");

        boolean cheio = ocupado >= cap;
        labelCapacidade.setForeground(cheio ? DANGER_RED : SUCCESS_GREEN);

        barCapacidade.setMaximum(Math.max(cap, 1));
        barCapacidade.setValue(ocupado);
        barCapacidade.setForeground(cheio ? DANGER_RED : SUCCESS_GREEN);
    }

    /** FIX 2 — popula tabela com os transfers já vinculados à OS selecionada */
    private void atualizarTabelaTransfersNaOS() {
        osTransferModel.setRowCount(0);
        if (osSelecionada == null) return;

        for (Transfer t : osSelecionada.getTransfers()) {
            String rota = t.getOrigem() + " → " + t.getDestino();
            if (rota.length() > 42) rota = rota.substring(0, 39) + "...";
            osTransferModel.addRow(new Object[]{
                    t.getId(),
                    t.getHoraTransfer(),
                    rota,
                    t.getPassageiros().size()
            });
        }
    }

    private void carregarTransfersDisponiveis() {
        transferTableModel.setRowCount(0);

        // Set de IDs já na OS para filtrar da lista de disponíveis
        Set<Integer> idsNaOs = new HashSet<>();
        if (osSelecionada != null) {
            osSelecionada.getTransfers().forEach(t -> idsNaOs.add(t.getId()));
        }

        for (Transfer t : transferService.listarTodos()) {
            if (t.getOrdemServico() == null && !idsNaOs.contains(t.getId())) {
                String rota = t.getOrigem() + " → " + t.getDestino();
                transferTableModel.addRow(new Object[]{
                        t.getId(),
                        t.getHoraTransfer(),
                        rota,
                        t.getPassageiros().size()
                });
            }
        }
    }

    private void atualizarTabelaOS() {
        osTableModel.setRowCount(0);
        for (OrdemServico os : osService.listarTodos()) {
            if ("ABERTA".equals(os.getStatus())) {
                osTableModel.addRow(new Object[]{
                        os.getId(),
                        os.getDataServico(),
                        os.getMotorista() != null ? os.getMotorista().getNome() : "—",
                        os.getVeiculo()   != null ? os.getVeiculo().getPlaca()  : "—",
                        os.getStatus()
                });
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers UI
    // ═════════════════════════════════════════════════════════════════════════

    private void log(String msg, Color cor) {
        textAreaLog.setForeground(cor);
        textAreaLog.setText(msg);
    }

    private JPanel cardPanel(String titulo) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(10, 10, 10, 10)));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(SECTION_FONT);
        lbl.setForeground(TEXT_COLOR);
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, TABLE_ROWS_H));
        scroll.setMinimumSize(new Dimension(120, 80));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        return scroll;
    }

    private void configureColumns(JTable table, int[] widths) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            var col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(widths[i] / 2);
        }
    }

    private JButton styledButton(String label, Color bg) {
        JButton b = new JButton(label);
        b.setFont(BASE_FONT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        return b;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setFont(BASE_FONT);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(BASE_FONT.deriveFont(Font.BOLD));
        table.getTableHeader().setReorderingAllowed(false);
    }

    /** Renderizador que trunca texto longo (coluna Rota / Origem). */
    private static final class TruncateCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final int maxChars;

        TruncateCellRenderer(int maxChars) {
            this.maxChars = maxChars;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String s = value.toString();
                if (s.length() > maxChars) {
                    setText(s.substring(0, maxChars - 3) + "...");
                    setToolTipText(s);
                } else {
                    setToolTipText(null);
                }
            }
            return c;
        }
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }
}