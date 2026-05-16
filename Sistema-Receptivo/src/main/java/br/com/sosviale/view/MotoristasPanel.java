package br.com.sosviale.view;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.service.MotoristaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MotoristasPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color DANGER_RED = new Color(200, 50, 50);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final MotoristaService service = new MotoristaService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nomeField;
    private JTextField cnhField;
    private JTextField telefoneField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;

    public MotoristasPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        carregarMotoristas();
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

        JLabel title = new JLabel("Cadastro de Motorista");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(title, gbc);

        JLabel nomeLabel = new JLabel("Nome completo:");
        nomeLabel.setFont(BASE_FONT);
        nomeLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(nomeLabel, gbc);

        nomeField = new JTextField();
        nomeField.setFont(BASE_FONT);
        nomeField.setPreferredSize(new Dimension(0, 34));
        nomeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(nomeField, gbc);

        JLabel cnhLabel = new JLabel("CNH:");
        cnhLabel.setFont(BASE_FONT);
        cnhLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 4, 0);
        form.add(cnhLabel, gbc);

        cnhField = new JTextField();
        cnhField.setFont(BASE_FONT);
        cnhField.setPreferredSize(new Dimension(0, 34));
        cnhField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(cnhField, gbc);

        gbc.gridy++; form.add(label("Telefone:"), gbc);
        telefoneField = new JTextField();
        telefoneField.setFont(BASE_FONT);
        telefoneField.setPreferredSize(new Dimension(0, 34));
        telefoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        gbc.gridy++; form.add(telefoneField, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = new JButton("Adicionar");
        salvarButton.setBackground(PRIMARY_BLUE);
        salvarButton.setForeground(Color.WHITE);
        salvarButton.setFocusPainted(false);
        salvarButton.setOpaque(true);
        salvarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE),
                new EmptyBorder(8, 14, 8, 14)
        ));
        salvarButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = new JButton("Excluir");
        excluirButton.setBackground(DANGER_RED);
        excluirButton.setForeground(Color.WHITE);
        excluirButton.setFocusPainted(false);
        excluirButton.setOpaque(true);
        excluirButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER_RED),
                new EmptyBorder(8, 14, 8, 14)
        ));
        excluirButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirMotorista());

        JButton limpar = new JButton("Limpar");
        limpar.setBackground(PANEL_BACKGROUND);
        limpar.setForeground(TEXT_COLOR);
        limpar.setFocusPainted(false);
        limpar.setOpaque(true);
        limpar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 14, 8, 14)
        ));
        limpar.setFont(BASE_FONT);
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

        JLabel title = new JLabel("Motoristas cadastrados");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "CNH"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 232, 236));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(BASE_FONT);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                nomeField.setText((String) tableModel.getValueAt(row, 1));
                cnhField.setText((String) tableModel.getValueAt(row, 2));
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        JLabel dica = new JLabel("💡 Clique em um motorista para editar ou excluir.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarMotoristas();
        return panel;
    }

    private void salvarOuAtualizar() {
        String nome = nomeField.getText().trim();
        String cnh = cnhField.getText().trim();
        String telefone = telefoneField.getText().trim();

        if (nome.isEmpty() || cnh.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (idSelecionado == null) {
                // Criamos o objeto completo
                Motorista novoMotorista = new Motorista(nome, cnh);
                novoMotorista.setTelefone(telefone);

                // DICA: Se o seu service ainda só aceita (nome, cnh),
                // você vai precisar atualizar o MotoristaService.java para aceitar o objeto Motorista!
                service.salvar(novoMotorista.getNome(), novoMotorista.getCnh());

                JOptionPane.showMessageDialog(this, "Motorista cadastrado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.atualizar(idSelecionado, nome, cnh);
                JOptionPane.showMessageDialog(this, "Motorista atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarMotoristas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirMotorista() {
        if (idSelecionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este motorista?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.excluir(idSelecionado);
                limparForm();
                carregarMotoristas();
                JOptionPane.showMessageDialog(this, "Motorista excluído!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarMotoristas() {
        tableModel.setRowCount(0);
        try {
            List<Motorista> motoristas = service.listarTodos();
            for (Motorista m : motoristas) {
                tableModel.addRow(new Object[]{m.getId(), m.getNome(), m.getCnh()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limparForm() {
        idSelecionado = null;
        nomeField.setText("");
        cnhField.setText("");
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
        nomeField.requestFocus();
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }
}