package br.com.sosviale.view;

import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransfersPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final TransferService service = new TransferService();
    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField origemField;
    private JTextField destinoField;
    private JTextField dataHoraField;
    private JButton salvarButton;

    // EXPLICAÇÃO: Alterado para Integer para bater com seu model (Erro image_8edebe.png)
    private Integer idSelecionado = null;

    public TransfersPanel() {
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
        gbc.gridx = 0; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Agendar Novo Transfer");
        title.setFont(SECTION_FONT);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 20, 0);
        form.add(title, gbc);

        // Origem
        form.add(new JLabel("Origem:"), gbc);
        gbc.gridy = 1;
        origemField = new JTextField();
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 10, 0);
        form.add(origemField, gbc);

        // Destino
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0);
        form.add(new JLabel("Destino:"), gbc);
        destinoField = new JTextField();
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 10, 0);
        form.add(destinoField, gbc);

        // Data Hora
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 0, 0);
        form.add(new JLabel("Data/Hora (dd/MM/yyyy HH:mm):"), gbc);
        dataHoraField = new JTextField();
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 20, 0);
        form.add(dataHoraField, gbc);

        // Botão Salvar
        salvarButton = new JButton("Agendar");
        salvarButton.setBackground(PRIMARY_BLUE);
        salvarButton.setForeground(Color.WHITE);
        salvarButton.addActionListener(e -> salvarTransfer());
        gbc.gridy = 7;
        form.add(salvarButton, gbc);

        return form;
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        tableModel = new DefaultTableModel(new String[]{"ID", "Origem", "Destino", "Data/Hora", "Status", "Motorista"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        carregarTransfers();
        return panel;
    }

    private void salvarTransfer() {
        try {
            Transfer t = new Transfer();
            t.setOrigem(origemField.getText().trim());
            t.setDestino(destinoField.getText().trim());

            // Conversão de data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            t.setDataHora(LocalDateTime.parse(dataHoraField.getText(), formatter));

            // EXPLICAÇÃO: Removido setMotorista/Veiculo pois seu model Transfer não possui esses campos diretamente (image_8edebe.png)
            if (idSelecionado == null) {
                service.cadastrar(t);
            } else {
                t.setId(idSelecionado);
                service.atualizar(t);
            }

            limpar();
            carregarTransfers();
            JOptionPane.showMessageDialog(this, "Transfer agendado!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: Verifique o formato da data (dd/MM/yyyy HH:mm)");
        }
    }

    private void carregarTransfers() {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<Transfer> lista = service.listarTodos();
        for (Transfer t : lista) {
            // EXPLICAÇÃO: Verificação de segurança para buscar motorista via OrdemServico (image_8edebe.png)
            String motorista = (t.getOrdemServico() != null && t.getOrdemServico().getMotorista() != null)
                    ? t.getOrdemServico().getMotorista().getNome()
                    : "Pendente (Sem OS)";

            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getOrigem(),
                    t.getDestino(),
                    t.getDataHora().format(formatter),
                    t.getStatus(),
                    motorista
            });
        }
    }

    private void limpar() {
        idSelecionado = null;
        origemField.setText("");
        destinoField.setText("");
        dataHoraField.setText("");
    }
}