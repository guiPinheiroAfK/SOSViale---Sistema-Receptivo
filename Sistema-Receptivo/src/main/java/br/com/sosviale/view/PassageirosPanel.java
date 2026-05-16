package br.com.sosviale.view;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.TipoDocumento;
import br.com.sosviale.service.PassageiroService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PassageirosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color DANGER_RED = new Color(200, 50, 50);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final PassageiroService service = new PassageiroService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nomeField;
    private JTextField documentoField;
    private JComboBox<TipoDocumento> tipoDocumentoCombo;
    private JTextField nacionalidadeField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;

    public PassageirosPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
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

        JLabel title = new JLabel("Cadastro de Passageiro");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(title, gbc);

        // Nome
        gbc.gridy++; form.add(label("Nome completo:"), gbc);
        nomeField = field();
        gbc.gridy++; form.add(nomeField, gbc);

        // Tipo de Documento
        gbc.gridy++; form.add(label("Tipo de Documento:"), gbc);
        tipoDocumentoCombo = new JComboBox<>(TipoDocumento.values());
        tipoDocumentoCombo.setFont(BASE_FONT);
        tipoDocumentoCombo.setBackground(Color.WHITE);
        tipoDocumentoCombo.setPreferredSize(new Dimension(0, 34));
        gbc.gridy++; form.add(tipoDocumentoCombo, gbc);

        // Número do Documento
        gbc.gridy++; form.add(label("Número do Documento:"), gbc);
        documentoField = field();
        gbc.gridy++; form.add(documentoField, gbc);

        // Nacionalidade
        gbc.gridy++; form.add(label("Nacionalidade:"), gbc);
        nacionalidadeField = field();
        nacionalidadeField.setText("Brasileira");
        gbc.gridy++; form.add(nacionalidadeField, gbc);

        // Ações
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("Adicionar", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = styledButton("Excluir", DANGER_RED);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirPassageiro());

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

    private void salvarOuAtualizar() {
        String nome = nomeField.getText().trim();
        String documento = documentoField.getText().trim();
        String nacionalidade = nacionalidadeField.getText().trim();
        TipoDocumento tipo = (TipoDocumento) tipoDocumentoCombo.getSelectedItem();

        try {
            if (idSelecionado == null) {
                service.salvar(nome, documento, tipo, nacionalidade);
                JOptionPane.showMessageDialog(this, "Passageiro cadastrado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.atualizar(idSelecionado, nome, documento, tipo, nacionalidade);
                JOptionPane.showMessageDialog(this, "Passageiro atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarPassageiros();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro crítico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODOS AUXILIARES (LABEL E STYLED BUTTON) ---
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

    // --- RESTANTE DA CLASSE (BUILD TABLE, CARREGAR, LIMPAR, ETC) ---
    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Passageiros cadastrados");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Tipo", "Documento", "Nacionalidade"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                nomeField.setText((String) tableModel.getValueAt(row, 1));
                tipoDocumentoCombo.setSelectedItem(tableModel.getValueAt(row, 2));
                documentoField.setText((String) tableModel.getValueAt(row, 3));
                nacionalidadeField.setText((String) tableModel.getValueAt(row, 4));
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        JLabel dica = new JLabel("💡 Clique em um passageiro para editar ou excluir.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarPassageiros();
        return panel;
    }

    private void carregarPassageiros() {
        tableModel.setRowCount(0);
        service.listarTodos().forEach(p -> tableModel.addRow(new Object[]{
                p.getId(), p.getNome(), p.getTipoDocumento(), p.getDocumento(), p.getNacionalidade()
        }));
    }

    private void excluirPassageiro() {
        if (idSelecionado != null) {
            service.excluir(idSelecionado);
            limparForm();
            carregarPassageiros();
        }
    }

    private void limparForm() {
        idSelecionado = null;
        nomeField.setText("");
        documentoField.setText("");
        tipoDocumentoCombo.setSelectedIndex(0);
        nacionalidadeField.setText("Brasileira");
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }
}