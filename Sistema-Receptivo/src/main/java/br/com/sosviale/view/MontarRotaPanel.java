package br.com.sosviale.view;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MontarRotaPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR        = new Color(38, 43, 51);
    private static final Color MUTED_TEXT        = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE      = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN     = new Color(34, 139, 34);
    private static final Font  BASE_FONT         = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT      = new Font("SansSerif", Font.BOLD, 15);

    private final OrdemServicoService osService      = new OrdemServicoService();
    private final TransferService     transferService = new TransferService();

    // Tabela de OS abertas
    private DefaultTableModel osTableModel;
    private JTable            osTable;

    // Tabela de transfers disponíveis
    private DefaultTableModel transferTableModel;
    private JTable            transferTable;

    // Tabela de transfers já na OS selecionada
    private DefaultTableModel naOsTableModel;
    private JTable            naOsTable;

    // OS atualmente selecionada
    private OrdemServico osSelecionada = null;

    // Label de capacidade
    private JLabel labelCapacidade;

    public MontarRotaPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);

        add(buildOsPanel(),         BorderLayout.WEST);
        add(buildTransfersPanel(),  BorderLayout.CENTER);

        carregarOS();
    }

    // -------------------------------------------------------
    // Painel esquerdo — lista de OS abertas
    // -------------------------------------------------------
    private JPanel buildOsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
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

        osTableModel = new DefaultTableModel(new String[]{"ID", "Data", "Motorista", "Veículo"}, 0) {
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

        JButton btnAtualizar = styledButton("Atualizar", PRIMARY_BLUE);
        btnAtualizar.addActionListener(e -> carregarOS());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(btnAtualizar);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // -------------------------------------------------------
    // Painel direito — transfers disponíveis + transfers na OS
    // -------------------------------------------------------
    private JPanel buildTransfersPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 14));
        panel.setOpaque(false);

        panel.add(buildTransfersDisponiveis());
        panel.add(buildTransfersNaOS());

        return panel;
    }

    private JPanel buildTransfersDisponiveis() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Transfers Disponíveis (sem OS)");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        transferTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Hora", "Origem", "Destino", "Pax"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        transferTable = new JTable(transferTableModel);
        transferTable.setRowHeight(28);
        transferTable.setFont(BASE_FONT);
        transferTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(transferTable), BorderLayout.CENTER);

        JButton btnAdicionar = styledButton("▼  Adicionar à OS selecionada", SUCCESS_GREEN);
        btnAdicionar.addActionListener(e -> adicionarTransferNaOS());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(btnAdicionar);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildTransfersNaOS() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Transfers na OS Selecionada");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        labelCapacidade = new JLabel("Selecione uma OS");
        labelCapacidade.setFont(BASE_FONT);
        labelCapacidade.setForeground(MUTED_TEXT);
        header.add(title, BorderLayout.WEST);
        header.add(labelCapacidade, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        naOsTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Hora", "Origem", "Destino", "Pax"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        naOsTable = new JTable(naOsTableModel);
        naOsTable.setRowHeight(28);
        naOsTable.setFont(BASE_FONT);
        naOsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(naOsTable), BorderLayout.CENTER);

        JButton btnRemover = styledButton("▲  Remover da OS", new Color(200, 50, 50));
        btnRemover.addActionListener(e -> removerTransferDaOS());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(btnRemover);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // -------------------------------------------------------
    // Lógica
    // -------------------------------------------------------
    private void carregarOS() {
        osTableModel.setRowCount(0);
        List<OrdemServico> lista = osService.listarTodos();
        for (OrdemServico os : lista) {
            if ("ABERTA".equals(os.getStatus())) {
                String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
                String veiculo   = os.getVeiculo()   != null ? os.getVeiculo().getPlaca()  : "N/D";
                osTableModel.addRow(new Object[]{os.getId(), os.getDataServico(), motorista, veiculo});
            }
        }
        osSelecionada = null;
        naOsTableModel.setRowCount(0);
        transferTableModel.setRowCount(0);
        labelCapacidade.setText("Selecione uma OS");
        carregarTransfersDisponiveis();
    }

    private void selecionarOS() {
        int row = osTable.getSelectedRow();
        if (row < 0) return;

        Integer idOs = (Integer) osTableModel.getValueAt(row, 0);
        osSelecionada = osService.buscarPorId(idOs);

        carregarTransfersNaOS();
        carregarTransfersDisponiveis();
        atualizarLabelCapacidade();
    }

    private void carregarTransfersDisponiveis() {
        transferTableModel.setRowCount(0);
        List<Transfer> todos = transferService.listarTodos();
        for (Transfer t : todos) {
            if (t.getOrdemServico() == null) {
                transferTableModel.addRow(new Object[]{
                        t.getId(),
                        t.getDataTransfer(),
                        t.getHoraTransfer(),
                        t.getOrigem(),
                        t.getDestino(),
                        t.getPassageiros().size()
                });
            }
        }
    }

    private void carregarTransfersNaOS() {
        naOsTableModel.setRowCount(0);
        if (osSelecionada == null) return;
        for (Transfer t : osSelecionada.getTransfers()) {
            naOsTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getDataTransfer(),
                    t.getHoraTransfer(),
                    t.getOrigem(),
                    t.getDestino(),
                    t.getPassageiros().size()
            });
        }
    }

    private void adicionarTransferNaOS() {
        if (osSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = transferTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um transfer para adicionar.", "Aviso", JOptionPane.WARNING_MESSAGE);
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
                    "Capacidade excedida! Limite do veículo: " + osSelecionada.getVeiculo().getCapacidade() + " pax.",
                    "Capacidade Excedida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Atribui e salva
        t.setOrdemServico(osSelecionada);
        t.setStatus(StatusTransfer.NA_OS);
        transferService.atualizar(t);
        osSelecionada.getTransfers().add(t);

        // Atualiza as tabelas
        carregarTransfersDisponiveis();
        carregarTransfersNaOS();
        atualizarLabelCapacidade();
    }

    private void removerTransferDaOS() {
        if (osSelecionada == null) return;

        int row = naOsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um transfer para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer tId = (Integer) naOsTableModel.getValueAt(row, 0);
        Transfer t  = transferService.buscarPorId(tId);

        // Remove da OS e volta para AGUARDANDO_OS
        t.setOrdemServico(null);
        t.setStatus(StatusTransfer.AGUARDANDO_OS);
        transferService.atualizar(t);
        osSelecionada.getTransfers().removeIf(tr -> tr.getId().equals(tId));

        carregarTransfersDisponiveis();
        carregarTransfersNaOS();
        atualizarLabelCapacidade();
    }

    private void atualizarLabelCapacidade() {
        if (osSelecionada == null || osSelecionada.getVeiculo() == null) return;
        int ocupado   = osSelecionada.getTransfers().stream()
                .mapToInt(t -> t.getPassageiros().size()).sum();
        int capacidade = osSelecionada.getVeiculo().getCapacidade();
        labelCapacidade.setText("Ocupação: " + ocupado + "/" + capacidade + " pax");
        labelCapacidade.setForeground(ocupado >= capacidade ? new Color(200, 50, 50) : SUCCESS_GREEN);
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(BASE_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }
}