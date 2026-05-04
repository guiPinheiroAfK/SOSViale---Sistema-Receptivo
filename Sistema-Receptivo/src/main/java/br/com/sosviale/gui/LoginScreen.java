package br.com.sosviale.gui;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.auth.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/*
 * Tela de Login do SOS VIALE
 * Integrada com AuthenticationService
 */
public class LoginScreen extends JFrame {

    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color BACKGROUND = new Color(244, 245, 247);
    private static final Color WHITE = Color.WHITE;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);

    private final AuthenticationService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public LoginScreen(AuthenticationService authService) {
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("SOS VIALE - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel com CardLayout para trocar entre Login e Registro
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND);

        mainPanel.add("login", createLoginPanel());
        mainPanel.add("register", createRegisterPanel());

        setContentPane(mainPanel);
        setVisible(true);
    }

    /**
     * Cria painel de login
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Logo/Título
        JLabel titleLabel = new JLabel("SOS VIALE");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Sistema Receptivo Trinacional");
        subtitleLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        subtitleLabel.setForeground(new Color(98, 108, 122));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 40, 20);
        panel.add(subtitleLabel, gbc);

        // Card de Login
        JPanel loginCard = createCard();

        // Username
        JLabel usernameLabel = new JLabel("Usuário:");
        usernameLabel.setFont(LABEL_FONT);
        usernameLabel.setForeground(new Color(38, 43, 51));
        loginCard.add(usernameLabel);

        usernameField = createTextField();
        usernameField.setText("admin");
        loginCard.add(usernameField);

        // Password
        JLabel passwordLabel = new JLabel("Senha:");
        passwordLabel.setFont(LABEL_FONT);
        passwordLabel.setForeground(new Color(38, 43, 51));
        loginCard.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(TEXT_FONT);
        passwordField.setText("admin123");
        passwordField.setPreferredSize(new Dimension(300, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(8, 8, 8, 8)
        ));

        passwordField.addActionListener(e -> performLogin());
        loginCard.add(passwordField);

        // Erro
        errorLabel = new JLabel();
        errorLabel.setForeground(new Color(200, 50, 50));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(errorLabel);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        panel.add(loginCard, gbc);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        loginButton = createPrimaryButton("Entrar");
        loginButton.addActionListener(e -> performLogin());
        buttonPanel.add(loginButton);

        registerButton = createSecondaryButton("Criar conta");
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        buttonPanel.add(registerButton);

        gbc.gridy = 3;
        gbc.insets = new Insets(20, 20, 40, 20);
        panel.add(buttonPanel, gbc);

        // Info para teste
        JLabel infoLabel = new JLabel("<html><center>Teste rápido:<br>Usuário: admin | Senha: admin123<br>" +
                "Clique em 'Criar conta' com senha: admin123</center></html>");
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        infoLabel.setForeground(new Color(98, 108, 122));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 20, 20, 20);
        panel.add(infoLabel, gbc);

        // Espaço vazio no final
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalStrut(0), gbc);

        return panel;
    }

    /**
     * Cria painel de registro
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Título
        JLabel titleLabel = new JLabel("Criar Conta");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(titleLabel, gbc);

        // Card de Registro
        JPanel registerCard = createCard();

        // Nome
        JLabel nameLabel = new JLabel("Nome completo:");
        nameLabel.setFont(LABEL_FONT);
        registerCard.add(nameLabel);

        JTextField nameField = createTextField();
        registerCard.add(nameField);

        // Username
        JLabel usernameLabel = new JLabel("Usuário:");
        usernameLabel.setFont(LABEL_FONT);
        registerCard.add(usernameLabel);

        JTextField regUsernameField = createTextField();
        registerCard.add(regUsernameField);

        // Password
        JLabel passwordLabel = new JLabel("Senha:");
        passwordLabel.setFont(LABEL_FONT);
        registerCard.add(passwordLabel);

        JPasswordField regPasswordField = new JPasswordField();
        regPasswordField.setFont(TEXT_FONT);
        regPasswordField.setPreferredSize(new Dimension(300, 38));
        regPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        registerCard.add(regPasswordField);

        // Admin Password
        JLabel adminPasswordLabel = new JLabel("Senha do Admin:");
        adminPasswordLabel.setFont(LABEL_FONT);
        adminPasswordLabel.setForeground(new Color(200, 50, 50));
        registerCard.add(adminPasswordLabel);

        JPasswordField adminPasswordField = new JPasswordField();
        adminPasswordField.setFont(TEXT_FONT);
        adminPasswordField.setPreferredSize(new Dimension(300, 38));
        adminPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 50, 50)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        registerCard.add(adminPasswordField);

        // Erro
        JLabel registerErrorLabel = new JLabel();
        registerErrorLabel.setForeground(new Color(200, 50, 50));
        registerErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        registerErrorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registerCard.add(registerErrorLabel);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 20, 20, 20);
        panel.add(registerCard, gbc);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton registerSubmitButton = createPrimaryButton("Criar Conta");
        registerSubmitButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String username = regUsernameField.getText().trim();
                String password = new String(regPasswordField.getPassword()).trim();
                String adminPassword = new String(adminPasswordField.getPassword()).trim();

                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    registerErrorLabel.setText("Preencha todos os campos");
                    return;
                }

                authService.registerUser(username, password, name, adminPassword);
                JOptionPane.showMessageDialog(
                        LoginScreen.this,
                        "Conta criada com sucesso!\nAgora faça login.",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE
                );
                cardLayout.show(mainPanel, "login");
                nameField.setText("");
                regUsernameField.setText("");
                regPasswordField.setText("");
                adminPasswordField.setText("");
            } catch (AuthenticationException | ValidationException ex) {
                registerErrorLabel.setText(ex.getMessage());
            }
        });
        buttonPanel.add(registerSubmitButton);

        JButton backButton = createSecondaryButton("Voltar");
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "login");
            nameField.setText("");
            regUsernameField.setText("");
            regPasswordField.setText("");
            adminPasswordField.setText("");
            registerErrorLabel.setText("");
        });
        buttonPanel.add(backButton);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 20, 40, 20);
        panel.add(buttonPanel, gbc);

        // Espaço vazio no final
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalStrut(0), gbc);

        return panel;
    }

    /**
     * Realiza login
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Preencha usuário e senha");
            return;
        }

        try {
            authService.login(username, password);
            // Login bem-sucedido - notifica listener
            SwingUtilities.invokeLater(() -> {
                dispose();
                // Dispara evento para abrir MainDashboard
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

    // ===== Helper Methods =====

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(TEXT_FONT);
        field.setPreferredSize(new Dimension(300, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(BACKGROUND);
        button.setForeground(PRIMARY_BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /*
     * Callback para quando login é bem-sucedido
     */
    public interface LoginCallback {
        void onLoginSuccess(String username);
    }

    private LoginCallback loginCallback;

    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }
}
