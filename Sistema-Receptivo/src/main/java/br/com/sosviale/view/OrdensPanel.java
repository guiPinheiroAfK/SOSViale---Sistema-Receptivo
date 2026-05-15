package br.com.sosviale.view;

import br.com.sosviale.model.*;
import br.com.sosviale.service.*;
import br.com.sosviale.util.OfflineReadGuard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class OrdensPanel extends JPanel {

    private static final Color PRIMARY_BLUE  = new Color(50, 91, 140);
    private static final Font  BASE_FONT     = new Font("SansSerif", Font.PLAIN, 13);

    private final OrdemServicoService osService       = new OrdemServicoService();
    private final MotoristaService    motoristaService = new MotoristaService();
    private final VeiculoService      veiculoService   = new VeiculoService();

    private DefaultTableModel        osTableModel;
    private JTable                   osTable;
    private JComboBox<Motorista>     comboMotoristas;
    private JComboBox<Veiculo>       comboVeiculos;

    public OrdensPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildLeftPanel(),   BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        carregarDadosBase();
    }

    private JPanel buildLeftPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(14, 14, 14, 14)
        ));
        form.setPreferredSize(new Dimension(345, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);

        JLabel title = new JLabel("Abrir Nova OS");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        form.add(title, gbc);

        comboMotoristas = new JComboBox<>();
        comboVeiculos   = new JComboBox<>();

        gbc.gridy++; form.add(new JLabel("Motorista:"), gbc);
        gbc.gridy++; form.add(comboMotoristas, gbc);
        gbc.gridy++; form.add(new JLabel("Veículo:"), gbc);
        gbc.gridy++; form.add(comboVeiculos, gbc);

        JButton btnAbrirOS = new JButton("Gerar OS");
        btnAbrirOS.setBackground(PRIMARY_BLUE);
        btnAbrirOS.setForeground(Color.WHITE);
        btnAbrirOS.setFocusPainted(false);
        btnAbrirOS.addActionListener(e -> abrirNovaOS());

        gbc.gridy++; gbc.weighty = 1; gbc.anchor = GridBagConstraints.SOUTH;
        form.add(btnAbrirOS, gbc);

        return form;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        String[] colunas = {"ID", "Data", "Motorista", "Veículo", "Status"};
        osTableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        osTable = new JTable(osTableModel);
        osTable.setRowHeight(28);
        osTable.setFont(BASE_FONT);
        osTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(osTable), BorderLayout.CENTER);
        return panel;
    }

    private void carregarDadosBase() {
        comboMotoristas.removeAllItems();
        comboVeiculos.removeAllItems();

        if (OfflineReadGuard.shouldSkipDatabaseReads()) {
            osTableModel.setRowCount(0);
            return;
        }

        List<Motorista> motoristas = motoristaService.listarTodos();
        for (Motorista m : motoristas) comboMotoristas.addItem(m);

        List<Veiculo> veiculos = veiculoService.listarTodos();
        for (Veiculo v : veiculos) comboVeiculos.addItem(v);

        atualizarTabelaOS();
    }

    private void atualizarTabelaOS() {
        osTableModel.setRowCount(0);
        List<OrdemServico> ordens = osService.listarTodos();
        for (OrdemServico os : ordens) {
            String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
            String veiculo   = os.getVeiculo()   != null ? os.getVeiculo().getPlaca()  : "N/D";
            osTableModel.addRow(new Object[]{
                    os.getId(), os.getDataServico(), motorista, veiculo, os.getStatus()
            });
        }
    }

    private void abrirNovaOS() {
        Motorista motoristaSelecionado = (Motorista) comboMotoristas.getSelectedItem();
        Veiculo   veiculoSelecionado   = (Veiculo)   comboVeiculos.getSelectedItem();

        if (motoristaSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um motorista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (veiculoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um veículo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            os.setMotorista(motoristaSelecionado);
            os.setVeiculo(veiculoSelecionado);
            osService.cadastrar(os);
            JOptionPane.showMessageDialog(this, "OS criada com sucesso!");
            atualizarTabelaOS();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar OS: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}