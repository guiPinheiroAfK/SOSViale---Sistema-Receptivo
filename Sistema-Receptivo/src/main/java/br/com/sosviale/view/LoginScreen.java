package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginScreen extends JFrame {

    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color BACKGROUND = new Color(244, 245, 247);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER = new Color(207, 214, 223);
    private static final Color TEXT = new Color(18, 25, 36);
    private static final Color MUTED_TEXT = new Color(78, 92, 112);
    private static final Color ERROR = new Color(210, 44, 52);

    private static final Font BRAND_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    private final AuthenticationService authService;
    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private LoginCallback loginCallback;

    public LoginScreen(AuthenticationService authService) {
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SOS VIALE - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND);

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createRegisterPanel(), "register");

        setContentPane(mainPanel);

        setMinimumSize(new Dimension(560, 640));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = createScreenPanel();

        panel.add(createHeader(), BorderLayout.NORTH);

        JPanel content = createContentPanel(
                "Acesso ao Sistema",
                "Entre com seu usuário e senha para continuar."
        );

        JPanel loginCard = createCard();

        usernameField = createTextField();
        usernameField.setText("admin");
        addField(loginCard, "Usuário:", usernameField, 0);

        passwordField = createPasswordField(BORDER);
        passwordField.setText("admin123");
        passwordField.addActionListener(e -> performLogin());
        addField(loginCard, "Senha:", passwordField, 1);

        errorLabel = createMessageLabel();
        addMessage(loginCard, errorLabel, 2);

        JPanel buttonPanel = createButtonPanel();

        loginButton = createPrimaryButton("Entrar");
        loginButton.addActionListener(e -> performLogin());
        buttonPanel.add(loginButton);

        registerButton = createSecondaryButton("Criar conta");
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        buttonPanel.add(registerButton);

        content.add(loginCard);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);

        panel.add(createCenteredWrapper(content), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = createScreenPanel();

        panel.add(createHeader(), BorderLayout.NORTH);

        JPanel content = createContentPanel(
                "Criar Conta",
                "Cadastre um novo usuário para acessar o sistema."
        );

        JPanel registerCard = createCard();

        JTextField nameField = createTextField();
        addField(registerCard, "Nome completo:", nameField, 0);

        JTextField regUsernameField = createTextField();
        addField(registerCard, "Usuário:", regUsernameField, 1);

        JPasswordField regPasswordField = createPasswordField(BORDER);
        addField(registerCard, "Senha:", regPasswordField, 2);

        JComboBox<Perfil> perfilCombo = new JComboBox<>(new Perfil[]{
                Perfil.ADMIN, Perfil.GERENTE, Perfil.MOTORISTA
        });
        perfilCombo.setFont(TEXT_FONT);
        perfilCombo.setBackground(WHITE);
        perfilCombo.setPreferredSize(new Dimension(520, 40));
        addField(registerCard, "Perfil:", perfilCombo, 3);

        JPasswordField adminPasswordField = createPasswordField(ERROR);
        addField(registerCard, "Senha do Admin:", adminPasswordField, 4, ERROR);

        JLabel registerErrorLabel = createMessageLabel();
        addMessage(registerCard, registerErrorLabel, 5);

        JPanel buttonPanel = createButtonPanel();

        JButton registerSubmitButton = createPrimaryButton("Criar Conta");
        registerSubmitButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String username = regUsernameField.getText().trim();
                String password = new String(regPasswordField.getPassword()).trim();
                String adminPassword = new String(adminPasswordField.getPassword()).trim();
                Perfil perfil = (Perfil) perfilCombo.getSelectedItem();

                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    registerErrorLabel.setText("Preencha todos os campos");
                    return;
                }

                userService.registrar(name, username, password, adminPassword, perfil);

                JOptionPane.showMessageDialog(
                        LoginScreen.this,
                        "Conta criada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE
                );

                cardLayout.show(mainPanel, "login");
            } catch (AuthenticationException | ValidationException ex) {
                registerErrorLabel.setText(ex.getMessage());
            }
        });
        buttonPanel.add(registerSubmitButton);

        JButton backButton = createSecondaryButton("Voltar");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        buttonPanel.add(backButton);

        content.add(registerCard);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);

        panel.add(createCenteredWrapper(content), BorderLayout.CENTER);

        return panel;
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Preencha usuário e senha");
            return;
        }

        try {
            authService.login(username, password);

            SwingUtilities.invokeLater(() -> {
                dispose();

                if (loginCallback != null) {
                    loginCallback.onLoginSuccess(username);
                }
            });
        } catch (AuthenticationException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private JPanel createScreenPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        return panel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel brandLabel = new JLabel("SOS VIALE | Sistema Receptivo");
        brandLabel.setFont(BRAND_FONT);
        brandLabel.setForeground(TEXT);

        header.add(brandLabel, BorderLayout.WEST);

        return header;
    }

    private JPanel createCenteredWrapper(JPanel content) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(34, 0, 0, 0);

        wrapper.add(content, gbc);

        return wrapper;
    }

    private JPanel createContentPanel(String title, String subtitle) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(0, 0, 34, 0));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.setMaximumSize(new Dimension(620, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(20));

        return content;
    }

    private JPanel createCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(22, 18, 22, 18)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(620, Integer.MAX_VALUE));

        return card;
    }

    private void addField(JPanel panel, String labelText, JComponent field, int row) {
        addField(panel, labelText, field, row, MUTED_TEXT);
    }

    private void addField(JPanel panel, String labelText, JComponent field, int row, Color labelColor) {
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(labelColor);

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row * 2;
        labelGbc.weightx = 1.0;
        labelGbc.fill = GridBagConstraints.HORIZONTAL;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(row == 0 ? 0 : 12, 0, 4, 0);

        panel.add(label, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 0;
        fieldGbc.gridy = row * 2 + 1;
        fieldGbc.weightx = 1.0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(field, fieldGbc);
    }

    private void addMessage(JPanel panel, JLabel label, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);

        panel.add(label, gbc);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(TEXT_FONT);
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(520, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)
        ));

        return field;
    }

    private JPasswordField createPasswordField(Color borderColor) {
        JPasswordField field = new JPasswordField();
        field.setFont(TEXT_FONT);
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(520, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(8, 10, 8, 10)
        ));

        return field;
    }

    private JLabel createMessageLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ERROR);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setHorizontalAlignment(SwingConstants.LEFT);

        return label;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return buttonPanel;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(11, 26, 11, 26));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(new Color(226, 231, 237));
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(132, 143, 158)),
                new EmptyBorder(10, 24, 10, 24)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public interface LoginCallback {
        void onLoginSuccess(String username);
    }

    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }
}
