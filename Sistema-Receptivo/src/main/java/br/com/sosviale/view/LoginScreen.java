package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.auth.SessionManager;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.offline.OfflineStore;
import br.com.sosviale.offline.dto.OfflineSessionDto;
import br.com.sosviale.i18n.I18nRegistry;
import br.com.sosviale.i18n.LanguageManager;
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
    private final boolean databaseDisponivel;
    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private LoginCallback loginCallback;
    private JLabel brandLabel;
    private JLabel loginTitleLabel;
    private JLabel loginSubtitleLabel;
    private JLabel usernameFieldLabel;
    private JLabel passwordFieldLabel;

    private JLabel registerTitleLabel;
    private JLabel registerSubtitleLabel;
    private JLabel registerFullnameLabel;
    private JLabel registerUsernameLabel;
    private JLabel registerPasswordLabel;
    private JLabel registerProfileLabel;
    private JLabel registerAdminPasswordLabel;
    private JButton registerSubmitButton;
    private JButton registerBackButton;
    private JComboBox<Perfil> registerPerfilCombo;

    public LoginScreen(AuthenticationService authService, boolean databaseDisponivel) {
        this.authService = authService;
        this.databaseDisponivel = databaseDisponivel;
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
        I18nRegistry.register(this::refreshTexts);
    }

    private void refreshTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        setTitle(lm.translate("login.title") + " - Login");
        if (brandLabel != null) {
            brandLabel.setText(lm.translate("app.title"));
        }
        if (loginTitleLabel != null) {
            loginTitleLabel.setText(lm.translate("login.access.title"));
        }
        if (loginSubtitleLabel != null) {
            loginSubtitleLabel.setText(lm.translate("login.access.subtitle"));
        }
        if (usernameFieldLabel != null) {
            usernameFieldLabel.setText(lm.translate("login.username.label"));
        }
        if (passwordFieldLabel != null) {
            passwordFieldLabel.setText(lm.translate("login.password.label"));
        }
        if (loginButton != null) {
            loginButton.setText(lm.translate("login.button.login"));
        }
        if (registerButton != null) {
            registerButton.setText(lm.translate("login.create.account"));
        }
        if (registerTitleLabel != null) {
            registerTitleLabel.setText(lm.translate("register.title"));
        }
        if (registerSubtitleLabel != null) {
            registerSubtitleLabel.setText(lm.translate("register.subtitle"));
        }
        if (registerFullnameLabel != null) {
            registerFullnameLabel.setText(lm.translate("register.fullname.label"));
        }
        if (registerUsernameLabel != null) {
            registerUsernameLabel.setText(lm.translate("register.username.label"));
        }
        if (registerPasswordLabel != null) {
            registerPasswordLabel.setText(lm.translate("register.password.label"));
        }
        if (registerProfileLabel != null) {
            registerProfileLabel.setText(lm.translate("register.profile.label"));
        }
        if (registerAdminPasswordLabel != null) {
            registerAdminPasswordLabel.setText(lm.translate("register.adminPassword.label"));
        }
        if (registerSubmitButton != null) {
            registerSubmitButton.setText(lm.translate("register.button.register"));
        }
        if (registerBackButton != null) {
            registerBackButton.setText(lm.translate("register.button.back"));
        }
        if (registerPerfilCombo != null) {
            registerPerfilCombo.repaint();
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = createScreenPanel();

        panel.add(createHeader(), BorderLayout.NORTH);

        JPanel content = createContentPanel();

        JPanel loginCard = createCard();

        usernameField = createTextField();
        usernameField.setText("admin");
        usernameFieldLabel = addField(loginCard, LanguageManager.getInstance().translate("login.username.label"), usernameField, 0);

        passwordField = createPasswordField(BORDER);
        passwordField.setText("admin123");
        passwordField.addActionListener(e -> performLogin());
        passwordFieldLabel = addField(loginCard, LanguageManager.getInstance().translate("login.password.label"), passwordField, 1);

        errorLabel = createMessageLabel();
        addMessage(loginCard, errorLabel, 2);

        JPanel buttonPanel = createButtonPanel();

        loginButton = createPrimaryButton(LanguageManager.getInstance().translate("login.button.login"));
        loginButton.addActionListener(e -> performLogin());
        buttonPanel.add(loginButton);

        registerButton = createSecondaryButton(LanguageManager.getInstance().translate("login.create.account"));
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        buttonPanel.add(registerButton);

        if (!databaseDisponivel && OfflineStore.getInstance().hasAnySnapshot()) {
            JButton offlineBtn = createSecondaryButton(
                    LanguageManager.getInstance().translate("login.button.offline"));
            offlineBtn.addActionListener(e -> performOfflineLogin());
            buttonPanel.add(offlineBtn);
        }

        content.add(loginCard);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);

        panel.add(createCenteredWrapper(content), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = createScreenPanel();

        panel.add(createHeader(), BorderLayout.NORTH);

        JPanel content = createRegisterContentPanel();

        JPanel registerCard = createCard();

        LanguageManager lm = LanguageManager.getInstance();

        JTextField nameField = createTextField();
        registerFullnameLabel = addField(registerCard, lm.translate("register.fullname.label"), nameField, 0);

        JTextField regUsernameField = createTextField();
        registerUsernameLabel = addField(registerCard, lm.translate("register.username.label"), regUsernameField, 1);

        JPasswordField regPasswordField = createPasswordField(BORDER);
        registerPasswordLabel = addField(registerCard, lm.translate("register.password.label"), regPasswordField, 2);

        registerPerfilCombo = new JComboBox<>(new Perfil[]{
                Perfil.ADMIN, Perfil.GERENTE, Perfil.MOTORISTA
        });
        registerPerfilCombo.setFont(TEXT_FONT);
        registerPerfilCombo.setBackground(WHITE);
        registerPerfilCombo.setPreferredSize(new Dimension(520, 40));
        registerPerfilCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Perfil perfil) {
                    setText(LanguageManager.getInstance().translate("perfil." + perfil.name()));
                }
                return this;
            }
        });
        registerProfileLabel = addField(registerCard, lm.translate("register.profile.label"), registerPerfilCombo, 3);

        JPasswordField adminPasswordField = createPasswordField(ERROR);
        registerAdminPasswordLabel = addField(
                registerCard, lm.translate("register.adminPassword.label"), adminPasswordField, 4, ERROR);

        JLabel registerErrorLabel = createMessageLabel();
        addMessage(registerCard, registerErrorLabel, 5);

        JPanel buttonPanel = createButtonPanel();

        registerSubmitButton = createPrimaryButton(lm.translate("register.button.register"));
        registerSubmitButton.addActionListener(e -> performRegistration(
                nameField, regUsernameField, regPasswordField, adminPasswordField, registerPerfilCombo, registerErrorLabel));
        buttonPanel.add(registerSubmitButton);

        registerBackButton = createSecondaryButton(lm.translate("register.button.back"));
        registerBackButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        buttonPanel.add(registerBackButton);

        content.add(registerCard);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);

        panel.add(createCenteredWrapper(content), BorderLayout.CENTER);

        return panel;
    }

    private void performRegistration(
            JTextField nameField,
            JTextField regUsernameField,
            JPasswordField regPasswordField,
            JPasswordField adminPasswordField,
            JComboBox<Perfil> perfilCombo,
            JLabel registerErrorLabel
    ) {
        try {
            String name = nameField.getText().trim();
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword()).trim();
            String adminPassword = new String(adminPasswordField.getPassword()).trim();
            Perfil perfil = (Perfil) perfilCombo.getSelectedItem();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                registerErrorLabel.setText(LanguageManager.getInstance().translate("register.validation.required"));
                return;
            }

            userService.registrar(name, username, password, adminPassword, perfil);

            JOptionPane.showMessageDialog(
                    LoginScreen.this,
                    LanguageManager.getInstance().translate("register.success.message"),
                    LanguageManager.getInstance().translate("register.success.title"),
                    JOptionPane.INFORMATION_MESSAGE
            );

            cardLayout.show(mainPanel, "login");
        } catch (AuthenticationException | ValidationException ex) {
            registerErrorLabel.setText(ex.getMessage());
        }
    }

    private void performOfflineLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            errorLabel.setText(LanguageManager.getInstance().translate("login.offline.need.user"));
            return;
        }
        OfflineSessionDto session = OfflineStore.getInstance().loadSession(username)
                .or(() -> OfflineStore.getInstance().loadAnySession())
                .orElse(null);
        if (session == null || !OfflineStore.getInstance().hasSnapshot(session.getUsuario())) {
            errorLabel.setText(LanguageManager.getInstance().translate("login.offline.no.cache"));
            return;
        }
        SessionManager.getInstance().iniciarSessaoOffline(
                session.getUsuario(),
                session.getNome(),
                Perfil.valueOf(session.getPerfil()),
                session.isAdmin()
        );
        SwingUtilities.invokeLater(() -> {
            dispose();
            if (loginCallback != null) {
                loginCallback.onLoginSuccess(session.getUsuario());
            }
        });
    }

    private void performLogin() {
        if (!databaseDisponivel) {
            errorLabel.setText(LanguageManager.getInstance().translate("login.offline.no.network"));
            performOfflineLogin();
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText(LanguageManager.getInstance().translate("login.validation.fill.credentials"));
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

        brandLabel = new JLabel(LanguageManager.getInstance().translate("app.title"));
        brandLabel.setFont(BRAND_FONT);
        brandLabel.setForeground(TEXT);

        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        langPanel.setOpaque(false);
        JComboBox<String> langCombo = new JComboBox<>(new String[]{
                LanguageManager.getInstance().translate("language.pt"),
                LanguageManager.getInstance().translate("language.en"),
                LanguageManager.getInstance().translate("language.es")
        });
        langCombo.setSelectedIndex(LanguageManager.getInstance().getCurrentLanguage().ordinal());
        langCombo.addActionListener(e -> {
            LanguageManager.Language[] langs = LanguageManager.Language.values();
            LanguageManager.getInstance().setLanguage(langs[langCombo.getSelectedIndex()]);
        });
        langPanel.add(langCombo);

        header.add(brandLabel, BorderLayout.WEST);
        header.add(langPanel, BorderLayout.EAST);

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

    private JPanel createRegisterContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(0, 0, 34, 0));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.setMaximumSize(new Dimension(620, Integer.MAX_VALUE));

        registerTitleLabel = new JLabel(LanguageManager.getInstance().translate("register.title"));
        registerTitleLabel.setFont(TITLE_FONT);
        registerTitleLabel.setForeground(TEXT);
        registerTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerSubtitleLabel = new JLabel(LanguageManager.getInstance().translate("register.subtitle"));
        registerSubtitleLabel.setFont(SUBTITLE_FONT);
        registerSubtitleLabel.setForeground(MUTED_TEXT);
        registerSubtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(registerTitleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(registerSubtitleLabel);
        content.add(Box.createVerticalStrut(20));
        return content;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(0, 0, 34, 0));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.setMaximumSize(new Dimension(620, Integer.MAX_VALUE));

        loginTitleLabel = new JLabel(LanguageManager.getInstance().translate("login.access.title"));
        loginTitleLabel.setFont(TITLE_FONT);
        loginTitleLabel.setForeground(TEXT);
        loginTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginSubtitleLabel = new JLabel(LanguageManager.getInstance().translate("login.access.subtitle"));
        loginSubtitleLabel.setFont(SUBTITLE_FONT);
        loginSubtitleLabel.setForeground(MUTED_TEXT);
        loginSubtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(loginTitleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(loginSubtitleLabel);
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

    private JLabel addField(JPanel panel, String labelText, JComponent field, int row) {
        return addField(panel, labelText, field, row, MUTED_TEXT);
    }

    private JLabel addField(JPanel panel, String labelText, JComponent field, int row, Color labelColor) {
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
        return label;
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
