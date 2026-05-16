package br.com.sosviale.view;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;
import br.com.sosviale.util.PdfItext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class ServicosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);

    private final TransferService     transferService = new TransferService();
    private final OrdemServicoService osService       = new OrdemServicoService();

    private DefaultTableModel tableModel;
    private JTable table;

    private JLabel labelId;
    private JLabel labelOs;
    private JLabel labelMotorista;
    private JLabel labelRota;
    private JLabel labelPax;
    private JComboBox<StatusTransfer> comboStatus;
    private JButton excluirButton;
    private JButton btnGerarPdf;
    private Integer transferIdSelecionado = null;
    private Integer osIdSelecionado = null;
    private boolean atualizandoCombo = false;

    public ServicosPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
    }

    public void atualizar() {
        carregarServicos();
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

        JLabel title = new JLabel("Detalhes do Serviço");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(title, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        form.add(label("Transfer #:"), gbc);
        labelId = valueLabel("—");
        gbc.gridy++;
        form.add(labelId, gbc);

        gbc.gridy++;
        form.add(label("Ordem de serviço:"), gbc);
        labelOs = valueLabel("—");
        gbc.gridy++;
        form.add(labelOs, gbc);

        gbc.gridy++;
        form.add(label("Motorista:"), gbc);
        labelMotorista = valueLabel("—");
        gbc.gridy++;
        form.add(labelMotorista, gbc);

        gbc.gridy++;
        form.add(label("Rota:"), gbc);
        labelRota = valueLabel("—");
        gbc.gridy++;
        form.add(labelRota, gbc);

        gbc.gridy++;
        form.add(label("Passageiros:"), gbc);
        labelPax = valueLabel("—");
        gbc.gridy++;
        form.add(labelPax, gbc);

        gbc.gridy++;
        form.add(label("Status:"), gbc);
        comboStatus = new JComboBox<>(new StatusTransfer[]{
                StatusTransfer.NA_OS,
                StatusTransfer.EM_EXECUCAO,
                StatusTransfer.CONCLUIDO,
                StatusTransfer.CANCELADO
        });
        comboStatus.setFont(BASE_FONT);
        comboStatus.setBackground(Color.WHITE);
        comboStatus.setPreferredSize(new Dimension(0, 34));
        comboStatus.setEnabled(false);
        comboStatus.addActionListener(e -> {
            if (!atualizandoCombo && transferIdSelecionado != null) {
                alterarStatus();
            }
        });
        gbc.gridy++;
        form.add(comboStatus, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        btnGerarPdf = styledButton("Baixar PDF da OS", PRIMARY_BLUE);
        btnGerarPdf.setEnabled(false);
        btnGerarPdf.addActionListener(e -> baixarPdfOs());

        excluirButton = styledButton("Excluir serviço", DANGER_RED);
        excluirButton.setEnabled(false);
        excluirButton.addActionListener(e -> excluirServico());

        actions.add(btnGerarPdf);
        actions.add(excluirButton);

        gbc.gridy = 99;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(18, 0, 0, 0);
        form.add(actions, gbc);

        return form;
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Serviços ativos (transfers na OS)");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "OS", "Motorista", "Origem / Destino", "Pax", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                preencherDetalhes(table.getSelectedRow());
            }
        });

        JLabel dica = new JLabel("💡 Selecione um serviço da OS para alterar status, baixar PDF da OS ou excluir o transfer.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarServicos();
        return panel;
    }

    private void preencherDetalhes(int row) {
        transferIdSelecionado = (Integer) tableModel.getValueAt(row, 0);
        osIdSelecionado = parseOsId(str(tableModel.getValueAt(row, 1)));
        labelId.setText(String.valueOf(transferIdSelecionado));
        labelOs.setText(str(tableModel.getValueAt(row, 1)));
        labelMotorista.setText(str(tableModel.getValueAt(row, 2)));
        labelRota.setText(str(tableModel.getValueAt(row, 3)));
        labelPax.setText(String.valueOf(tableModel.getValueAt(row, 4)));

        atualizandoCombo = true;
        comboStatus.setSelectedItem(tableModel.getValueAt(row, 5));
        atualizandoCombo = false;

        comboStatus.setEnabled(true);
        excluirButton.setEnabled(true);
        btnGerarPdf.setEnabled(osIdSelecionado != null);
    }

    private Integer parseOsId(String osLabel) {
        if (osLabel == null || !osLabel.startsWith("OS-")) return null;
        try {
            return Integer.parseInt(osLabel.substring(3).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void baixarPdfOs() {
        if (osIdSelecionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um serviço vinculado a uma OS.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            OrdemServico os = osService.buscarComTransfers(osIdSelecionado);
            if (os == null) {
                JOptionPane.showMessageDialog(this, "OS não encontrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (os.getTransfers() == null || os.getTransfers().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "A OS não possui transfers vinculados para gerar o PDF.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Salvar PDF da OS #" + os.getId());
            chooser.setSelectedFile(new File(PdfItext.nomeArquivoPadrao(os)));
            chooser.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File arquivo = chooser.getSelectedFile();
            String caminho = arquivo.getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".pdf")) {
                caminho += ".pdf";
            }

            String gerado = PdfItext.gerarPdfOs(os, caminho);

            int abrir = JOptionPane.showConfirmDialog(this,
                    "PDF salvo em:\n" + gerado + "\n\nDeseja abrir o arquivo?",
                    "PDF gerado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (abrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(gerado));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar PDF: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterarStatus() {
        if (transferIdSelecionado == null) return;

        StatusTransfer novoStatus = (StatusTransfer) comboStatus.getSelectedItem();
        try {
            Transfer t = transferService.buscarPorId(transferIdSelecionado);
            if (t == null) {
                JOptionPane.showMessageDialog(this, "Transfer não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            t.setStatus(novoStatus);
            transferService.atualizar(t);
            carregarServicos();
            selecionarLinhaPorId(transferIdSelecionado);
            JOptionPane.showMessageDialog(this,
                    "Status atualizado para: " + novoStatus.getDescricao(),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirServico() {
        if (transferIdSelecionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir o transfer #" + transferIdSelecionado + " do banco de dados?\n" +
                        "Esta ação não pode ser desfeita.",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            transferService.excluir(transferIdSelecionado);
            limparDetalhes();
            carregarServicos();
            JOptionPane.showMessageDialog(this, "Serviço excluído do banco.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selecionarLinhaPorId(Integer id) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (id.equals(tableModel.getValueAt(i, 0))) {
                table.setRowSelectionInterval(i, i);
                preencherDetalhes(i);
                return;
            }
        }
        limparDetalhes();
    }

    private void limparDetalhes() {
        transferIdSelecionado = null;
        osIdSelecionado = null;
        labelId.setText("—");
        labelOs.setText("—");
        labelMotorista.setText("—");
        labelRota.setText("—");
        labelPax.setText("—");
        atualizandoCombo = true;
        comboStatus.setSelectedIndex(0);
        atualizandoCombo = false;
        comboStatus.setEnabled(false);
        excluirButton.setEnabled(false);
        btnGerarPdf.setEnabled(false);
        table.clearSelection();
    }

    private void carregarServicos() {
        Integer idManter = transferIdSelecionado;
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

        if (idManter != null) {
            selecionarLinhaPorId(idManter);
        }
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD));
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }
}
