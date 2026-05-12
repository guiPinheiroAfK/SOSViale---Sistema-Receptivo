package br.com.sosviale.view;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.TipoDocumento;
import br.com.sosviale.service.PassageiroService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument; // NOVO: Necessário para o filtro
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.List;

public class PassageirosPanel extends JPanel {

    // Constantes de estilo permanecem as mesmas...
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color DANGER_RED = new Color(200, 50, 50);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final PassageiroService service = new PassageiroService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nomeField;
    private JTextField documentoField;
    private JComboBox<TipoDocumento> tipoDocumentoCombo;
    private JTextField nacionalidadeField;
    private JButton salvarButton;
    private JButton excluirButton;
    private Integer idSelecionado = null;

    public PassageirosPanel() {
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

        // Título
        JLabel title = new JLabel("Cadastro de Passageiro");
        title.setFont(SECTION_FONT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(title, gbc);

        // Nome
        JLabel nomeLabel = new JLabel("Nome completo:");
        nomeLabel.setFont(BASE_FONT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(nomeLabel, gbc);

        nomeField = new JTextField();
        nomeField.setFont(BASE_FONT);
        nomeField.setPreferredSize(new Dimension(0, 34));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(nomeField, gbc);

        // Tipo de Documento
        JLabel tipoLabel = new JLabel("Tipo de Documento:");
        tipoLabel.setFont(BASE_FONT);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(tipoLabel, gbc);

        tipoDocumentoCombo = new JComboBox<>(TipoDocumento.values());
        tipoDocumentoCombo.setFont(BASE_FONT);
        tipoDocumentoCombo.setBackground(Color.WHITE);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(tipoDocumentoCombo, gbc);

        // Número do Documento
        JLabel documentoLabel = new JLabel("Número (CPF/RG/Passaporte):");
        documentoLabel.setFont(BASE_FONT);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(documentoLabel, gbc);

        documentoField = new JTextField();
        documentoField.setFont(BASE_FONT);
        documentoField.setPreferredSize(new Dimension(0, 34));

        // EXPLICAÇÃO: Aqui aplicamos o DocumentFilter para limitar a 15 caracteres.
        // Usamos 15 para caber "123.456.789-01" (14 chars) com folga.
        AbstractDocument doc = (AbstractDocument) documentoField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                int currentLength = fb.getDocument().getLength();
                if ((currentLength + text.length() - length) <= 15) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    Toolkit.getDefaultToolkit().beep(); // Avisa o usuário que o limite chegou
                }
            }
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(documentoField, gbc);

        // Nacionalidade
        JLabel nacionalidadeLabel = new JLabel("Nacionalidade:");
        nacionalidadeLabel.setFont(BASE_FONT);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(nacionalidadeLabel, gbc);

        nacionalidadeField = new JTextField("Brasileira");
        nacionalidadeField.setFont(BASE_FONT);
        nacionalidadeField.setPreferredSize(new Dimension(0, 34));
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 20, 0);
        form.add(nacionalidadeField, gbc);

        // Botões (Ações)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = new JButton("Adicionar");
        salvarButton.setBackground(PRIMARY_BLUE);
        salvarButton.setForeground(Color.WHITE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = new JButton("Excluir");
        excluirButton.setBackground(DANGER_RED);
        excluirButton.setForeground(Color.WHITE);
        excluirButton.setVisible(false);
        excluirButton.addActionListener(e -> excluirPassageiro());

        JButton limpar = new JButton("Limpar");
        limpar.addActionListener(e -> limparForm());

        actions.add(salvarButton);
        actions.add(excluirButton);
        actions.add(limpar);

        gbc.gridy = 9;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
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

        JLabel title = new JLabel("Passageiros cadastrados");
        title.setFont(SECTION_FONT);
        panel.add(title, BorderLayout.NORTH);

        // EXPLICAÇÃO: Adicionamos "Tipo" na listagem para o usuário saber o que é aquele doc.
        tableModel = new DefaultTableModel(new String[]{"ID", "Nome", "Tipo", "Documento", "Nacionalidade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // EXPLICAÇÃO: Sincroniza os campos do form quando clica na linha da tabela
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                idSelecionado = (Integer) tableModel.getValueAt(row, 0);
                nomeField.setText((String) tableModel.getValueAt(row, 1));
                tipoDocumentoCombo.setSelectedItem(tableModel.getValueAt(row, 2)); // NOVO: Seta o Enum no Combo
                documentoField.setText((String) tableModel.getValueAt(row, 3));
                nacionalidadeField.setText((String) tableModel.getValueAt(row, 4));
                salvarButton.setText("Salvar alteração");
                excluirButton.setVisible(true);
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        carregarPassageiros();
        return panel;
    }

    private void salvarOuAtualizar() {
        String nome = nomeField.getText().trim();
        String documento = documentoField.getText().trim();
        String nacionalidade = nacionalidadeField.getText().trim();

        // EXPLICAÇÃO: Captura o Enum selecionado no JComboBox
        TipoDocumento tipo = (TipoDocumento) tipoDocumentoCombo.getSelectedItem();

        try {
            if (idSelecionado == null) {
                // EXPLICAÇÃO: Agora passamos o 'tipo' para o service
                service.salvar(nome, documento, tipo, nacionalidade);
                JOptionPane.showMessageDialog(this, "Passageiro cadastrado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.atualizar(idSelecionado, nome, documento, tipo, nacionalidade);
                JOptionPane.showMessageDialog(this, "Passageiro atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarPassageiros();
        } catch (IllegalArgumentException e) {
            // EXPLICAÇÃO: Aqui é onde as mensagens do seu validador (ex: "CPF inválido") aparecem.
            JOptionPane.showMessageDialog(this, e.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirPassageiro() {
        if (idSelecionado == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Excluir passageiro?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.excluir(idSelecionado);
            limparForm();
            carregarPassageiros();
        }
    }

    private void carregarPassageiros() {
        tableModel.setRowCount(0);
        List<Passageiro> lista = service.listarTodos();
        for (Passageiro p : lista) {
            // EXPLICAÇÃO: Adicionando o p.getTipoDocumento() na linha da tabela
            tableModel.addRow(new Object[]{p.getId(), p.getNome(), p.getTipoDocumento(), p.getDocumento(), p.getNacionalidade()});
        }
    }

    private void limparForm() {
        idSelecionado = null;
        nomeField.setText("");
        documentoField.setText("");
        tipoDocumentoCombo.setSelectedIndex(0); // Volta para a primeira opção (ex: CPF)
        nacionalidadeField.setText("Brasileira");
        salvarButton.setText("Adicionar");
        excluirButton.setVisible(false);
        table.clearSelection();
    }
}