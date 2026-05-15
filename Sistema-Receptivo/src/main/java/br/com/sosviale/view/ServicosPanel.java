package br.com.sosviale.view;

import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.offline.MotoristaFieldService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.util.PdfItext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Map;

public class ServicosPanel extends JPanel implements LanguageManager.LanguageChangeListener {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);

    private final MotoristaFieldService fieldService = new MotoristaFieldService();

    private DefaultTableModel tableModel;
    private JTable table;

    private JLabel titleFormLabel;
    private JLabel labelTransferKey, labelOsKey, labelMotoristaKey, labelRotaKey, labelPaxKey, labelStatusKey;
    private JLabel labelId;
    private JLabel labelOs;
    private JLabel labelMotorista;
    private JLabel labelRota;
    private JLabel labelPax;
    private JLabel tableTitleLabel;
    private JLabel dicaLabel;
    private JLabel offlineStatusLabel;
    private JComboBox<StatusTransfer> comboStatus;
    private JButton excluirButton;
    private JButton btnGerarPdf;
    private Integer transferIdSelecionado = null;
    private Integer osIdSelecionado = null;
    private boolean atualizandoCombo = false;

    public ServicosPanel() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        offlineStatusLabel = new JLabel();
        offlineStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        offlineStatusLabel.setForeground(new Color(200, 120, 40));
        topBar.add(offlineStatusLabel, BorderLayout.WEST);
        topBar.add(new NotificationBellPanel(), BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.add(buildForm(), BorderLayout.WEST);
        body.add(buildTable(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        LanguageManager.getInstance().addLanguageChangeListener(this);
        updateTexts();
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

        titleFormLabel = new JLabel();
        titleFormLabel.setFont(SECTION_FONT);
        titleFormLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(titleFormLabel, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        labelTransferKey = label("servicos.label.transfer");
        form.add(labelTransferKey, gbc);
        labelId = valueLabel("—");
        gbc.gridy++;
        form.add(labelId, gbc);

        gbc.gridy++;
        labelOsKey = label("servicos.label.os");
        form.add(labelOsKey, gbc);
        labelOs = valueLabel("—");
        gbc.gridy++;
        form.add(labelOs, gbc);

        gbc.gridy++;
        labelMotoristaKey = label("servicos.label.driver");
        form.add(labelMotoristaKey, gbc);
        labelMotorista = valueLabel("—");
        gbc.gridy++;
        form.add(labelMotorista, gbc);

        gbc.gridy++;
        labelRotaKey = label("servicos.label.route");
        form.add(labelRotaKey, gbc);
        labelRota = valueLabel("—");
        gbc.gridy++;
        form.add(labelRota, gbc);

        gbc.gridy++;
        labelPaxKey = label("servicos.label.passengers");
        form.add(labelPaxKey, gbc);
        labelPax = valueLabel("—");
        gbc.gridy++;
        form.add(labelPax, gbc);

        gbc.gridy++;
        labelStatusKey = label("servicos.label.status");
        form.add(labelStatusKey, gbc);
        comboStatus = new JComboBox<>(StatusTransfer.values());
        comboStatus.setFont(BASE_FONT);
        comboStatus.setBackground(Color.WHITE);
        comboStatus.setPreferredSize(new Dimension(0, 34));
        comboStatus.setEnabled(false);
        comboStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StatusTransfer st) {
                    setText(LanguageManager.getInstance().translateStatus(st));
                }
                return this;
            }
        });
        comboStatus.addActionListener(e -> {
            if (!atualizandoCombo && transferIdSelecionado != null) {
                alterarStatus();
            }
        });
        gbc.gridy++;
        form.add(comboStatus, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        btnGerarPdf = styledButton("servicos.button.pdf", PRIMARY_BLUE);
        btnGerarPdf.setEnabled(false);
        btnGerarPdf.addActionListener(e -> baixarPdfOs());

        excluirButton = styledButton("servicos.button.delete", DANGER_RED);
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

        tableTitleLabel = new JLabel();
        tableTitleLabel.setFont(SECTION_FONT);
        tableTitleLabel.setForeground(TEXT_COLOR);
        panel.add(tableTitleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"", "", "", "", "", ""}, 0) {
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

        dicaLabel = new JLabel();
        dicaLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dicaLabel.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dicaLabel, BorderLayout.SOUTH);
        carregarServicos();
        return panel;
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        titleFormLabel.setText(lm.translate("servicos.form.title"));
        labelTransferKey.setText(lm.translate("servicos.label.transfer"));
        labelOsKey.setText(lm.translate("servicos.label.os"));
        labelMotoristaKey.setText(lm.translate("servicos.label.driver"));
        labelRotaKey.setText(lm.translate("servicos.label.route"));
        labelPaxKey.setText(lm.translate("servicos.label.passengers"));
        labelStatusKey.setText(lm.translate("servicos.label.status"));
        tableTitleLabel.setText(lm.translate("servicos.table.title"));
        dicaLabel.setText(lm.translate("servicos.table.hint"));
        btnGerarPdf.setText(lm.translate("servicos.button.pdf"));
        excluirButton.setText(lm.translate("servicos.button.delete"));
        tableModel.setColumnIdentifiers(new String[]{
                lm.translate("servicos.table.col.id"),
                lm.translate("servicos.table.col.os"),
                lm.translate("servicos.table.col.driver"),
                lm.translate("servicos.table.col.route"),
                lm.translate("servicos.table.col.pax"),
                lm.translate("servicos.table.col.status")
        });
        comboStatus.repaint();
        carregarServicos();
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        updateTexts();
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
        Object statusVal = tableModel.getValueAt(row, 5);
        if (statusVal instanceof StatusTransfer st) {
            comboStatus.setSelectedItem(st);
        }
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
                    LanguageManager.getInstance().translate("servicos.warn.select.os"),
                    LanguageManager.getInstance().translate("transfers.message.warning"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            OrdemServico os = fieldService.buscarOsComTransfers(osIdSelecionado);
            if (os == null) {
                JOptionPane.showMessageDialog(this,
                        LanguageManager.getInstance().translate("servicos.error.os.notfound"),
                        LanguageManager.getInstance().translate("transfers.message.error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (os.getTransfers() == null || os.getTransfers().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        LanguageManager.getInstance().translate("servicos.warn.no.transfers"),
                        LanguageManager.getInstance().translate("transfers.message.warning"),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("OS #" + os.getId());
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
                    LanguageManager.getInstance().translate("servicos.pdf.saved", Map.of("path", gerado)),
                    LanguageManager.getInstance().translate("servicos.pdf.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);

            if (abrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(gerado));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    LanguageManager.getInstance().translate("servicos.pdf.error", Map.of("msg", ex.getMessage())),
                    LanguageManager.getInstance().translate("transfers.message.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterarStatus() {
        if (transferIdSelecionado == null) return;

        StatusTransfer novoStatus = (StatusTransfer) comboStatus.getSelectedItem();
        try {
            fieldService.atualizarStatus(transferIdSelecionado, novoStatus);
            carregarServicos();
            selecionarLinhaPorId(transferIdSelecionado);
            JOptionPane.showMessageDialog(this,
                    LanguageManager.getInstance().translate("servicos.status.updated", Map.of(
                            "status", LanguageManager.getInstance().translateStatus(novoStatus))),
                    LanguageManager.getInstance().translate("transfers.message.success"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    LanguageManager.getInstance().translate("servicos.error.update", Map.of("msg", ex.getMessage())),
                    LanguageManager.getInstance().translate("transfers.message.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirServico() {
        if (transferIdSelecionado == null) return;
        LanguageManager lm = LanguageManager.getInstance();
        int confirm = JOptionPane.showConfirmDialog(this,
                lm.translate("servicos.delete.confirm", Map.of("id", String.valueOf(transferIdSelecionado))),
                lm.translate("transfers.message.delete.confirm.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            fieldService.excluirTransfer(transferIdSelecionado);
            limparDetalhes();
            carregarServicos();
            JOptionPane.showMessageDialog(this,
                    lm.translate("servicos.delete.success"),
                    lm.translate("transfers.message.success"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    lm.translate("servicos.error.delete", Map.of("msg", ex.getMessage())),
                    lm.translate("transfers.message.error"),
                    JOptionPane.ERROR_MESSAGE);
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
        LanguageManager lm = LanguageManager.getInstance();

        for (Transfer t : fieldService.listarServicosAtivos()) {
            OrdemServico os = t.getOrdemServico();
            if (os == null) continue;

            String motorista = os.getMotorista() != null
                    ? os.getMotorista().getNome()
                    : lm.translate("common.na");
            tableModel.addRow(new Object[]{
                    t.getId(),
                    "OS-" + os.getId(),
                    motorista,
                    t.getOrigem() + " ➔ " + t.getDestino(),
                    t.getPassageiros() != null ? t.getPassageiros().size() : 0,
                    t.getStatus()
            });
        }

        atualizarIndicadorOffline();

        if (idManter != null) {
            selecionarLinhaPorId(idManter);
        }
    }

    private void atualizarIndicadorOffline() {
        if (offlineStatusLabel == null) return;
        LanguageManager lm = LanguageManager.getInstance();
        MotoristaFieldService.DataSource src = fieldService.getLastSource();
        int pending = fieldService.getLastPendingCount();

        String texto = switch (src) {
            case ONLINE -> {
                String base = lm.translate("offline.status.online");
                if (pending > 0) {
                    yield base + " · " + lm.translate("offline.status.pending", Map.of("n", String.valueOf(pending)));
                }
                yield fieldService.getUltimaSincronizacao()
                        .map(s -> base + " · " + lm.translate("offline.status.synced", Map.of("when", s)))
                        .orElse(base);
            }
            case OFFLINE_CACHE -> {
                String base = lm.translate("offline.status.offline");
                if (pending > 0) {
                    yield base + " · " + lm.translate("offline.status.pending", Map.of("n", String.valueOf(pending)));
                }
                yield base;
            }
            case EMPTY -> lm.translate("offline.status.empty");
        };
        offlineStatusLabel.setText(texto);
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private JLabel label(String translationKey) {
        JLabel l = new JLabel(LanguageManager.getInstance().translate(translationKey));
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

    private JButton styledButton(String translationKey, Color bg) {
        JButton b = new JButton(LanguageManager.getInstance().translate(translationKey));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }
}
