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
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);

    private final AuthenticationService authService;
    private final UserService userService = new UserService();
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
        setSize(450, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND);

        mainPanel.add("login", createLoginPanel());
        mainPanel.add("register", createRegisterPanel());

        setContentPane(mainPanel);
        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

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

        JPanel loginCard = createCard();

        JLabel usernameLabel = new JLabel("Usuário:");
        usernameLabel.setFont(LABEL_FONT);
        usernameLabel.setForeground(new Color(38, 43, 51));
        loginCard.add(usernameLabel);

        usernameField = createTextField();
        usernameField.setText("admin");
        loginCard.add(usernameField);

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

        errorLabel = new JLabel();
        errorLabel.setForeground(new Color(200, 50, 50));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginCard.add(errorLabel);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        panel.add(loginCard, gbc);

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

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel titleLabel = new JLabel("Criar Conta");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(titleLabel, gbc);

        JPanel registerCard = createCard();

        JLabel nameLabel = new JLabel("Nome completo:");
        nameLabel.setFont(LABEL_FONT);
        registerCard.add(nameLabel);

        JTextField nameField = createTextField();
        registerCard.add(nameField);

        JLabel usernameLabel = new JLabel("Usuário:");
        usernameLabel.setFont(LABEL_FONT);
        registerCard.add(usernameLabel);

        JTextField regUsernameField = createTextField();
        registerCard.add(regUsernameField);

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

        JLabel perfilLabel = new JLabel("Perfil:");
        perfilLabel.setFont(LABEL_FONT);
        registerCard.add(perfilLabel);

        JComboBox<Perfil> perfilCombo = new JComboBox<>(new Perfil[]{
                Perfil.ATENDENTE, Perfil.GERENTE, Perfil.MOTORISTA
        });
        perfilCombo.setFont(TEXT_FONT);
        perfilCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerCard.add(perfilCombo);

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

        JLabel registerErrorLabel = new JLabel();
        registerErrorLabel.setForeground(new Color(200, 50, 50));
        registerErrorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        registerErrorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registerCard.add(registerErrorLabel);

        gbc.gridy = 1;
        gbc.insets = new Insets(20, 20, 20, 20);
        panel.add(registerCard, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

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
                        "Conta criada com sucesso!\nAgora faça login.",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE
                );
                cardLayout.show(mainPanel, "login");
                nameField.setText("");
                regUsernameField.setText("");
                regPasswordField.setText("");
                adminPasswordField.setText("");
                registerErrorLabel.setText("");
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

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalStrut(0), gbc);

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
        button.setOpaque(true);
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
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public interface LoginCallback {
        void onLoginSuccess(String username);
    }

    private LoginCallback loginCallback;

    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }
}