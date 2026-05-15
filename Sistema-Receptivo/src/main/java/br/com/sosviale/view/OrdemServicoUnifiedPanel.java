package br.com.sosviale.view;

import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.*;
import br.com.sosviale.service.*;
import br.com.sosviale.service.pathfinding.RouteResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class OrdemServicoUnifiedPanel extends JPanel {

    // ───────────────── CORES
    private static final Color PRIMARY_BLUE   = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN  = new Color(34, 139, 34);
    private static final Color WARNING_ORANGE = new Color(200, 120, 40);
    private static final Color DANGER_RED     = new Color(200, 50, 50);

    private static final Color PANEL_BG       = Color.WHITE;
    private static final Color BORDER_COLOR   = new Color(210,214,220);
    private static final Color TEXT_COLOR     = new Color(38,43,51);
    private static final Color MUTED_TEXT     = new Color(98,108,122);

    // ───────────────── FONTES
    private static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD, 16);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font BASE_FONT    = new Font("SansSerif", Font.PLAIN, 12);

    // ───────────────── SERVICES
    private final OrdemServicoService osService = new OrdemServicoService();
    private final MotoristaService motoristaService = new MotoristaService();
    private final VeiculoService veiculoService = new VeiculoService();
    private final TransferService transferService = new TransferService();

    // ───────────────── COMPONENTES
    private JComboBox<Motorista> comboMotoristas;
    private JComboBox<Veiculo> comboVeiculos;

    private JTable osTable;
    private JTable transferTable;
    private JTable osTransferTable;

    private DefaultTableModel osTableModel;
    private DefaultTableModel transferTableModel;
    private DefaultTableModel osTransferModel;

    private JLabel labelOsSelecionada;
    private JLabel labelMotorista;
    private JLabel labelVeiculo;
    private JLabel labelCapacidade;

    private JProgressBar barCapacidade;

    private JTextArea textAreaLog;

    private OrdemServico osSelecionada;
    private JLabel headerTitleLabel;

    // ───────────────── CONSTRUTOR
    public OrdemServicoUnifiedPanel() {

        setLayout(new BorderLayout(10,10));
        setBackground(new Color(245,246,248));
        setBorder(new EmptyBorder(10,10,10,10));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(10));
        content.add(buildTopRow());
        content.add(Box.createVerticalStrut(10));
        content.add(buildBottomRow());

        add(content, BorderLayout.CENTER);

        carregarDados();
        I18nRegistry.register(() -> {
            if (headerTitleLabel != null) {
                headerTitleLabel.setText(LanguageManager.getInstance().translate("ordem.title"));
            }
        });
    }

    // ───────────────── HEADER
    private JPanel buildHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        headerTitleLabel = new JLabel(LanguageManager.getInstance().translate("ordem.title"));
        headerTitleLabel.setFont(TITLE_FONT);
        headerTitleLabel.setForeground(TEXT_COLOR);

        panel.add(headerTitleLabel, BorderLayout.WEST);

        return panel;
    }

    // ───────────────── TOPO
    private JPanel buildTopRow() {

        JPanel row = new JPanel(new GridLayout(1,2,10,0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0,280));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,280));

        row.add(buildCreateOsPanel());
        row.add(buildTransfersVinculadosPanel());

        return row;
    }

    // ───────────────── BASE
    private JPanel buildBottomRow() {

        JPanel row = new JPanel(new GridLayout(1,2,10,0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0,320));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,320));

        row.add(buildOsListPanel());
        row.add(buildTransferPanel());

        return row;
    }

    // ───────────────── CRIAR OS
    private JPanel buildCreateOsPanel() {

        JPanel card = cardPanel("Criar Nova OS");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;

        comboMotoristas = new JComboBox<>();
        comboVeiculos = new JComboBox<>();

        comboMotoristas.setPreferredSize(new Dimension(240,30));
        comboVeiculos.setPreferredSize(new Dimension(240,30));

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Motorista:"), c);

        c.gridx = 1;
        form.add(comboMotoristas, c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Veículo:"), c);

        c.gridx = 1;
        form.add(comboVeiculos, c);

        JButton btnCriarOS = styledButton("Gerar OS", PRIMARY_BLUE);

        c.gridx = 1;
        c.gridy = 2;

        form.add(btnCriarOS, c);

        btnCriarOS.addActionListener(e -> criarNovaOS());

        card.add(form, BorderLayout.CENTER);

        return card;
    }

    // ───────────────── OS VINCULADA
    private JPanel buildTransfersVinculadosPanel() {

        JPanel card = cardPanel("Transfers vinculados à OS");

        JPanel content = new JPanel(new BorderLayout(0,10));
        content.setOpaque(false);

        labelOsSelecionada = new JLabel("Selecione uma OS");
        labelOsSelecionada.setFont(new Font("SansSerif", Font.BOLD, 14));
        labelOsSelecionada.setForeground(PRIMARY_BLUE);

        JPanel topo = new JPanel(new GridLayout(1,4,10,0));
        topo.setOpaque(false);

        labelMotorista = valueLabel("—");
        labelVeiculo = valueLabel("—");
        labelCapacidade = valueLabel("0 / 0");

        topo.add(wrapResumo("Motorista", labelMotorista));
        topo.add(wrapResumo("Veículo", labelVeiculo));
        topo.add(wrapResumo("Capacidade", labelCapacidade));

        barCapacidade = new JProgressBar();
        topo.add(barCapacidade);

        osTransferModel = new DefaultTableModel(
                new String[]{"ID","Hora","Rota","Pax"},0
        ){
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        osTransferTable = new JTable(osTransferModel);

        styleTable(osTransferTable);

        JScrollPane scroll = tableScroll(osTransferTable);

        JPanel topWrapper = new JPanel(new BorderLayout(0,10));
        topWrapper.setOpaque(false);

        topWrapper.add(labelOsSelecionada, BorderLayout.NORTH);
        topWrapper.add(topo, BorderLayout.CENTER);

        content.add(topWrapper, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    // ───────────────── LISTA OS
    private JPanel buildOsListPanel() {

        JPanel card = cardPanel("Ordens Abertas");

        osTableModel = new DefaultTableModel(
                new String[]{"ID","Data","Motorista","Veículo","Status"},0
        ){
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        osTable = new JTable(osTableModel);

        styleTable(osTable);

        osTable.getSelectionModel().addListSelectionListener(e -> {

            if(!e.getValueIsAdjusting()) {
                selecionarOS();
            }
        });

        card.add(tableScroll(osTable), BorderLayout.CENTER);

        return card;
    }

    // ───────────────── TRANSFERS
    private JPanel buildTransferPanel() {

        JPanel card = cardPanel("Transfers Disponíveis");

        JPanel content = new JPanel(new BorderLayout(0,10));
        content.setOpaque(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.setOpaque(false);

        JButton btnVincular = styledButton("Vincular", SUCCESS_GREEN);
        JButton btnOtimizar = styledButton("Otimizar rota", WARNING_ORANGE);

        btnVincular.addActionListener(e -> vincularTransfersSelecionados());
        btnOtimizar.addActionListener(e -> otimizarRota());

        toolbar.add(btnVincular);
        toolbar.add(btnOtimizar);

        transferTableModel = new DefaultTableModel(
                new String[]{"ID","Hora","Rota","Pax"},0
        ){
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        transferTable = new JTable(transferTableModel);

        styleTable(transferTable);

        content.add(toolbar, BorderLayout.NORTH);
        content.add(tableScroll(transferTable), BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);

        return card;
    }


    // ───────────────── DADOS
    private void carregarDados() {

        comboMotoristas.removeAllItems();
        comboVeiculos.removeAllItems();

        for(Motorista m : motoristaService.listarTodos()) {
            comboMotoristas.addItem(m);
        }

        for(Veiculo v : veiculoService.listarTodos()) {
            comboVeiculos.addItem(v);
        }

        atualizarTabelaOS();
        carregarTransfersDisponiveis();
    }

    private void criarNovaOS() {

        try {

            Motorista motorista = (Motorista) comboMotoristas.getSelectedItem();
            Veiculo veiculo = (Veiculo) comboVeiculos.getSelectedItem();

            OrdemServico os = new OrdemServico();

            os.setDataServico(LocalDate.now());
            os.setMotorista(motorista);
            os.setVeiculo(veiculo);

            osService.cadastrar(os);

            carregarDados();

            log("OS criada com sucesso.");

        } catch (Exception ex) {

            log("Erro ao criar OS.");
        }
    }

    private void selecionarOS() {

        int row = osTable.getSelectedRow();

        if(row == -1) return;

        Integer osId = (Integer) osTableModel.getValueAt(row,0);

        osSelecionada = osService.buscarComTransfers(osId);

        if(osSelecionada == null) return;

        labelOsSelecionada.setText(
                "OS #" + osSelecionada.getId()
        );

        labelMotorista.setText(
                osSelecionada.getMotorista().getNome()
        );

        labelVeiculo.setText(
                osSelecionada.getVeiculo().getPlaca()
        );

        atualizarCapacidade();
        atualizarTabelaTransfersNaOS();
        carregarTransfersDisponiveis();
    }

    private void vincularTransfersSelecionados() {

        if(osSelecionada == null) {

            log("Selecione uma OS.");
            return;
        }

        int[] rows = transferTable.getSelectedRows();

        if(rows.length == 0) {

            log("Selecione um transfer.");
            return;
        }

        try {

            for(int row : rows) {

                Integer transferId = (Integer)
                        transferTableModel.getValueAt(row,0);

                Transfer t = transferService.buscarPorId(transferId);

                transferService.vincularAOS(transferId, osSelecionada);
            }

            osSelecionada =
                    osService.buscarComTransfers(osSelecionada.getId());

            atualizarTabelaTransfersNaOS();
            carregarTransfersDisponiveis();
            atualizarCapacidade();

            log("Transfer(s) vinculado(s).");

        } catch (Exception ex) {

            log("Erro ao vincular transfer.");
        }
    }

    private void otimizarRota() {

        if(osSelecionada == null) {

            log("Selecione uma OS.");
            return;
        }

        try {

            RouteResult resultado =
                    PathFindingTimeWindow.otimizarComTimeWindow(osSelecionada);

            textAreaLog.setText(
                    "ROTA OTIMIZADA\n\n" +
                            "Distância: " +
                            resultado.getDistanciaTotalKm() +
                            " km"
            );

        } catch (Exception ex) {

            log("Erro na otimização.");
        }
    }

    private void atualizarTabelaOS() {

        osTableModel.setRowCount(0);

        for(OrdemServico os : osService.listarTodos()) {

            if("ABERTA".equals(os.getStatus())) {

                osTableModel.addRow(new Object[]{
                        os.getId(),
                        os.getDataServico(),
                        os.getMotorista() != null
                                ? os.getMotorista().getNome()
                                : "—",
                        os.getVeiculo() != null
                                ? os.getVeiculo().getPlaca()
                                : "—",
                        os.getStatus()
                });
            }
        }
    }

    private void atualizarTabelaTransfersNaOS() {

        osTransferModel.setRowCount(0);

        if(osSelecionada == null) return;

        for(Transfer t : osSelecionada.getTransfers()) {

            osTransferModel.addRow(new Object[]{
                    t.getId(),
                    t.getHoraTransfer(),
                    t.getOrigem() + " → " + t.getDestino(),
                    t.getPassageiros().size()
            });
        }
    }

    private void carregarTransfersDisponiveis() {

        transferTableModel.setRowCount(0);

        for(Transfer t : transferService.listarTodos()) {

            if(t.getOrdemServico() == null) {

                transferTableModel.addRow(new Object[]{
                        t.getId(),
                        t.getHoraTransfer(),
                        t.getOrigem() + " → " + t.getDestino(),
                        t.getPassageiros().size()
                });
            }
        }
    }

    private void atualizarCapacidade() {

        if(osSelecionada == null) return;

        int ocupado = osSelecionada.getTransfers()
                .stream()
                .mapToInt(t -> t.getPassageiros().size())
                .sum();

        int cap = osSelecionada.getVeiculo().getCapacidade();

        labelCapacidade.setText(ocupado + " / " + cap);

        barCapacidade.setMaximum(cap);
        barCapacidade.setValue(ocupado);

        barCapacidade.setForeground(
                ocupado >= cap
                        ? DANGER_RED
                        : SUCCESS_GREEN
        );
    }

    // ───────────────── HELPERS
    private JPanel cardPanel(String titulo) {

        JPanel panel = new JPanel(new BorderLayout(0,10));

        panel.setBackground(PANEL_BG);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12,12,12,12)
        ));

        JLabel lbl = new JLabel(titulo);

        lbl.setFont(SECTION_FONT);
        lbl.setForeground(TEXT_COLOR);

        panel.add(lbl, BorderLayout.NORTH);

        return panel;
    }

    private JScrollPane tableScroll(JTable table) {

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        scroll.getViewport().setBackground(Color.WHITE);

        return scroll;
    }

    private JButton styledButton(String text, Color bg) {

        JButton btn = new JButton(text);

        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);

        btn.setFocusPainted(false);

        btn.setFont(BASE_FONT);

        btn.setBorder(new EmptyBorder(8,14,8,14));

        return btn;
    }

    private void styleTable(JTable table) {

        table.setRowHeight(28);

        table.setFont(BASE_FONT);

        table.setShowVerticalLines(false);

        table.setGridColor(new Color(235,235,235));

        table.setSelectionBackground(new Color(220,235,252));

        table.setSelectionForeground(TEXT_COLOR);

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("SansSerif", Font.BOLD, 12));

        header.setBackground(new Color(245,246,248));

        header.setForeground(TEXT_COLOR);

        header.setReorderingAllowed(false);

        header.setPreferredSize(new Dimension(0,30));
    }

    private JLabel valueLabel(String text) {

        JLabel lbl = new JLabel(text);

        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));

        lbl.setForeground(TEXT_COLOR);

        return lbl;
    }

    private JPanel wrapResumo(String titulo, JLabel valor) {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(titulo);

        t.setFont(BASE_FONT);
        t.setForeground(MUTED_TEXT);

        panel.add(t);
        panel.add(valor);

        return panel;
    }

    private void log(String msg) {

        System.out.println(msg);

        JOptionPane.showMessageDialog(
                this,
                msg,
                "SOS VIALE",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

}
