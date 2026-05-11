package br.com.sosviale.view;

import br.com.sosviale.model.*;
import br.com.sosviale.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class OrdensPanel extends JPanel {

    private final OrdemServicoService osService = new OrdemServicoService();
    private final TransferService transferService = new TransferService();
    // Instancie os services de Veiculo e Motorista se já os tiver
    // private final MotoristaService motoristaService = new MotoristaService();
    // private final VeiculoService veiculoService = new VeiculoService();

    private DefaultTableModel osTableModel;
    private JTable osTable;
    private JComboBox<String> comboMotoristas;
    private JComboBox<String> comboVeiculos;

    public OrdensPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);

        add(buildLeftPanel(), BorderLayout.WEST);
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

        // Inputs
        comboMotoristas = new JComboBox<>();
        comboVeiculos = new JComboBox<>();

        gbc.gridy++; form.add(new JLabel("Motorista:"), gbc);
        gbc.gridy++; form.add(comboMotoristas, gbc);

        gbc.gridy++; form.add(new JLabel("Veículo:"), gbc);
        gbc.gridy++; form.add(comboVeiculos, gbc);

        // Botão Salvar
        JButton btnAbrirOS = new JButton("Gerar OS");
        btnAbrirOS.setBackground(new Color(50, 91, 140));
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

        // Tabela de OS
        String[] colunas = {"ID", "Data", "Motorista", "Veículo", "Status"};
        osTableModel = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        osTable = new JTable(osTableModel);
        osTable.setRowHeight(28);

        JScrollPane scrollPane = new JScrollPane(osTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Barra de Ações Inferior
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);

        JButton btnMontarRota = new JButton("Atribuir Transfers (Montar Rota)");
        btnMontarRota.addActionListener(e -> selecionarOSParaMontagem());

        JButton btnGerarPdf = new JButton("Gerar PDF da OS");
        btnGerarPdf.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chamar emissão de PDF..."));

        actions.add(btnMontarRota);
        actions.add(btnGerarPdf);

        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void carregarDadosBase() {
        // Mock populando combos (substitua chamando listagens dos services reais)
        comboMotoristas.addItem("1 - Roberto Silva");
        comboVeiculos.addItem("1 - Mercedes Sprinter (15 pax)");

        atualizarTabelaOS();
    }

    private void atualizarTabelaOS() {
        osTableModel.setRowCount(0);
        List<OrdemServico> ordens = osService.listarTodos();
        for (OrdemServico os : ordens) {
            String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
            String veiculo = os.getVeiculo() != null ? os.getVeiculo().getLabel() : "N/D";
            osTableModel.addRow(new Object[]{
                    os.getId(), os.getDataServico(), motorista, veiculo, os.getStatus()
            });
        }
    }

    private void abrirNovaOS() {
        // Aqui você pega o ID do ComboBox selecionado e busca no DB
        // String selecionado = (String) comboMotoristas.getSelectedItem();
        // Integer idMotorista = Integer.parseInt(selecionado.split(" - ")[0]);
        // Mesma lógica pro Veículo.

        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            // os.setMotorista(motoristaBuscado);
            // os.setVeiculo(veiculoBuscado);

            // osService.salvar(os);
            JOptionPane.showMessageDialog(this, "OS Criada com Sucesso!");
            atualizarTabelaOS();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar OS: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selecionarOSParaMontagem() {
        int selectedRow = osTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS na tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idOs = (Integer) osTableModel.getValueAt(selectedRow, 0);
        OrdemServico osSelecionada = osService.buscarPorId(idOs);

        if (osSelecionada != null) {
            abrirDialogoMontagem(osSelecionada);
        }
    }

    // Tradução literal das regras 1 e 2 do CLI para uma Dialog interativa
    private void abrirDialogoMontagem(OrdemServico os) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Montar Rota - OS #" + os.getId(), true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel transfersModel = new DefaultTableModel(new String[]{"ID", "Data/Hora", "Origem", "Destino", "Pax"}, 0);
        JTable transfersTable = new JTable(transfersModel);

        // Puxar apenas os PENDENTES/AGENDADOS sem OS
        List<Transfer> disponiveis = transferService.listarTodos();
        for (Transfer t : disponiveis) {
            if (t.getOrdemServico() == null /* && t.getStatus() == StatusTransfer.AGENDADO */) {
                transfersModel.addRow(new Object[]{t.getId(), t.getDataHora(), t.getOrigem(), t.getDestino(), t.getPassageiros().size()});
            }
        }

        JButton btnAdicionar = new JButton("Adicionar Transfer à OS");
        btnAdicionar.addActionListener(e -> {
            int row = transfersTable.getSelectedRow();
            if (row >= 0) {
                Long tId = (Long) transfersModel.getValueAt(row, 0);
                Transfer t = transferService.buscarPorId(tId);

                // Regra 1 e 2 de negócio (Idêntico ao seu CLI)
                int totalAtual = os.getTransfers().stream().mapToInt(tr -> tr.getPassageiros().size()).sum();
                int totalNovo = totalAtual + t.getPassageiros().size();

                if (totalNovo > os.getVeiculo().getCapacidade()) {
                    JOptionPane.showMessageDialog(dialog, "Capacidade excedida! Limite: " + os.getVeiculo().getCapacidade());
                    return;
                }

                if (!os.getTransfers().isEmpty()) {
                    Transfer ultimo = os.getTransfers().get(os.getTransfers().size() - 1);
                    if (!t.getDataHora().isAfter(ultimo.getDataHora())) {
                        JOptionPane.showMessageDialog(dialog, "Conflito: O transfer deve ser posterior a " + ultimo.getDataHora());
                        return;
                    }
                }

                t.setOrdemServico(os);
                transferService.atualizar(t);
                os.getTransfers().add(t);
                transfersModel.removeRow(row); // Tira da lista visual de disponíveis
                JOptionPane.showMessageDialog(dialog, "Transfer adicionado à rota!");
            }
        });

        dialog.add(new JScrollPane(transfersTable), BorderLayout.CENTER);
        dialog.add(btnAdicionar, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}