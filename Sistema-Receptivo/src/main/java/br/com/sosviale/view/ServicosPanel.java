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

public class ServicosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN    = new Color(34, 139, 34);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Color WARNING_ORANGE   = new Color(210, 120, 0);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 15);

    private final OrdemServicoService osService      = new OrdemServicoService();
    private final TransferService     transferService = new TransferService();

    // Tabela de OS com transfers
    private DefaultTableModel osTableModel;
    private JTable            osTable;

    // Tabela de transfers da OS selecionada
    private DefaultTableModel transferTableModel;
    private JTable            transferTable;

    // OS atualmente selecionada
    private OrdemServico osSelecionada = null;

    // Painel de detalhes
    private JLabel labelMotorista;
    private JLabel labelVeiculo;
    private JLabel labelData;
    private JLabel labelStatus;

    public ServicosPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(buildOsPanel(),       BorderLayout.WEST);
        add(buildServicosPanel(), BorderLayout.CENTER);

        carregarOS();
    }

    // -------------------------------------------------------
    // Painel esquerdo — lista de OS com transfers vinculados
    // -------------------------------------------------------
    private JPanel buildOsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Ordens de Serviço");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        osTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Motorista", "Veículo", "Status"}, 0) {
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
    // Painel direito — detalhes da OS + transfers com status
    // -------------------------------------------------------
    private JPanel buildServicosPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        panel.add(buildDetalhesOS(),   BorderLayout.NORTH);
        panel.add(buildTransfers(),    BorderLayout.CENTER);
        panel.add(buildAcoesStatus(),  BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildDetalhesOS() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 14, 6));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        labelMotorista = infoLabel("—");
        labelVeiculo   = infoLabel("—");
        labelData      = infoLabel("—");
        labelStatus    = infoLabel("—");

        panel.add(mutedLabel("Motorista:"));
        panel.add(mutedLabel("Veículo:"));
        panel.add(mutedLabel("Data:"));
        panel.add(mutedLabel("Status OS:"));
        panel.add(labelMotorista);
        panel.add(labelVeiculo);
        panel.add(labelData);
        panel.add(labelStatus);

        return panel;
    }

    private JPanel buildTransfers() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Transfers do Serviço");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        transferTableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Hora", "Origem", "Destino", "Passageiros", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        transferTable = new JTable(transferTableModel);
        transferTable.setRowHeight(28);
        transferTable.setFont(BASE_FONT);
        transferTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(transferTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAcoesStatus() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        JButton btnExecucao  = styledButton("▶  Em Execução",  WARNING_ORANGE);
        JButton btnConcluido = styledButton("✔  Concluído",    SUCCESS_GREEN);
        JButton btnCancelado = styledButton("✖  Cancelado",    DANGER_RED);

        btnExecucao.addActionListener(e  -> atualizarStatus(StatusTransfer.EM_EXECUCAO));
        btnConcluido.addActionListener(e -> atualizarStatus(StatusTransfer.CONCLUIDO));
        btnCancelado.addActionListener(e -> atualizarStatus(StatusTransfer.CANCELADO));

        panel.add(btnExecucao);
        panel.add(btnConcluido);
        panel.add(btnCancelado);

        return panel;
    }

    // -------------------------------------------------------
    // Lógica
    // -------------------------------------------------------
    private void carregarOS() {
        osTableModel.setRowCount(0);
        List<OrdemServico> lista = osService.listarTodos();
        for (OrdemServico os : lista) {
            // Só mostra OS que têm transfers vinculados
            if (!os.getTransfers().isEmpty()) {
                String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
                String veiculo   = os.getVeiculo()   != null ? os.getVeiculo().getPlaca()  : "N/D";
                osTableModel.addRow(new Object[]{
                        os.getId(), os.getDataServico(), motorista, veiculo, os.getStatus()
                });
            }
        }
        osSelecionada = null;
        transferTableModel.setRowCount(0);
        resetarDetalhes();
    }

    private void selecionarOS() {
        int row = osTable.getSelectedRow();
        if (row < 0) return;

        Integer idOs = (Integer) osTableModel.getValueAt(row, 0);
        osSelecionada = osService.buscarPorId(idOs);

        atualizarDetalhes();
        carregarTransfers();
    }

    private void atualizarDetalhes() {
        if (osSelecionada == null) { resetarDetalhes(); return; }

        labelMotorista.setText(osSelecionada.getMotorista() != null
                ? osSelecionada.getMotorista().getNome() : "N/D");
        labelVeiculo.setText(osSelecionada.getVeiculo() != null
                ? osSelecionada.getVeiculo().getLabel() + " — " + osSelecionada.getVeiculo().getPlaca()
                + " (" + osSelecionada.getVeiculo().getCapacidade() + " pax)" : "N/D");
        labelData.setText(osSelecionada.getDataServico() != null
                ? osSelecionada.getDataServico().toString() : "N/D");
        labelStatus.setText(osSelecionada.getStatus());
    }

    private void resetarDetalhes() {
        labelMotorista.setText("—");
        labelVeiculo.setText("—");
        labelData.setText("—");
        labelStatus.setText("—");
    }

    private void carregarTransfers() {
        transferTableModel.setRowCount(0);
        if (osSelecionada == null) return;

        for (Transfer t : osSelecionada.getTransfers()) {
            String passageiros = t.getPassageiros().isEmpty() ? "Nenhum"
                    : String.join(", ", t.getPassageiros().stream()
                    .map(p -> p.getNome()).toList());
            transferTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getDataTransfer(),
                    t.getHoraTransfer(),
                    t.getOrigem(),
                    t.getDestino(),
                    passageiros,
                    t.getStatus().getDescricao()
            });
        }
    }

    private void atualizarStatus(StatusTransfer novoStatus) {
        if (osSelecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = transferTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um transfer para atualizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer tId = (Integer) transferTableModel.getValueAt(row, 0);
        Transfer t  = transferService.buscarPorId(tId);

        // Validação de fluxo: não pode voltar status
        if (t.getStatus() == StatusTransfer.CONCLUIDO || t.getStatus() == StatusTransfer.CANCELADO) {
            JOptionPane.showMessageDialog(this,
                    "Este transfer já está " + t.getStatus().getDescricao() + " e não pode ser alterado.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        t.setStatus(novoStatus);
        transferService.atualizar(t);
        carregarTransfers();

        JOptionPane.showMessageDialog(this,
                "Status atualizado para: " + novoStatus.getDescricao(),
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
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