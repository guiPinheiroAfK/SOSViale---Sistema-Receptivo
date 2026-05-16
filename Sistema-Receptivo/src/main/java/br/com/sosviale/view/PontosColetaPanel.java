package br.com.sosviale.view;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.service.PontoColetaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/*
 * Painel de gerenciamento de Pontos de Coleta.
 *
 * Agora atua como um CATÁLOGO DE ENDEREÇOS.
 * Cadastra locais genéricos (Hotéis, Aeroportos, Pontos Turísticos) que
 * serão utilizados posteriormente como Origem/Destino nos Transfers.
 */
public class PontosColetaPanel extends JPanel {

    // ─── Constantes visuais ─────────────────────────────────────────────────
    private static final Color PANEL_BG      = Color.WHITE;
    private static final Color BORDER_COLOR  = new Color(210, 214, 220);
    private static final Color PRIMARY_BLUE  = new Color(50, 91, 140);
    private static final Color DANGER_RED    = new Color(200, 50, 50);
    private static final Color MUTED_TEXT    = new Color(98, 108, 122);
    private static final Font  BASE_FONT     = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  BOLD_FONT     = new Font("SansSerif", Font.BOLD, 14);

    // ─── Colunas da tabela ────────────────────────────────────────────────────
    private static final String[] COLUNAS = {
            "ID", "Local de Coleta", "Lat", "Lng"
    };

    // ─── Serviços ─────────────────────────────────────────────────────────────
    private final PontoColetaService pcService = new PontoColetaService();

    // Componentes
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        fieldLocal;
    private JTextField        fieldLat;
    private JTextField        fieldLng;

    // ─── Estado
    /* ID do ponto em edição; null = modo criação. */
    private Long editandoId = null;

    // Construção
    public PontosColetaPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        atualizarTabela();
    }

    // =========================================================================
    // Formulário lateral
    // =========================================================================

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)
        ));
        form.setPreferredSize(new Dimension(310, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        JLabel title = new JLabel("Cadastro de Local");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        form.add(title, gbc);

        // Local de coleta
        fieldLocal = field("Ex: Hotel Bourbon");
        gbc.gridy++; form.add(label("Nome do Local:"), gbc);
        gbc.gridy++; form.add(fieldLocal, gbc);

        // Latitude
        fieldLat = field("Ex: -25.5925 (Opcional)");
        gbc.gridy++; form.add(label("Latitude:"), gbc);
        gbc.gridy++; form.add(fieldLat, gbc);

        // Longitude
        fieldLng = field("Ex: -54.4880 (Opcional)");
        gbc.gridy++; form.add(label("Longitude:"), gbc);
        gbc.gridy++; form.add(fieldLng, gbc);

        // Botões
        JPanel botoes = new JPanel(new GridLayout(1, 2, 8, 0));
        botoes.setOpaque(false);

        JButton btnSalvar = styledButton("Salvar", PRIMARY_BLUE);
        btnSalvar.addActionListener(e -> salvar());

        JButton btnLimpar = styledButton("Limpar", MUTED_TEXT);
        btnLimpar.addActionListener(e -> limparFormulario());

        botoes.add(btnSalvar);
        botoes.add(btnLimpar);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        form.add(botoes, gbc);

        return form;
    }

    // =========================================================================
    // Tabela principal
    // =========================================================================

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        tableModel = new DefaultTableModel(COLUNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(BASE_FONT);
        table.getTableHeader().setFont(BOLD_FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Centraliza colunas ID, Lat e Lng
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        // Ajusta a largura das colunas
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

        // Clique na linha preenche o formulário para edição
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencherFormularioParaEdicao();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        // Barra de ações inferior
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acoes.setOpaque(false);

        JButton btnEditar = styledButton("Editar Selecionado", PRIMARY_BLUE);
        btnEditar.addActionListener(e -> preencherFormularioParaEdicao());

        JButton btnExcluir = styledButton("Excluir Selecionado", DANGER_RED);
        btnExcluir.addActionListener(e -> excluir());

        JButton btnAtualizar = styledButton("↻ Atualizar", MUTED_TEXT);
        btnAtualizar.addActionListener(e -> atualizarTabela());

        acoes.add(btnEditar);
        acoes.add(btnExcluir);
        acoes.add(btnAtualizar);

        panel.add(acoes, BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    // Lógica de dados
    // =========================================================================

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        try {
            // Puxa todos os locais cadastrados (independente de transfer)
            List<PontoColeta> pontos = pcService.listarTodos();
            for (PontoColeta pc : pontos) {
                tableModel.addRow(new Object[]{
                        pc.getId(),
                        pc.getLocalColeta(),
                        pc.getLatitude()  != null ? String.format("%.4f", pc.getLatitude())  : "—",
                        pc.getLongitude() != null ? String.format("%.4f", pc.getLongitude()) : "—"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar locais: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvar() {
        String local = fieldLocal.getText().trim();

        if (local.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome do local é obrigatório.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PontoColeta pc;
            if (editandoId != null) {
                pc = pcService.buscarPorId(editandoId);
            } else {
                pc = new PontoColeta();
            }

            pc.setLocalColeta(local);
            pc.setLatitude(parseDouble(fieldLat.getText().trim()));
            pc.setLongitude(parseDouble(fieldLng.getText().trim()));

            if (editandoId != null) {
                pcService.atualizar(pc);
                JOptionPane.showMessageDialog(this, "Local atualizado com sucesso!");
            } else {
                pcService.cadastrar(pc);
                JOptionPane.showMessageDialog(this, "Local cadastrado com sucesso!");
            }

            limparFormulario();
            atualizarTabela();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um local na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pega o ID da tabela (que é exibido como Integer)
        Integer id = (Integer) tableModel.getValueAt(row, 0);
        String local = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir o local \"" + local + "\"?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Passa o ID convertido para Long caso seu Service espere isso
                pcService.excluir(id.longValue());
                limparFormulario();
                atualizarTabela();
                JOptionPane.showMessageDialog(this, "Local excluído.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void preencherFormularioParaEdicao() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        // Pega o ID da tabela
        Integer idInt = (Integer) tableModel.getValueAt(row, 0);
        editandoId = idInt.longValue();

        try {
            PontoColeta pc = pcService.buscarPorId(editandoId);
            if (pc == null) return;

            fieldLocal.setText(pc.getLocalColeta());
            fieldLat.setText(pc.getLatitude()  != null ? String.valueOf(pc.getLatitude())  : "");
            fieldLng.setText(pc.getLongitude() != null ? String.valueOf(pc.getLongitude()) : "");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar local: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        editandoId = null;
        fieldLocal.setText("");
        fieldLat.setText("");
        fieldLng.setText("");
        table.clearSelection();
    }

    // =========================================================================
    // Helpers de parsing
    // =========================================================================

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // =========================================================================
    // Helpers de UI
    // =========================================================================

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }

    private JTextField field(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(BASE_FONT);
        tf.setToolTipText(placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(BASE_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }
}