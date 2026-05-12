package br.com.sosviale.view;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.service.MotoristaService;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.VeiculoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class OrdensPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final OrdemServicoService osService = new OrdemServicoService();
    private final MotoristaService motoristaService = new MotoristaService();
    private final VeiculoService veiculoService = new VeiculoService();

    private DefaultTableModel tableModel;
    private JComboBox<String> motoristaCombo;
    private JComboBox<String> veiculoCombo;
    private JComboBox<String> statusCombo;
    private List<Motorista> motoristas;
    private List<Veiculo> veiculos;

    public OrdensPanel() {
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

        JLabel title = new JLabel("Abrir Ordem de Serviço");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(title, gbc);

        // Motorista
        JLabel motoristaLabel = new JLabel("Motorista:");
        motoristaLabel.setFont(BASE_FONT);
        motoristaLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(motoristaLabel, gbc);

        motoristaCombo = new JComboBox<>();
        motoristaCombo.setFont(BASE_FONT);
        motoristaCombo.setBackground(PANEL_BACKGROUND);
        motoristaCombo.setOpaque(true);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(motoristaCombo, gbc);

        // Veículo
        JLabel veiculoLabel = new JLabel("Veículo:");
        veiculoLabel.setFont(BASE_FONT);
        veiculoLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 0, 4, 0);
        form.add(veiculoLabel, gbc);

        veiculoCombo = new JComboBox<>();
        veiculoCombo.setFont(BASE_FONT);
        veiculoCombo.setBackground(PANEL_BACKGROUND);
        veiculoCombo.setOpaque(true);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(veiculoCombo, gbc);

        // Status
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(BASE_FONT);
        statusLabel.setForeground(MUTED_TEXT);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 4, 0);
        form.add(statusLabel, gbc);

        statusCombo = new JComboBox<>(new String[]{"ABERTA", "EM ROTA", "CONCLUÍDA"});
        statusCombo.setFont(BASE_FONT);
        statusCombo.setBackground(PANEL_BACKGROUND);
        statusCombo.setOpaque(true);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(statusCombo, gbc);

        carregarCombos();

        // Botão
        JButton salvar = new JButton("Abrir OS");
        salvar.setBackground(PRIMARY_BLUE);
        salvar.setForeground(Color.WHITE);
        salvar.setFocusPainted(false);
        salvar.setOpaque(true);
        salvar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE),
                new EmptyBorder(8, 14, 8, 14)
        ));
        salvar.setFont(new Font("SansSerif", Font.BOLD, 12));
        salvar.addActionListener(e -> abrirOS());

        gbc.gridy = 99;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(18, 0, 0, 0);
        form.add(salvar, gbc);

        return form;
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Ordens de Serviço");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Data", "Motorista", "Veículo", "Status", "Transfers"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
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

        JLabel dica = new JLabel("💡 A OS é criada com a data de hoje automaticamente.");
        dica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dica.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dica, BorderLayout.SOUTH);
        carregarOS();
        return panel;
    }

    private void carregarCombos() {
        motoristaCombo.removeAllItems();
        veiculoCombo.removeAllItems();

        try {
            motoristas = motoristaService.listarTodos();
            for (Motorista m : motoristas) {
                motoristaCombo.addItem(m.getId() + " - " + m.getNome());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            veiculos = veiculoService.listarTodos();
            for (Veiculo v : veiculos) {
                veiculoCombo.addItem(v.getId() + " - " + v.getLabel() + " (" + v.getPlaca() + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirOS() {
        if (motoristaCombo.getSelectedIndex() < 0 || veiculoCombo.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione motorista e veículo!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            os.setMotorista(motoristas.get(motoristaCombo.getSelectedIndex()));
            os.setVeiculo(veiculos.get(veiculoCombo.getSelectedIndex()));
            os.setStatus((String) statusCombo.getSelectedItem());

            osService.cadastrar(os);
            carregarOS();
            JOptionPane.showMessageDialog(this, "OS aberta com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarOS() {
        tableModel.setRowCount(0);
        try {
            List<OrdemServico> lista = osService.listarTodos();
            for (OrdemServico os : lista) {
                String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "-";
                String veiculo = os.getVeiculo() != null ? os.getVeiculo().getLabel() : "-";
                tableModel.addRow(new Object[]{
                        os.getId(),
                        os.getDataServico(),
                        motorista,
                        veiculo,
                        os.getStatus(),
                        os.getTransfers().size() + " transfer(s)"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}