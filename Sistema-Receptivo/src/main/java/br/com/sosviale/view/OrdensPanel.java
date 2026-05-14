package br.com.sosviale.view;

import br.com.sosviale.model.*;
import br.com.sosviale.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Painel de gerenciamento de Ordens de Serviço.
 *
 * Agora com arquitetura limpa: delega toda a lógica de negócio e pathfinding
 * para o OrdemServicoService, mantendo a UI fluida usando SwingWorker.
 */
public class OrdensPanel extends JPanel {

    private static final Color PRIMARY_BLUE  = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN = new Color(34, 139, 34);
    private static final Font  BASE_FONT     = new Font("SansSerif", Font.PLAIN, 13);

    private final OrdemServicoService osService = new OrdemServicoService();
    private final TransferService transferService = new TransferService();

    private DefaultTableModel osTableModel;
    private JTable            osTable;
    private JComboBox<String> comboMotoristas;
    private JComboBox<String> comboVeiculos;

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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton btnMontarRota = new JButton("Atribuir Transfers (Montar Rota)");
        btnMontarRota.addActionListener(e -> selecionarOSParaMontagem());

        JButton btnOtimizarRota = styledButton("Otimizar Rota (Básico)", PRIMARY_BLUE);
        btnOtimizarRota.setToolTipText("Otimiza em linha reta (Haversine). Não requer internet.");
        btnOtimizarRota.addActionListener(e -> executarPathfinding(false));

        JButton btnOtimizarGps = styledButton("Otimizar com GPS", SUCCESS_GREEN);
        btnOtimizarGps.setToolTipText("Usa GPS do motorista e distâncias reais (OSRM). Requer internet.");
        btnOtimizarGps.addActionListener(e -> executarPathfinding(true));

        JButton btnVerRota = styledButton("Ver Rota do Motorista", new Color(105, 105, 105)); // Um tom de cinza escuro
        btnVerRota.setToolTipText("Exibe a ordem exata de paradas e passageiros agrupados nesta OS.");
        btnVerRota.addActionListener(e -> abrirDialogoRota());

        actions.add(btnMontarRota);
        actions.add(btnOtimizarRota);
        actions.add(btnOtimizarGps);
        actions.add(btnVerRota);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void carregarDadosBase() {
        // TODO: Buscar motoristas e veículos reais do banco
        comboMotoristas.addItem("1 - Roberto Silva");
        comboVeiculos.addItem("1 - Mercedes Sprinter (15 pax)");
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

    // ====================================================================================
    // PATHFINDING (Agora extremamente limpo graças ao OrdemServicoService)
    // ====================================================================================

    private void executarPathfinding(boolean usarGps) {
        int selectedRow = osTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS na tabela antes de otimizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idOs = (Integer) osTableModel.getValueAt(selectedRow, 0);
        rodarPathfindingEmBackground(idOs, usarGps);
    }

    private void rodarPathfindingEmBackground(Integer idOs, boolean usarGps) {
        String modoLabel = usarGps ? "GPS + Estrada (OSRM)" : "Básico (Haversine)";

        JDialog progresso = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Processando...", false);
        JLabel lbl = new JLabel("Calculando rota [" + modoLabel + "] para OS #" + idOs + "...", SwingConstants.CENTER);
        lbl.setBorder(new EmptyBorder(24, 24, 24, 24));
        progresso.add(lbl);
        progresso.pack();
        progresso.setLocationRelativeTo(this);
        progresso.setVisible(true);

        // Usamos SwingWorker para a tela não travar durante as requisições web do Nominatim/OSRM
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Apenas UMA linha! O service faz as querys, cálculos, agrupa as paradas e salva no banco.
                osService.montarRotaOtimizada(idOs, usarGps);
                return null;
            }

            @Override
            protected void done() {
                progresso.dispose();
                try {
                    get(); // Lança exceções capturadas no doInBackground, se houver
                    JOptionPane.showMessageDialog(OrdensPanel.this,
                            "Rota otimizada e paradas geradas com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    atualizarTabelaOS();
                } catch (Exception ee) {
                    JOptionPane.showMessageDialog(OrdensPanel.this,
                            "Erro ao otimizar rota: " + ee.getCause().getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void abrirDialogoRota() {
        int selectedRow = osTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS na tabela para ver a rota.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idOs = (Integer) osTableModel.getValueAt(selectedRow, 0);
        OrdemServico osSelecionada = osService.buscarPorId(idOs);

        // Verifica se a lista de paradas da OS está vazia ou nula
        if (osSelecionada.getParadasRota() == null || osSelecionada.getParadasRota().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Esta OS ainda não possui uma rota gerada. Clique em 'Otimizar Rota' primeiro.",
                    "Rota Vazia", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        mostrarDialogoParadas(osSelecionada);
    }

    // ====================================================================================
    // MONTAGEM DE OS
    // ====================================================================================

    private void abrirNovaOS() {
        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            // TODO: Buscar entidade Veiculo e Motorista reais pelo texto do combo e setar na OS

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

        if (osSelecionada != null) abrirDialogoMontagem(osSelecionada);
    }

    private void abrirDialogoMontagem(OrdemServico os) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Montar Rota - OS #" + os.getId(), true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel transfersModel = new DefaultTableModel(new String[]{"ID", "Data", "Hora", "Origem", "Destino", "Pax"}, 0);
        JTable transfersTable = new JTable(transfersModel);

        List<Transfer> disponiveis = transferService.listarTodos();
        for (Transfer t : disponiveis) {
            if (t.getOrdemServico() == null) {
                transfersModel.addRow(new Object[]{
                        t.getId(), t.getDataTransfer(), t.getHoraTransfer(),
                        t.getOrigem(), t.getDestino(), t.getPassageiros().size()
                });
            }
        }

        JButton btnAdicionar = new JButton("Adicionar Transfer a OS");
        btnAdicionar.addActionListener(e -> {
            int row = transfersTable.getSelectedRow();
            if (row < 0) return;

            // CORREÇÃO: O ID que vem do model é um Integer, o cast para Long estava dando ClassCastException
            Integer tId = (Integer) transfersModel.getValueAt(row, 0);
            Transfer t = transferService.buscarPorId(tId);

            int totalAtual = os.getTransfers().stream().mapToInt(tr -> tr.getPassageiros().size()).sum();
            int totalNovo = totalAtual + t.getPassageiros().size();

            // Validação blindada para evitar NullPointerException se não houver veículo
            if (os.getVeiculo() != null && totalNovo > os.getVeiculo().getCapacidade()) {
                JOptionPane.showMessageDialog(dialog, "Capacidade excedida! Limite: " + os.getVeiculo().getCapacidade());
                return;
            }

            if (!os.getTransfers().isEmpty()) {
                Transfer ultimo = os.getTransfers().get(os.getTransfers().size() - 1);
                if (t.getHoraTransfer() != null && ultimo.getHoraTransfer() != null &&
                        !t.getHoraTransfer().isAfter(ultimo.getHoraTransfer())) {
                    JOptionPane.showMessageDialog(dialog, "Aviso: Este transfer ocorrerá antes do último adicionado.");
                }
            }

            t.setOrdemServico(os);
            transferService.atualizar(t);
            os.getTransfers().add(t);
            transfersModel.removeRow(row);
            JOptionPane.showMessageDialog(dialog, "Transfer adicionado à rota!");
        });

        dialog.add(new JScrollPane(transfersTable), BorderLayout.CENTER);
        dialog.add(btnAdicionar, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(BASE_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private void mostrarDialogoParadas(OrdemServico os) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Rota da OS #" + os.getId(), true);
        dialog.setSize(700, 400);
        dialog.setLayout(new BorderLayout(10, 10));

        // Cabeçalho simples
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        header.add(new JLabel("Motorista: " + (os.getMotorista() != null ? os.getMotorista().getNome() : "Não definido")));
        dialog.add(header, BorderLayout.NORTH);

        // Nomes das colunas focadas no motorista
        String[] colunas = {"Ordem", "Local de Parada", "Ação", "Horário"};

        List<ParadaOS> paradas = os.getParadasRota();

        // Criando a matriz de dados usando um for clássico
        Object[][] dados = new Object[paradas.size()][4];

        for (int i = 0; i < paradas.size(); i++) {
            ParadaOS parada = paradas.get(i);

            dados[i][0] = parada.getOrdemParada() + "º Parada";
            dados[i][1] = parada.getLocalParada(); // Ex: Hotel Bourbon
            dados[i][2] = parada.getAcao();        // Ex: "Embarcar 3 pessoas"
            dados[i][3] = parada.getHorarioPrevisto() != null ? parada.getHorarioPrevisto().toString() : "--:--";
        }

        JTable tabelaParadas = new JTable(dados, colunas);
        tabelaParadas.setRowHeight(30);
        tabelaParadas.setFont(BASE_FONT);
        tabelaParadas.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Ajuste de largura das colunas para ficar bonito
        tabelaParadas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabelaParadas.getColumnModel().getColumn(1).setPreferredWidth(250);
        tabelaParadas.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabelaParadas.getColumnModel().getColumn(3).setPreferredWidth(100);

        dialog.add(new JScrollPane(tabelaParadas), BorderLayout.CENTER);

        // Botão de fechar embaixo
        JButton btnFechar = styledButton("Fechar", PRIMARY_BLUE);
        btnFechar.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(btnFechar);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

}