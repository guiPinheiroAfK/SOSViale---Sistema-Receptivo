package br.com.sosviale.view;

import br.com.sosviale.model.*;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PontoColetaRepository;
import br.com.sosviale.service.*;
import br.com.sosviale.service.pathfinding.RouteResult;
import br.com.sosviale.util.PathFindingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Painel de gerenciamento de Ordens de Serviço.
 *
 * Permite criar, listar e montar rotas para as OS. A partir desta versão,
 * integra a resolução de rotas por pathfinding diretamente no fluxo de montagem
 * de OS, usando PathFindingUtil para otimizar a sequência de pontos de
 * coleta dos transfers associados.
 *
 * Fluxo de otimização de rota
 *
 * Usuário seleciona uma OS na tabela.
 * Clica em "Otimizar Rota" (modo básico) ou "Otimizar com GPS" (modo estrada).
 * O pathfinding executa em background (SwingWorker) sem travar a UI.
 * O resultado é exibido em um diálogo com log de decisões e distância total.
 * O usuário pode aplicar a ordem otimizada no banco ou descartar.
 */
public class OrdensPanel extends JPanel {

    private static final Color PRIMARY_BLUE  = new Color(50, 91, 140);
    private static final Color SUCCESS_GREEN = new Color(34, 139, 34);
    private static final Color MUTED_TEXT    = new Color(98, 108, 122);
    private static final Font  BASE_FONT     = new Font("SansSerif", Font.PLAIN, 13);

    private final OrdemServicoService   osService      = new OrdemServicoService();
    private final TransferService       transferService = new TransferService();
    private final MotoristaRepository   motoristaRepo   = new MotoristaRepository();
    private final PontoColetaRepository pcRepo          = new PontoColetaRepository();

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

        // inputs
        comboMotoristas = new JComboBox<>();
        comboVeiculos   = new JComboBox<>();

        gbc.gridy++; form.add(new JLabel("Motorista:"), gbc);
        gbc.gridy++; form.add(comboMotoristas, gbc);
        gbc.gridy++; form.add(new JLabel("Veiculo:"), gbc);
        gbc.gridy++; form.add(comboVeiculos, gbc);

        // Botão Salvar
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

        String[] colunas = {"ID", "Data", "Motorista", "Veiculo", "Status"};
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

        JButton btnOtimizarRota = styledButton("Otimizar Rota (Basico)", PRIMARY_BLUE);
        btnOtimizarRota.setToolTipText(
                "Otimiza usando distancia em linha reta (Haversine). Nao requer internet.");
        btnOtimizarRota.addActionListener(e -> executarPathfinding(false));

        JButton btnOtimizarGps = styledButton("Otimizar com GPS", SUCCESS_GREEN);
        btnOtimizarGps.setToolTipText(
                "Usa posicao GPS do motorista e distancias reais de estrada (OSRM). Requer internet.");
        btnOtimizarGps.addActionListener(e -> executarPathfinding(true));

        JButton btnGerarPdf = new JButton("Gerar PDF da OS");
        btnGerarPdf.addActionListener(
                e -> JOptionPane.showMessageDialog(this, "Chamar emissao de PDF..."));

        actions.add(btnMontarRota);
        actions.add(btnOtimizarRota);
        actions.add(btnOtimizarGps);
        actions.add(btnGerarPdf);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void carregarDadosBase() {
        comboMotoristas.addItem("1 - Roberto Silva");
        comboVeiculos.addItem("1 - Mercedes Sprinter (15 pax)");
        atualizarTabelaOS();
    }

    private void atualizarTabelaOS() {
        osTableModel.setRowCount(0);
        List<OrdemServico> ordens = osService.listarTodos();
        for (OrdemServico os : ordens) {
            String motorista = os.getMotorista() != null ? os.getMotorista().getNome() : "N/D";
            String veiculo   = os.getVeiculo()   != null ? os.getVeiculo().getLabel()  : "N/D";
            osTableModel.addRow(new Object[]{
                    os.getId(), os.getDataServico(), motorista, veiculo, os.getStatus()
            });
        }
    }

    // Pathfinding integrado ao contexto de OrdemServico

    private void executarPathfinding(boolean usarGps) {
        int selectedRow = osTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma OS na tabela antes de otimizar a rota.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idOs = (Integer) osTableModel.getValueAt(selectedRow, 0);
        OrdemServico os = osService.buscarPorId(idOs);

        if (os == null) {
            JOptionPane.showMessageDialog(this,
                    "Ordem de Servico #" + idOs + " nao encontrada.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (os.getTransfers().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "A OS #" + idOs + " nao possui transfers. Adicione transfers antes de otimizar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        rodarPathfindingEmBackground(os, usarGps);
    }

    /*
     * Executa o pathfinding em background via SwingWorker, sem bloquear o EDT.
     * Exibe progresso durante o calculo e apresenta o resultado ao concluir.
     */
    private void rodarPathfindingEmBackground(OrdemServico os, boolean usarGps) {
        String modoLabel = usarGps ? "GPS + Estrada (OSRM)" : "Basico (Haversine)";

        JDialog progresso = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Otimizando Rota - OS #" + os.getId(), false);
        JLabel labelProgresso = new JLabel(
                "Calculando rota [" + modoLabel + "] para OS #" + os.getId() + "...",
                SwingConstants.CENTER);
        labelProgresso.setBorder(new EmptyBorder(24, 24, 24, 24));
        labelProgresso.setFont(BASE_FONT);
        progresso.add(labelProgresso);
        progresso.pack();
        progresso.setLocationRelativeTo(this);
        progresso.setVisible(true);

        SwingWorker<RouteResult, Void> worker = new SwingWorker<RouteResult, Void>() {

            @Override
            protected RouteResult doInBackground() {
                if (usarGps && os.getMotorista() != null) {
                    Motorista motoristaAtualizado =
                            motoristaRepo.buscarPorId(os.getMotorista().getId().intValue());
                    return PathFindingUtil.otimizarComGps(os, motoristaAtualizado);
                }
                return PathFindingUtil.otimizar(os);
            }

            @Override
            protected void done() {
                progresso.dispose();
                try {
                    exibirResultadoPathfinding(os, get());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(OrdensPanel.this,
                            "Otimizacao interrompida.", "Aviso", JOptionPane.WARNING_MESSAGE);
                } catch (ExecutionException ee) {
                    JOptionPane.showMessageDialog(OrdensPanel.this,
                            "Erro ao otimizar rota: " + ee.getCause().getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /*
     * Exibe o resultado do pathfinding: log de decisoes, distancia total e
     * botao para aplicar a ordem otimizada no banco via PathFindingUtil.
     */
    private void exibirResultadoPathfinding(OrdemServico os, RouteResult resultado) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Rota Otimizada - OS #" + os.getId(), true);
        dialog.setSize(640, 480);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.getRootPane().setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel cabecalho = new JLabel(String.format(
                "<html><b>OS #%d</b> &mdash; Modo: %s | Distancia total estimada: <b>%.2f km</b></html>",
                os.getId(),
                resultado.getModoCalculo().descricao,
                resultado.getDistanciaTotalKm()
        ));
        cabecalho.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dialog.add(cabecalho, BorderLayout.NORTH);

        JTextArea logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setBackground(new Color(248, 249, 250));

        StringBuilder sb = new StringBuilder();
        List<String> log = resultado.getLogDecisoes();
        if (log.isEmpty()) {
            sb.append("Nenhum log de decisao disponivel.\n");
        } else {
            log.forEach(linha -> sb.append(linha).append("\n"));
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(0);
        dialog.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boolean rotaVazia = resultado.getRotaOtimizada().isEmpty();

        JButton btnAplicar = styledButton("Aplicar Ordem no Banco", SUCCESS_GREEN);
        btnAplicar.setEnabled(!rotaVazia);
        btnAplicar.setToolTipText(rotaVazia
                ? "Nenhuma rota para aplicar"
                : "Renumera ordemParada de cada PontoColeta conforme a rota otimizada");
        btnAplicar.addActionListener(e -> {
            try {
                PathFindingUtil.aplicarOrdemOtimizada(resultado, pcRepo);
                JOptionPane.showMessageDialog(dialog,
                        "Ordem otimizada aplicada com sucesso!\n"
                                + resultado.getRotaOtimizada().size()
                                + " pontos renumerados no banco.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Erro ao aplicar ordem: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(BASE_FONT);
        btnFechar.addActionListener(e -> dialog.dispose());

        botoes.add(btnAplicar);
        botoes.add(btnFechar);
        dialog.add(botoes, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Montagem de rota (existente - preservado sem alteracao)

    private void abrirNovaOS() {
        try {
            OrdemServico os = new OrdemServico();
            os.setDataServico(LocalDate.now());
            //os.setMotorista(motoristaBuscado);
            // os.setVeiculo(veiculoBuscado);
            // osService.salvar(os);
            JOptionPane.showMessageDialog(this, "OS Criada com Sucesso!");
            atualizarTabelaOS();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao criar OS: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selecionarOSParaMontagem() {
        int selectedRow = osTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma OS na tabela primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idOs = (Integer) osTableModel.getValueAt(selectedRow, 0);
        OrdemServico osSelecionada = osService.buscarPorId(idOs);

        if (osSelecionada != null) abrirDialogoMontagem(osSelecionada);
    }

    private void abrirDialogoMontagem(OrdemServico os) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Montar Rota - OS #" + os.getId(), true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel transfersModel = new DefaultTableModel(
                new String[]{"ID", "Data/Hora", "Origem", "Destino", "Pax"}, 0);
        JTable transfersTable = new JTable(transfersModel);

        List<Transfer> disponiveis = transferService.listarTodos();
        for (Transfer t : disponiveis) {
            if (t.getOrdemServico() == null) {
                transfersModel.addRow(new Object[]{
                        t.getId(), t.getDataTransfer(), t.getHoraTransfer(),
                        t.getOrigem(), t.getDestino(),
                        t.getPassageiros().size()
                });
            }
        }

        JButton btnAdicionar = new JButton("Adicionar Transfer a OS");
        btnAdicionar.addActionListener(e -> {
            int row = transfersTable.getSelectedRow();
            if (row < 0) return;

            Long tId = (Long) transfersModel.getValueAt(row, 0);
            Transfer t = transferService.buscarPorId(Math.toIntExact(tId));

            int totalAtual = os.getTransfers().stream()
                    .mapToInt(tr -> tr.getPassageiros().size()).sum();
            int totalNovo = totalAtual + t.getPassageiros().size();

            if (totalNovo > os.getVeiculo().getCapacidade()) {
                JOptionPane.showMessageDialog(dialog,
                        "Capacidade excedida! Limite: " + os.getVeiculo().getCapacidade());
                return;
            }

            if (!os.getTransfers().isEmpty()) {
                Transfer ultimo = os.getTransfers().get(os.getTransfers().size() - 1);
                if (!t.getHoraTransfer().isAfter(ultimo.getHoraTransfer())) {
                    JOptionPane.showMessageDialog(dialog,
                            "Conflito: O transfer deve ser posterior a " + ultimo.getHoraTransfer());
                    return;
                }
            }

            t.setOrdemServico(os);
            transferService.atualizar(t);
            os.getTransfers().add(t);
            transfersModel.removeRow(row);
            JOptionPane.showMessageDialog(dialog, "Transfer adicionado a rota!");
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
}