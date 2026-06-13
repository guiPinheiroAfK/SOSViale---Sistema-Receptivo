package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.service.UserService;
import br.com.sosviale.util.OfflineReadGuard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Map;

public class UsuariosPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color DANGER_RED       = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);

    private final UserService service = new UserService();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField nomeField;
    private JTextField usuarioField;
    private JPasswordField senhaField;
    private JPasswordField senhaAdminField;
    private JComboBox<Perfil> perfilCombo;
    private JButton salvarButton;
    private JButton excluirButton;
    private String usuarioSelecionado = null;

    // labels/titulos traduzíveis
    private JLabel formTitleLabel;
    private JLabel tableTitleLabel;
    private JLabel lblNome, lblUsuario, lblSenha, lblPerfil, lblSenhaAdmin;
    private JLabel dicaLabel;

    public UsuariosPanel() {
        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        I18nRegistry.register(this::refreshTexts);
    }

    public void atualizar() {
        carregarUsuarios();
    }

    private void refreshTexts() {
        LanguageManager lm = LanguageManager.getInstance();

        if (formTitleLabel != null) formTitleLabel.setText(lm.translate("users.title"));
        if (tableTitleLabel != null) tableTitleLabel.setText(lm.translate("users.list.title"));

        if (lblNome != null) lblNome.setText(lm.translate("users.label.fullname"));
        if (lblUsuario != null) lblUsuario.setText(lm.translate("users.label.username"));
        if (lblSenha != null) lblSenha.setText(lm.translate("users.label.password"));
        if (lblPerfil != null) lblPerfil.setText(lm.translate("register.profile.label"));
        if (lblSenhaAdmin != null) lblSenhaAdmin.setText(lm.translate("users.label.adminpassword"));

        if (senhaField != null) senhaField.setToolTipText(lm.translate("users.label.password.hint"));
        if (senhaAdminField != null) senhaAdminField.setToolTipText(lm.translate("users.label.adminpassword.hint"));

        if (excluirButton != null) excluirButton.setText(lm.translate("users.button.delete"));
        if (salvarButton != null) {
            salvarButton.setText(usuarioSelecionado == null
                    ? lm.translate("passengers.button.add")
                    : lm.translate("transfers.button.edit"));
        }

        if (dicaLabel != null) dicaLabel.setText(lm.translate("users.table.hint"));

        if (tableModel != null) {
            tableModel.setColumnIdentifiers(new String[]{
                    lm.translate("users.table.col.id"),
                    lm.translate("users.table.col.name"),
                    lm.translate("users.table.col.username"),
                    lm.translate("users.table.col.profile"),
                    lm.translate("users.table.col.created")
            });
        }
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

        formTitleLabel = new JLabel();
        formTitleLabel.setFont(SECTION_FONT);
        formTitleLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(formTitleLabel, gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridy++;
        lblNome = label("");
        form.add(lblNome, gbc);
        nomeField = field();
        gbc.gridy++;
        form.add(nomeField, gbc);

        gbc.gridy++;
        lblUsuario = label("");
        form.add(lblUsuario, gbc);
        usuarioField = field();
        gbc.gridy++;
        form.add(usuarioField, gbc);

        gbc.gridy++;
        lblSenha = label("");
        form.add(lblSenha, gbc);
        senhaField = passwordField();
        gbc.gridy++;
        form.add(senhaField, gbc);

        gbc.gridy++;
        lblPerfil = label("");
        form.add(lblPerfil, gbc);
        perfilCombo = new JComboBox<>(Perfil.values());
        perfilCombo.setFont(BASE_FONT);
        perfilCombo.setBackground(Color.WHITE);
        perfilCombo.setPreferredSize(new Dimension(0, 34));
        gbc.gridy++;
        form.add(perfilCombo, gbc);

        gbc.gridy++;
        lblSenhaAdmin = label("");
        form.add(lblSenhaAdmin, gbc);
        senhaAdminField = passwordField();
        gbc.gridy++;
        form.add(senhaAdminField, gbc);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        salvarButton = styledButton("", PRIMARY_BLUE);
        salvarButton.addActionListener(e -> salvarOuAtualizar());

        excluirButton = styledButton("", DANGER_RED);
        excluirButton.setEnabled(false);
        excluirButton.addActionListener(e -> excluirUsuario());

        JButton limpar = styledButton("", Color.LIGHT_GRAY);
        limpar.setForeground(TEXT_COLOR);
        limpar.addActionListener(e -> limparForm());

        actions.add(salvarButton);
        actions.add(excluirButton);
        actions.add(limpar);

        gbc.gridy = 99;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(18, 0, 0, 0);
        form.add(actions, gbc);

        // "Limpar" usa a mesma chave comum de outros painéis
        limpar.setText(LanguageManager.getInstance().translate("common.clear"));

        return form;
    }

    private JComponent buildTable() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        tableTitleLabel = new JLabel();
        tableTitleLabel.setFont(SECTION_FONT);
        tableTitleLabel.setForeground(TEXT_COLOR);
        panel.add(tableTitleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Nome", "Usuário", "Perfil", "Criado em"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                usuarioSelecionado = (String) tableModel.getValueAt(row, 2);
                nomeField.setText(str(tableModel.getValueAt(row, 1)));
                usuarioField.setText(usuarioSelecionado);
                usuarioField.setEditable(false);
                String perfilStr = String.valueOf(tableModel.getValueAt(row, 3));
                try {
                    perfilCombo.setSelectedItem(Perfil.valueOf(perfilStr));
                } catch (IllegalArgumentException ignored) {
                    perfilCombo.setSelectedIndex(0);
                }
                senhaField.setText("");
                perfilCombo.setEnabled(true);
                excluirButton.setEnabled(true);
                salvarButton.setText(LanguageManager.getInstance().translate("transfers.button.edit"));
            }
        });

        dicaLabel = new JLabel();
        dicaLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        dicaLabel.setForeground(MUTED_TEXT);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dicaLabel, BorderLayout.SOUTH);
        carregarUsuarios();
        return panel;
    }

    private void salvarOuAtualizar() {
        LanguageManager lm = LanguageManager.getInstance();
        String nome = nomeField.getText().trim();
        String usuario = usuarioField.getText().trim();
        String senha = new String(senhaField.getPassword());
        String senhaAdmin = new String(senhaAdminField.getPassword());
        Perfil perfil = (Perfil) perfilCombo.getSelectedItem();

        try {
            if (usuarioSelecionado == null) {
                if (senha.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            lm.translate("users.message.password.required"),
                            lm.translate("transfers.message.warning"),
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                service.registrar(nome, usuario, senha, senhaAdmin, perfil);
                JOptionPane.showMessageDialog(this,
                        lm.translate("users.message.created"),
                        lm.translate("transfers.message.success"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.atualizar(usuarioSelecionado, nome, perfil, senhaAdmin);
                if (!senha.isEmpty()) {
                    service.resetarSenhaAdmin(usuarioSelecionado, senha, senhaAdmin);
                }
                JOptionPane.showMessageDialog(this,
                        lm.translate("users.message.updated"),
                        lm.translate("transfers.message.success"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            limparForm();
            carregarUsuarios();
        } catch (AuthenticationException | ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    lm.translate("transfers.message.warning"), JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    lm.translate("users.message.error", Map.of("msg", String.valueOf(ex.getMessage()))),
                    lm.translate("transfers.message.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirUsuario() {
        if (usuarioSelecionado == null) return;
        LanguageManager lm = LanguageManager.getInstance();

        int confirm = JOptionPane.showConfirmDialog(this,
                lm.translate("users.message.delete.confirm", Map.of("usuario", usuarioSelecionado)),
                lm.translate("transfers.message.delete.confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.excluir(usuarioSelecionado);

            limparForm();
            carregarUsuarios();
            JOptionPane.showMessageDialog(this,
                    lm.translate("users.message.deleted"),
                    lm.translate("transfers.message.success"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (AuthenticationException | ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    lm.translate("transfers.message.warning"), JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    lm.translate("users.message.delete.error", Map.of("msg", String.valueOf(ex.getMessage()))),
                    lm.translate("transfers.message.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarUsuarios() {
        if (OfflineReadGuard.shouldSkipDatabaseReads()) return;
        tableModel.setRowCount(0);
        for (User u : service.listarTodos()) {
            String perfil = u.isAdmin() ? "ADMIN" : (u.getPerfil() != null ? u.getPerfil().name() : "—");
            String criado = u.getCriadoEm() != null ? dateFmt.format(u.getCriadoEm()) : "—";
            tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getNome(),
                    u.getUsuario(),
                    perfil,
                    criado
            });
        }
    }

    private void limparForm() {
        LanguageManager lm = LanguageManager.getInstance();
        usuarioSelecionado = null;
        nomeField.setText("");
        usuarioField.setText("");
        usuarioField.setEditable(true);
        senhaField.setText("");
        senhaAdminField.setText("");
        perfilCombo.setSelectedIndex(0);
        perfilCombo.setEnabled(true);
        salvarButton.setText(lm.translate("passengers.button.add"));
        excluirButton.setEnabled(false);
        table.clearSelection();
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED_TEXT);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(BASE_FONT);
        f.setPreferredSize(new Dimension(0, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        return f;
    }

    private JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(BASE_FONT);
        f.setPreferredSize(new Dimension(0, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        return f;
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }
}