package br.com.sosviale.view;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;
import br.com.sosviale.util.OfflineReadGuard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AtribuirOS_TransferPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN    = new Color(34, 139, 34);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 15);

    private final OrdemServicoService osService      = new OrdemServicoService();
    private final TransferService     transferService = new TransferService();

    // Tabela de OS abertas
    private DefaultTableModel osTableModel;
    private JTable            osTable;

    // Tabela de transfers disponíveis
    private DefaultTableModel transferTableModel;
    private JTable            transferTable;

    // OS atualmente selecionada
    private OrdemServico osSelecionada = null;

    // Detalhes da OS selecionada
    private JLabel labelMotorista;
    private JLabel labelVeiculo;
    private JLabel labelCapacidade;

    public AtribuirOS_TransferPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(buildOsPanel(),        BorderLayout.WEST);
        add(buildTransfersPanel(), BorderLayout.CENTER);

        carregarOS();
    }

    // -------------------------------------------------------
    // Painel esquerdo — OS abertas + detalhes da selecionada
    // -------------------------------------------------------
    private JPanel buildOsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Ordens de Serviço Abertas");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        osTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Motorista", "Veículo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        osTable = new JTable(osTableModel);
        osTable.setRowHeight(28);
        osTable.setFont(BASE_FONT);
        osTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        osTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && osTable.getSelectedRow() != -1) {
                selecionarOS();
            }
        });

        panel.add(new JScrollPane(osTable), BorderLayout.CENTER);

        // Detalhes da OS selecionada
        JPanel detalhes = new JPanel(new GridLayout(3, 2, 8, 6));
        detalhes.setBackground(new Color(244, 245, 247));
        detalhes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));

        labelMotorista = infoLabel("—");
        labelVeiculo   = infoLabel("—");
        labelCapacidade = infoLabel("—");

        detalhes.add(mutedLabel("Motorista:"));   detalhes.add(labelMotorista);
        detalhes.add(mutedLabel("Veículo:"));     detalhes.add(labelVeiculo);
        detalhes.add(mutedLabel("Capacidade:"));  detalhes.add(labelCapacidade);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(detalhes, BorderLayout.CENTER);

        JButton btnAtualizar = styledButton("Atualizar", PRIMARY_BLUE);
        btnAtualizar.addActionListener(e -> carregarOS());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnAtualizar);
        south.add(btnPanel, BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    // -------------------------------------------------------
    // Painel direito — transfers disponíveis
    // -------------------------------------------------------
    private JPanel buildTransfersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Header com título e contador
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Transfers Disponíveis");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        JLabel subtitle = new JLabel("Transfers sem OS atribuída");
        subtitle.setFont(BASE_FONT);
        subtitle.setForeground(MUTED_TEXT);
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(subtitle);
        header.add(titleStack, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        transferTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Hora", "Origem", "Destino", "Passageiros"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        transferTable = new JTable(transferTableModel);
        transferTable.setRowHeight(28);
        transferTable.setFont(BASE_FONT);
        transferTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(transferTable), BorderLayout.CENTER);

        // Botão de adicionar + aviso
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);

        JLabel aviso = new JLabel("Após vincular, acesse a página Serviços para acompanhar.");
        aviso.setFont(new Font("SansSerif", Font.ITALIC, 12));
        aviso.setForeground(MUTED_TEXT);

        JButton btnAdicionar = styledButton("▼  Vincular Transfer à OS selecionada", SUCCESS_GREEN);
        btnAdicionar.addActionListener(e -> adicionarTransferNaOS());

        bottom.add(aviso,        BorderLayout.NORTH);
        bottom.add(btnAdicionar, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // Lógica
    private void carregarOS() {
        osTableModel.setRowCount(0);
        if (OfflineReadGuard.shouldSkipDatabaseReads()) {
            transferTableModel.setRowCount(0);
            osSelecionada = null;
            resetarDetalhes();
            return;
        }
        List<OrdemServico> lista = osService.listarTodos();
        for (OrdemServico os : lista) {
            if ("ABERTA".equals(os.getStatus())) {
                String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
                String veiculo   = os.getVeiculo()   != null ? os.getVeiculo().getPlaca()  : "N/D";
                osTableModel.addRow(new Object[]{os.getId(), os.getDataServico(), motorista, veiculo});
            }
        }
        osSelecionada = null;
        resetarDetalhes();
        carregarTransfersDisponiveis();
    }

    private void selecionarOS() {
        int row = osTable.getSelectedRow();
        if (row < 0) return;

        Integer idOs = (Integer) osTableModel.getValueAt(row, 0);
        osSelecionada = osService.buscarPorId(idOs);

        atualizarDetalhes();
    }

    private void atualizarDetalhes() {
        if (osSelecionada == null) { resetarDetalhes(); return; }
        labelMotorista.setText(osSelecionada.getMotorista() != null
                ? osSelecionada.getMotorista().getNome() : "N/D");
        labelVeiculo.setText(osSelecionada.getVeiculo() != null
                ? osSelecionada.getVeiculo().getLabel() : "N/D");

        int ocupado    = osSelecionada.getTransfers().stream()
                .mapToInt(t -> t.getPassageiros().size()).sum();
        int capacidade = osSelecionada.getVeiculo() != null
                ? osSelecionada.getVeiculo().getCapacidade() : 0;
        labelCapacidade.setText(ocupado + "/" + capacidade + " pax");
        labelCapacidade.setForeground(ocupado >= capacidade && capacidade > 0
                ? new Color(200, 50, 50) : SUCCESS_GREEN);
    }

    private void resetarDetalhes() {
        labelMotorista.setText("—");
        labelVeiculo.setText("—");
        labelCapacidade.setText("—");
    }

    private void carregarTransfersDisponiveis() {
        transferTableModel.setRowCount(0);
        List<Transfer> todos = transferService.listarTodos();
        for (Transfer t : todos) {
            if (t.getOrdemServico() == null) {
                String passageiros = t.getPassageiros().isEmpty() ? "Nenhum"
                        : String.join(", ", t.getPassageiros().stream()
                        .map(p -> p.getNome()).toList());
                transferTableModel.addRow(new Object[]{
                        t.getId(),
                        t.getDataTransfer(),
                        t.getHoraTransfer(),
                        t.getOrigem(),
                        t.getDestino(),
                        passageiros
                });
            }
        }
    }

    private void adicionarTransferNaOS() {
        if (osSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = transferTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um transfer para vincular.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer tId = (Integer) transferTableModel.getValueAt(row, 0);
        Transfer t  = transferService.buscarPorId(tId);

        // Validação de capacidade
        int totalAtual = osSelecionada.getTransfers().stream()
                .mapToInt(tr -> tr.getPassageiros().size()).sum();
        int totalNovo  = totalAtual + t.getPassageiros().size();

        if (osSelecionada.getVeiculo() != null && totalNovo > osSelecionada.getVeiculo().getCapacidade()) {
            JOptionPane.showMessageDialog(this,
                    "Capacidade excedida! Limite do veículo: "
                            + osSelecionada.getVeiculo().getCapacidade() + " pax.",
                    "Capacidade Excedida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Vincula e salva
        t.setOrdemServico(osSelecionada);
        t.setStatus(StatusTransfer.NA_OS);
        transferService.atualizar(t);
        osSelecionada.getTransfers().add(t);

        // Atualiza
        carregarTransfersDisponiveis();
        atualizarDetalhes();

        JOptionPane.showMessageDialog(this,
                "Serviço criado! Abra a página Serviços para visualizar.",
                "Vinculado com sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    // -------------------------------------------------------
    // Helpers de UI
    // -------------------------------------------------------
    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(BASE_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }
}