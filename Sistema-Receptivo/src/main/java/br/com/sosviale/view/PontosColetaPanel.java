package br.com.sosviale.view;

import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.service.PontoColetaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PontosColetaPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);

    private final PontoColetaService service = new PontoColetaService();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField fieldLocal;
    private JTextField fieldLat;
    private JTextField fieldLng;
    private JButton salvarButton;
    private JButton excluirButton;
    private Long idSelecionado = null;
    private JLabel formTitleLabel;
    private JLabel tableTitleLabel;
    private JLabel dicaLabel;

    public PontosColetaPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        I18nRegistry.register(this::refreshTexts);
    }

    private void refreshTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        if (formTitleLabel != null) formTitleLabel.setText(lm.translate("collection.form.title"));
        if (tableTitleLabel != null) tableTitleLabel.setText(lm.translate("collection.list.title"));
        if (dicaLabel != null) dicaLabel.setText(lm.translate("collection.table.hint"));
        if (tableModel != null) {
            tableModel.setColumnIdentifiers(new String[]{
                    lm.translate("collection.table.id"),
                    lm.translate("collection.table.name"),
                    lm.translate("collection.table.address"),
                    lm.translate("collection.table.city")
            });
        }
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

        formTitleLabel = new JLabel();
        formTitleLabel.setFont(SECTION_FONT);
        formTitleLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(formTitleLabel, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        form.add(label("Nome do Local:"), gbc);
        fieldLocal = field();
        gbc.gridy++;
        form.add(fieldLocal, gbc);

        gbc.gridy++;
        form.add(label("Latitude:"), gbc);
        fieldLat = field();
        fieldLat.setToolTipText("Ex: -25.5925 (opcional)");
        gbc.gridy++;
        form.add(fieldLat, gbc);

        gbc.gridy++;
        form.add(label("Longitude:"), gbc);
        fieldLng = field();
        fieldLng.setToolTipText("Ex: -54.4880 (opcional)");
        gbc.gridy++;
        form.add(fieldLng, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("Adicionar", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = styledButton("Excluir", DANGER_RED);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirPonto());

        JButton limpar = styledButton("Limpar", Color.LIGHT_GRAY);
        limpar.setForeground(TEXT_COLOR);
        limpar.addActionListener(e -> limparForm());

        actions.add(salvarButton);
        actions.add(excluirButton);
        actions.add(limpar);

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

        tableModel = new DefaultTableModel(new String[]{"ID", "Local de Coleta", "Lat", "Lng"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                Integer idInt = (Integer) tableModel.getValueAt(row, 0);
                idSelecionado = idInt.longValue();
                fieldLocal.setText(str(tableModel.getValueAt(row, 1)));

                try {
                    PontoColeta pc = service.buscarPorId(idSelecionado);
                    if (pc != null) {
                        fieldLat.setText(pc.getLatitude() != null ? String.valueOf(pc.getLatitude()) : "");
                        fieldLng.setText(pc.getLongitude() != null ? String.valueOf(pc.getLongitude()) : "");
                    }
                } catch (Exception ex) {
                    fieldLat.setText("");
                    fieldLng.setText("");
                }

                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        dicaLabel = new JLabel();
        dicaLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dicaLabel.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dicaLabel, BorderLayout.SOUTH);
        carregarPontos();
        return panel;
    }

    private void salvarOuAtualizar() {
        String local = fieldLocal.getText().trim();
        if (local.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome do local é obrigatório.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PontoColeta pc;
            if (idSelecionado != null) {
                pc = service.buscarPorId(idSelecionado);
                if (pc == null) {
                    JOptionPane.showMessageDialog(this, "Local não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                pc = new PontoColeta();
            }

            pc.setLocalColeta(local);
            pc.setLatitude(parseDouble(fieldLat.getText().trim()));
            pc.setLongitude(parseDouble(fieldLng.getText().trim()));

            if (idSelecionado != null) {
                service.atualizar(pc);
                JOptionPane.showMessageDialog(this, "Local atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.cadastrar(pc);
                JOptionPane.showMessageDialog(this, "Local cadastrado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            limparForm();
            carregarPontos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirPonto() {
        if (idSelecionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este local?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.excluir(idSelecionado);
            limparForm();
            carregarPontos();
            JOptionPane.showMessageDialog(this, "Local excluído!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarPontos() {
        tableModel.setRowCount(0);
        for (PontoColeta pc : service.listarTodos()) {
            tableModel.addRow(new Object[]{
                    pc.getId(),
                    pc.getLocalColeta(),
                    pc.getLatitude() != null ? String.format("%.4f", pc.getLatitude()) : "—",
                    pc.getLongitude() != null ? String.format("%.4f", pc.getLongitude()) : "—"
            });
        }
    }

    private void limparForm() {
        idSelecionado = null;
        fieldLocal.setText("");
        fieldLat.setText("");
        fieldLng.setText("");
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Coordenada inválida: " + raw);
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

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(BASE_FONT);
        f.setPreferredSize(new Dimension(0, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        return f;
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
