package br.com.sosviale.view;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.PontoColetaService;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;

/**
 * Painel de gerenciamento de Pontos de Coleta.
 *
 * <p>Exibe todos os pontos de coleta cadastrados, permitindo criar, editar
 * e excluir registros vinculados a transfers. Integra-se ao módulo de navegação
 * principal ({@link ProtipoMainDashboard}) como item de menu independente.
 *
 * <p><b>Responsabilidades:</b>
 * <ul>
 *   <li>Listar pontos de coleta com transfer de origem, local, hora e coordenadas.</li>
 *   <li>Cadastrar novos pontos vinculados a um transfer existente.</li>
 *   <li>Editar local, horário e coordenadas de pontos existentes.</li>
 *   <li>Excluir pontos de coleta com confirmação do usuário.</li>
 * </ul>
 */
public class PontosColetaPanel extends JPanel {

    // ─── Constantes visuais (espelho do padrão do dashboard) ─────────────────
    private static final Color PANEL_BG      = Color.WHITE;
    private static final Color BORDER_COLOR  = new Color(210, 214, 220);
    private static final Color PRIMARY_BLUE  = new Color(50, 91, 140);
    private static final Color DANGER_RED    = new Color(200, 50, 50);
    private static final Color MUTED_TEXT    = new Color(98, 108, 122);
    private static final Font  BASE_FONT     = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  BOLD_FONT     = new Font("SansSerif", Font.BOLD, 14);

    // ─── Colunas da tabela ────────────────────────────────────────────────────
    private static final String[] COLUNAS = {
            "ID", "Transfer", "Local de Coleta", "Ordem", "Horário", "Lat", "Lng"
    };

    // ─── Serviços ─────────────────────────────────────────────────────────────
    private final PontoColetaService pcService      = new PontoColetaService();
    private final TransferService    transferService = new TransferService();

    // ─── Componentes ──────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        fieldLocal;
    private JTextField        fieldOrdem;
    private JTextField        fieldHorario;
    private JTextField        fieldLat;
    private JTextField        fieldLng;
    private JComboBox<TransferItem> comboTransfers;

    // ─── Estado ───────────────────────────────────────────────────────────────
    /** ID do ponto em edição; null = modo criação. */
    private Long editandoId = null;

    // =========================================================================
    // Construção
    // =========================================================================

    public PontosColetaPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        carregarTransfers();
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

        JLabel title = new JLabel("Ponto de Coleta");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        form.add(title, gbc);

        // Transfer (ComboBox)
        comboTransfers = new JComboBox<>();
        comboTransfers.setFont(BASE_FONT);
        gbc.gridy++; form.add(label("Transfer vinculado:"), gbc);
        gbc.gridy++; form.add(comboTransfers, gbc);

        // Local de coleta
        fieldLocal = field("Ex: Aeroporto Internacional de Foz");
        gbc.gridy++; form.add(label("Local de Coleta:"), gbc);
        gbc.gridy++; form.add(fieldLocal, gbc);

        // Ordem da parada
        fieldOrdem = field("Ex: 1");
        gbc.gridy++; form.add(label("Ordem da Parada:"), gbc);
        gbc.gridy++; form.add(fieldOrdem, gbc);

        // Horário previsto
        fieldHorario = field("HH:mm — opcional");
        gbc.gridy++; form.add(label("Horário Previsto (HH:mm):"), gbc);
        gbc.gridy++; form.add(fieldHorario, gbc);

        // Latitude
        fieldLat = field("Ex: -25.5925 — opcional");
        gbc.gridy++; form.add(label("Latitude:"), gbc);
        gbc.gridy++; form.add(fieldLat, gbc);

        // Longitude
        fieldLng = field("Ex: -54.4880 — opcional");
        gbc.gridy++; form.add(label("Longitude:"), gbc);
        gbc.gridy++; form.add(fieldLng, gbc);

        // Botões
        JPanel botoes = new JPanel(new GridLayout(1, 2, 8, 0));
        botoes.setOpaque(false);

        JButton btnSalvar = styledButton("Salvar", PRIMARY_BLUE);
        btnSalvar.addActionListener(e -> salvar());

        JButton btnLimpar = styledButton("Novo", MUTED_TEXT);
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

        // Centraliza colunas numéricas
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 3, 5, 6}) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

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

    private void carregarTransfers() {
        comboTransfers.removeAllItems();
        try {
            List<Transfer> transfers = transferService.listarTodos();
            for (Transfer t : transfers) {
                comboTransfers.addItem(new TransferItem(t));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar transfers: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);
        try {
            // Carrega pontos de todos os transfers visíveis
            List<Transfer> transfers = transferService.listarTodos();
            for (Transfer t : transfers) {
                List<PontoColeta> pontos = pcService.buscarPorTransfer(t.getId().intValue());
                for (PontoColeta pc : pontos) {
                    tableModel.addRow(new Object[]{
                            pc.getId(),
                            "#" + t.getId() + " — " + t.getOrigem(),
                            pc.getLocalColeta(),
                            pc.getOrdemParada(),
                            pc.getHorarioPrevisto() != null ? pc.getHorarioPrevisto().toString() : "—",
                            pc.getLatitude()  != null ? String.format("%.4f", pc.getLatitude())  : "—",
                            pc.getLongitude() != null ? String.format("%.4f", pc.getLongitude()) : "—"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar pontos de coleta: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvar() {
        TransferItem transferSelecionado = (TransferItem) comboTransfers.getSelectedItem();
        if (transferSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um transfer.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String local = fieldLocal.getText().trim();
        String ordemStr = fieldOrdem.getText().trim();

        if (local.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Local de coleta é obrigatório.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ordemStr.isEmpty() || !ordemStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Ordem da parada deve ser um número inteiro.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PontoColeta pc = editandoId != null
                    ? pcService.buscarPorId(editandoId)
                    : new PontoColeta();

            pc.setTransfer(transferSelecionado.transfer);
            pc.setLocalColeta(local);
            pc.setOrdemParada(Integer.parseInt(ordemStr));
            pc.setHorarioPrevisto(parseHorario(fieldHorario.getText().trim()));
            pc.setLatitude(parseDouble(fieldLat.getText().trim()));
            pc.setLongitude(parseDouble(fieldLng.getText().trim()));

            if (editandoId != null) {
                pcService.atualizar(pc);
                JOptionPane.showMessageDialog(this, "Ponto de coleta atualizado com sucesso!");
            } else {
                pcService.cadastrar(pc);
                JOptionPane.showMessageDialog(this, "Ponto de coleta cadastrado com sucesso!");
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
            JOptionPane.showMessageDialog(this, "Selecione um ponto de coleta na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(row, 0);
        String local = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Excluir o ponto \"" + local + "\"?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                pcService.excluir(id);
                limparFormulario();
                atualizarTabela();
                JOptionPane.showMessageDialog(this, "Ponto excluído.");
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

        editandoId = ((Number) tableModel.getValueAt(row, 0)).longValue();

        try {
            PontoColeta pc = pcService.buscarPorId(editandoId);
            if (pc == null) return;

            fieldLocal.setText(pc.getLocalColeta());
            fieldOrdem.setText(String.valueOf(pc.getOrdemParada()));
            fieldHorario.setText(pc.getHorarioPrevisto() != null ? pc.getHorarioPrevisto().toString() : "");
            fieldLat.setText(pc.getLatitude()  != null ? String.valueOf(pc.getLatitude())  : "");
            fieldLng.setText(pc.getLongitude() != null ? String.valueOf(pc.getLongitude()) : "");

            // Seleciona o transfer correto no combo
            if (pc.getTransfer() != null) {
                for (int i = 0; i < comboTransfers.getItemCount(); i++) {
                    TransferItem item = comboTransfers.getItemAt(i);
                    if (item.transfer.getId().equals(pc.getTransfer().getId())) {
                        comboTransfers.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar ponto: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        editandoId = null;
        fieldLocal.setText("");
        fieldOrdem.setText("");
        fieldHorario.setText("");
        fieldLat.setText("");
        fieldLng.setText("");
        if (comboTransfers.getItemCount() > 0) comboTransfers.setSelectedIndex(0);
        table.clearSelection();
    }

    // =========================================================================
    // Helpers de parsing (sem exceção não-tratada ao chegar na UI)
    // =========================================================================

    private LocalTime parseHorario(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalTime.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

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

    // =========================================================================
    // Wrapper para exibição de Transfer no JComboBox
    // =========================================================================

    /** Encapsula um Transfer com toString legível para o JComboBox. */
    private static final class TransferItem {
        final Transfer transfer;

        TransferItem(Transfer transfer) {
            this.transfer = transfer;
        }

        @Override
        public String toString() {
            return "#" + transfer.getId() + " — " + transfer.getOrigem()
                    + " → " + transfer.getDestino();
        }
    }
}