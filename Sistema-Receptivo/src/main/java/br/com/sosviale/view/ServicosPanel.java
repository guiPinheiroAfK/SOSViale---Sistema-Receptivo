package br.com.sosviale.view;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class ServicosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color INFO_BG          = new Color(244, 245, 247);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 15);

    private final TransferService transferService = new TransferService();

    private DefaultTableModel tableModel;
    private JTable            table;

    public ServicosPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(PANEL_BACKGROUND);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Controle de Serviços Ativos");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.setFont(BASE_FONT);
        btnAtualizar.addActionListener(e -> atualizar());
        header.add(title, BorderLayout.WEST);
        header.add(btnAtualizar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabela
        String[] colunas = {"ID", "OS", "Motorista", "Origem / Destino", "Pax", "Status"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(BASE_FONT);

        setupStatusEditor();

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer com o InfoBox
        add(buildInfoBox(), BorderLayout.SOUTH);

        atualizar();
    }

    /** Recarrega a lista ao abrir a aba ou clicar em Atualizar. */
    public void atualizar() {
        carregarServicos();
    }

    private JPanel buildInfoBox() {
        JPanel infoBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoBox.setBackground(INFO_BG);
        infoBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JLabel icon = new JLabel("ℹ");
        icon.setFont(new Font("Serif", Font.BOLD, 16));
        icon.setForeground(new Color(50, 91, 140));

        JLabel text = new JLabel("Clique no status do serviço para atualizar o andamento em tempo real.");
        text.setFont(new Font("SansSerif", Font.ITALIC, 12));
        text.setForeground(MUTED_TEXT);

        infoBox.add(icon);
        infoBox.add(text);
        return infoBox;
    }

    private void setupStatusEditor() {
        JComboBox<StatusTransfer> comboStatus = new JComboBox<>(new StatusTransfer[]{
                StatusTransfer.NA_OS,
                StatusTransfer.EM_EXECUCAO,
                StatusTransfer.CONCLUIDO,
                StatusTransfer.CANCELADO
        });
        comboStatus.setFont(BASE_FONT);

        TableColumn statusCol = table.getColumnModel().getColumn(5);
        statusCol.setCellEditor(new DefaultCellEditor(comboStatus));

        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();

            if (col == 5 && row >= 0) {
                Integer tId = (Integer) tableModel.getValueAt(row, 0);
                StatusTransfer novoStatus = (StatusTransfer) tableModel.getValueAt(row, col);

                try {
                    Transfer t = transferService.buscarPorId(tId);
                    if (t != null) {
                        t.setStatus(novoStatus);
                        transferService.atualizar(t);
                        carregarServicos();

                        JOptionPane.showMessageDialog(this,
                                "Status do Transfer #" + tId + " atualizado para: " + novoStatus.getDescricao(),
                                "Atualização de Serviço",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void carregarServicos() {
        tableModel.setRowCount(0);

        for (Transfer t : transferService.listarVinculadosOrdemServico()) {
            OrdemServico os = t.getOrdemServico();
            if (os == null) continue;

            String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
            tableModel.addRow(new Object[]{
                    t.getId(),
                    "OS-" + os.getId(),
                    motorista,
                    t.getOrigem() + " ➔ " + t.getDestino(),
                    t.getPassageiros().size(),
                    t.getStatus()
            });
        }
    }
}