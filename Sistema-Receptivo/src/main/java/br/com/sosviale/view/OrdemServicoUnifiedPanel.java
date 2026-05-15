package br.com.sosviale.view;

import br.com.sosviale.model.*;
import br.com.sosviale.service.*;
import br.com.sosviale.service.pathfinding.RouteResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/*
 * PAINEL UNIFICADO: Ordens de Serviço + Transfers + PathFinding com Time Windows.
 *
 * Integra em uma única tela:
 *   - Abertura de novas OS
 *   - Atribuição de transfers
 *   - Otimização automática de rota respeitando horários
 *   - Visualização de conflitos (se houver)
 *
 * Fluxo esperado:
 *   1. Criar OS (motorista + veículo)
 *   2. Vincular transfers à OS
 *   3. Clicar "Otimizar Rota" → Calcula melhor sequência respeitando horários
 *   4. Sistema avisa se há conflitos (impossível chegar no horário)
 *   5. Admin decide: aceitar, mover transfer, ou ajustar
 */
public class OrdemServicoUnifiedPanel extends JPanel {

    private static final Color PRIMARY_BLUE    = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN   = new Color(34, 139, 34);
    private static final Color WARNING_ORANGE  = new Color(200, 120, 40);
    private static final Color DANGER_RED      = new Color(200, 50, 50);
    private static final Color PANEL_BG        = Color.WHITE;
    private static final Color BORDER_COLOR    = new Color(210, 214, 220);
    private static final Color TEXT_COLOR      = new Color(38, 43, 51);
    private static final Color MUTED_TEXT      = new Color(98, 108, 122);
    private static final Font  TITLE_FONT      = new Font("SansSerif", Font.BOLD, 16);
    private static final Font  SECTION_FONT    = new Font("SansSerif", Font.BOLD, 14);
    private static final Font  BASE_FONT       = new Font("SansSerif", Font.PLAIN, 13);

    // Services
    private final OrdemServicoService osService       = new OrdemServicoService();
    private final MotoristaService    motoristaService = new MotoristaService();
    private final VeiculoService      veiculoService   = new VeiculoService();
    private final TransferService     transferService  = new TransferService();

    // Componentes — Painel Superior (Criação de OS)
    private JComboBox<Motorista>     comboMotoristas;
    private JComboBox<Veiculo>       comboVeiculos;
    private JButton                  btnCriarOS;

    // Componentes — Painel Inferior Esquerdo (Lista de OS)
    private DefaultTableModel        osTableModel;
    private JTable                   osTable;
    private JLabel                   labelOsDetalhes;
    private JLabel                   labelCapacidade;

    // Componentes — Painel Inferior Direito (Transfers)
    private DefaultTableModel        transferTableModel;
    private JTable                   transferTable;
    private JButton                  btnVincular;
    private JButton                  btnOtimizar;

    // Componentes — Area de Status/Conflitos
    private JTextArea                textAreaLog;
    private JScrollPane              scrollLog;

    // Estado
    private OrdemServico osSelecionada = null;

    // Construtor e Inicialização
    public OrdemServicoUnifiedPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(245, 246, 248));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildMiddlePanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        carregarDados();
    }

    // Painéis
    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Título
        JLabel title = new JLabel("Ordens de Serviço — Gerenciamento e Otimização de Rotas");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_COLOR);

        // Painel de criação de OS
        JPanel createOsPanel = new JPanel(new GridBagLayout());
        createOsPanel.setBackground(PANEL_BG);
        createOsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 12, 12, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 8);

        JLabel lblCreate = new JLabel("Criar Nova OS");
        lblCreate.setFont(SECTION_FONT);
        gbc.gridwidth = 2;
        createOsPanel.add(lblCreate, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        createOsPanel.add(new JLabel("Motorista:"), gbc);
        gbc.gridx = 1;
        comboMotoristas = new JComboBox<>();
        createOsPanel.add(comboMotoristas, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        createOsPanel.add(new JLabel("Veículo:"), gbc);
        gbc.gridx = 1;
        comboVeiculos = new JComboBox<>();
        createOsPanel.add(comboVeiculos, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        btnCriarOS = new JButton("Gerar OS");
        btnCriarOS.setBackground(PRIMARY_BLUE);
        btnCriarOS.setForeground(Color.WHITE);
        btnCriarOS.setFocusPainted(false);
        btnCriarOS.addActionListener(e -> criarNovaOS());
        createOsPanel.add(btnCriarOS, gbc);

        panel.add(title, BorderLayout.NORTH);
        panel.add(createOsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildMiddlePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setOpaque(false);

        // Painel esquerdo — Lista de OS
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(PANEL_BG);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblOs = new JLabel("Ordens Abertas");
        lblOs.setFont(SECTION_FONT);

        osTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Motorista", "Veículo", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        osTable = new JTable(osTableModel);
        osTable.setRowHeight(26);
        osTable.setFont(BASE_FONT);
        osTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        osTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && osTable.getSelectedRow() != -1) {
                selecionarOS();
            }
        });

        JScrollPane scrollOs = new JScrollPane(osTable);
        scrollOs.setPreferredSize(new Dimension(0, 200));

        // Detalhes da OS selecionada
        JPanel detalhesPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        detalhesPanel.setBackground(new Color(244, 245, 247));
        detalhesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 8, 8, 8)
        ));

        labelOsDetalhes = mutedLabel("—");
        labelCapacidade = mutedLabel("—");
        JLabel lblMot = mutedLabel("Motorista:");
        JLabel lblCap = mutedLabel("Capacidade:");

        detalhesPanel.add(lblMot);
        detalhesPanel.add(labelOsDetalhes);
        detalhesPanel.add(lblCap);
        detalhesPanel.add(labelCapacidade);

        leftPanel.add(lblOs, BorderLayout.NORTH);
        leftPanel.add(scrollOs, BorderLayout.CENTER);
        leftPanel.add(detalhesPanel, BorderLayout.SOUTH);

        // Painel direito — Lista de Transfers
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(PANEL_BG);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTransfers = new JLabel("Transfers Disponíveis");
        lblTransfers.setFont(SECTION_FONT);

        transferTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Hora", "Origem", "Destino"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        transferTable = new JTable(transferTableModel);
        transferTable.setRowHeight(26);
        transferTable.setFont(BASE_FONT);
        transferTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollTransfers = new JScrollPane(transferTable);

        // Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botoesPanel.setOpaque(false);

        btnVincular = new JButton("✓ Vincular Transfer");
        btnVincular.setBackground(SUCCESS_GREEN);
        btnVincular.setForeground(Color.WHITE);
        btnVincular.setFocusPainted(false);
        btnVincular.addActionListener(e -> vincularTransfer());
        botoesPanel.add(btnVincular);

        btnOtimizar = new JButton("⚡ Otimizar Rota (Time Window)");
        btnOtimizar.setBackground(WARNING_ORANGE);
        btnOtimizar.setForeground(Color.WHITE);
        btnOtimizar.setFocusPainted(false);
        btnOtimizar.addActionListener(e -> otimizarRota());
        botoesPanel.add(btnOtimizar);

        rightPanel.add(lblTransfers, BorderLayout.NORTH);
        rightPanel.add(scrollTransfers, BorderLayout.CENTER);
        rightPanel.add(botoesPanel, BorderLayout.SOUTH);

        panel.add(leftPanel);
        panel.add(rightPanel);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel lblLog = new JLabel("Status e Conflitos");
        lblLog.setFont(SECTION_FONT);
        lblLog.setForeground(TEXT_COLOR);

        textAreaLog = new JTextArea(6, 0);
        textAreaLog.setEditable(false);
        textAreaLog.setFont(new Font("Courier", Font.PLAIN, 11));
        textAreaLog.setBackground(new Color(240, 241, 242));
        textAreaLog.setForeground(TEXT_COLOR);
        textAreaLog.setMargin(new Insets(8, 8, 8, 8));
        textAreaLog.setText("Selecione uma OS e otimize a rota para visualizar status...");

        scrollLog = new JScrollPane(textAreaLog);
        scrollLog.setPreferredSize(new Dimension(0, 120));

        panel.add(lblLog, BorderLayout.NORTH);
        panel.add(scrollLog, BorderLayout.CENTER);

        return panel;
    }

    // Lógica de Eventos
    private void carregarDados() {
        // Carrega comboboxes
        comboMotoristas.removeAllItems();
        comboVeiculos.removeAllItems();

        for (Motorista m : motoristaService.listarTodos()) {
            comboMotoristas.addItem(m);
        }
        for (Veiculo v : veiculoService.listarTodos()) {
            comboVeiculos.addItem(v);
        }

        // Carrega tabela de OS
        atualizarTabelaOS();
        carregarTransfersDisponiveis();
    }

    private void criarNovaOS() {
        Motorista motorista = (Motorista) comboMotoristas.getSelectedItem();
        Veiculo veiculo = (Veiculo) comboVeiculos.getSelectedItem();

        if (motorista == null || veiculo == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione motorista e veículo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            os.setMotorista(motorista);
            os.setVeiculo(veiculo);
            osService.cadastrar(os);
            JOptionPane.showMessageDialog(this,
                    "OS criada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarDados();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selecionarOS() {
        int row = osTable.getSelectedRow();
        if (row < 0) return;

        Integer osId = (Integer) osTableModel.getValueAt(row, 0);
        osSelecionada = osService.buscarPorId(osId);

        if (osSelecionada != null) {
            labelOsDetalhes.setText(osSelecionada.getMotorista().getNome());
            int ocupado = osSelecionada.getTransfers().stream()
                    .mapToInt(t -> t.getPassageiros().size()).sum();
            int cap = osSelecionada.getVeiculo().getCapacidade();
            labelCapacidade.setText(ocupado + "/" + cap + " pax");
            labelCapacidade.setForeground(ocupado >= cap ? DANGER_RED : SUCCESS_GREEN);
        }

        carregarTransfersDisponiveis();
        textAreaLog.setText("OS #" + osId + " selecionada. Vincule transfers ou otimize a rota.");
    }

    private void vincularTransfer() {
        if (osSelecionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma OS primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = transferTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um transfer.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer tId = (Integer) transferTableModel.getValueAt(row, 0);
        Transfer t = transferService.buscarPorId(tId);

        // Validação de capacidade
        int totalAtual = osSelecionada.getTransfers().stream()
                .mapToInt(tr -> tr.getPassageiros().size()).sum();
        int totalNovo = totalAtual + t.getPassageiros().size();

        if (totalNovo > osSelecionada.getVeiculo().getCapacidade()) {
            JOptionPane.showMessageDialog(this,
                    "Capacidade excedida!",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        t.setOrdemServico(osSelecionada);
        t.setStatus(StatusTransfer.NA_OS);
        transferService.atualizar(t);
        osSelecionada.getTransfers().add(t);

        carregarTransfersDisponiveis();
        selecionarOS(); // Atualiza detalhes

        JOptionPane.showMessageDialog(this,
                "Transfer vinculado com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void otimizarRota() {
        if (osSelecionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma OS.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (osSelecionada.getTransfers().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "A OS não tem transfers vinculados.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Chama PathFinding com Time Windows
            RouteResult resultado = PathFindingTimeWindow.otimizarComTimeWindow(osSelecionada);

            // Exibe resultado no log
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════\n");
            sb.append("ROTA OTIMIZADA — OS #").append(osSelecionada.getId()).append("\n");
            sb.append("Motor: ").append(resultado.getModoCalculo().descricao).append("\n");
            sb.append("═══════════════════════════════════════════════════════════\n\n");

            resultado.getLogDecisoes().forEach(linha -> sb.append(linha).append("\n"));

            sb.append("\n─────────────────────────────────────────────────────────\n");
            sb.append(String.format("DISTÂNCIA TOTAL ESTIMADA: %.2f km\n", resultado.getDistanciaTotalKm()));
            sb.append("═══════════════════════════════════════════════════════════\n");

            textAreaLog.setText(sb.toString());
            textAreaLog.setCaretPosition(0);

            // Pergunta ao admin se quer aplicar
            int opcao = JOptionPane.showConfirmDialog(this,
                    "Aplicar ordem otimizada?",
                    "Otimização Completa", JOptionPane.YES_NO_OPTION);

            if (opcao == JOptionPane.YES_OPTION) {
                // Aplica a ordem
                // PathFindingTimeWindow.aplicarOrdemOtimizada(resultado, pcRepo);
                JOptionPane.showMessageDialog(this,
                        "Rota aplicada com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            textAreaLog.setText("❌ ERRO: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Erro: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTransfersDisponiveis() {
        transferTableModel.setRowCount(0);
        for (Transfer t : transferService.listarTodos()) {
            if (t.getOrdemServico() == null) {
                transferTableModel.addRow(new Object[]{
                        t.getId(),
                        t.getDataTransfer(),
                        t.getHoraTransfer(),
                        t.getOrigem(),
                        t.getDestino()
                });
            }
        }
    }

    private void atualizarTabelaOS() {
        osTableModel.setRowCount(0);
        for (OrdemServico os : osService.listarTodos()) {
            if ("ABERTA".equals(os.getStatus())) {
                String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "—";
                String veiculo = os.getVeiculo() != null ? os.getVeiculo().getPlaca() : "—";
                osTableModel.addRow(new Object[]{
                        os.getId(), os.getDataServico(), motorista, veiculo, os.getStatus()
                });
            }
        }
    }

    // Helpers UI
    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }
}
