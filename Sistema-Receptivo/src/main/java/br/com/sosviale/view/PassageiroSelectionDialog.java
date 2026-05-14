package br.com.sosviale.view;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.service.PassageiroService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PassageiroSelectionDialog extends JDialog {

    private JTextField buscaField;
    private JTable tabelaResultados;
    private DefaultTableModel model;
    private Passageiro selecionado;
    private final PassageiroService service = new PassageiroService();

    public PassageiroSelectionDialog(Frame parent) {
        super(parent, "Selecionar Passageiro", true);
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Painel Superior com a busca
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Pesquisar:"), BorderLayout.WEST);

        buscaField = new JTextField();
        buscaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrar();
            }
        });
        topPanel.add(buscaField, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Tabela
        model = new DefaultTableModel(new String[]{"ID", "Nome", "Documento"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaResultados = new JTable(model);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tabelaResultados), BorderLayout.CENTER);

        // Botão Selecionar
        JButton btn = new JButton("Confirmar Seleção");
        btn.addActionListener(e -> {
            int row = tabelaResultados.getSelectedRow();
            if (row != -1) {
                // Converte o índice da linha da tabela (que pode estar filtrada) para o modelo
                int modelRow = tabelaResultados.convertRowIndexToModel(row);
                Integer id = (Integer) model.getValueAt(modelRow, 0);
                selecionado = service.buscarPorId(id);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Selecione um passageiro na lista.");
            }
        });

        JPanel southPanel = new JPanel();
        southPanel.add(btn);
        add(southPanel, BorderLayout.SOUTH);

        carregarTodos();
    }

    private void carregarTodos() {
        model.setRowCount(0);
        service.listarTodos().forEach(p ->
                model.addRow(new Object[]{p.getId(), p.getNome(), p.getDocumento()})
        );
    }

    private void filtrar() {
        String busca = buscaField.getText().toLowerCase().trim();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tabelaResultados.setRowSorter(sorter);

        if (busca.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Filtra pelo nome (coluna 1) ignorando case
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + busca, 1));
        }
    }

    public Passageiro getSelecionado() {
        return selecionado;
    }
}