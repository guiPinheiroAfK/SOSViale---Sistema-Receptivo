package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.i18n.LanguageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProtipoMainDashboard extends JFrame implements LanguageManager.LanguageChangeListener {

    private static final Color APP_BACKGROUND  = new Color(244, 245, 247);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR    = new Color(210, 214, 220);
    private static final Color TEXT_COLOR      = new Color(38, 43, 51);
    private static final Color MUTED_TEXT      = new Color(98, 108, 122);
    private static final Color ACTIVE_NAV      = new Color(218, 231, 245);
    private static final Color PRIMARY_BLUE    = new Color(50, 91, 140);
    private static final Font  BASE_FONT       = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  TITLE_FONT      = new Font("SansSerif", Font.BOLD, 20);
    private static final Font  SECTION_FONT    = new Font("SansSerif", Font.BOLD, 16);

    private final AuthenticationService authService;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);
    private final JLabel     pageTitle    = new JLabel();
    private final JLabel     pageSubtitle = new JLabel();
    private final JLabel     userLabel    = new JLabel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private JLabel      productLabel;
    private JTextField  searchField;
    private JButton     logoutButton;
    private JLabel      menuLabel;
    private JLabel      versionLabel;
    private JLabel      adminLabel;
    private Map<String, String> navLabels    = new LinkedHashMap<>();
    private Map<String, String> navSubtitles = new LinkedHashMap<>();

    public ProtipoMainDashboard(AuthenticationService authService) {
        this.authService = authService;
        configureLookAndFeel();

        setTitle("SOS VIALE - Sistema Receptivo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setContentPane(buildShell());
        setLocationRelativeTo(null);

        LanguageManager.getInstance().addLanguageChangeListener(this);
        selectPage("dashboard", "menu.dashboard", "menu.dashboard.subtitle");
    }

    private JComponent buildShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(APP_BACKGROUND);
        root.add(buildHeader(),     BorderLayout.NORTH);
        root.add(buildNavigation(), BorderLayout.WEST);
        root.add(buildMainArea(),   BorderLayout.CENTER);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(PANEL_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 18, 14, 18)
        ));

        productLabel = new JLabel(LanguageManager.getInstance().translate("app.title"));
        productLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        productLabel.setForeground(TEXT_COLOR);

        searchField = new JTextField(" " + LanguageManager.getInstance().translate("app.search.placeholder"));
        searchField.setPreferredSize(new Dimension(360, 34));
        searchField.setForeground(MUTED_TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        userLabel.setText("👤 " + authService.getCurrentUser());
        userLabel.setFont(BASE_FONT);
        userLabel.setForeground(MUTED_TEXT);
        right.add(userLabel);

        JPanel languagePanel = createLanguageSelector();
        right.add(languagePanel);

        logoutButton = new JButton(LanguageManager.getInstance().translate("button.logout"));
        logoutButton.setFont(BASE_FONT);
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        logoutButton.addActionListener(e -> performLogout());
        right.add(logoutButton);

        header.add(productLabel, BorderLayout.WEST);
        header.add(searchField,  BorderLayout.CENTER);
        header.add(right,        BorderLayout.EAST);

        return header;
    }

    private JPanel createLanguageSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        panel.setOpaque(false);

        JLabel langLabel = new JLabel(LanguageManager.getInstance().translate("language.label") + ":");
        langLabel.setFont(BASE_FONT);
        langLabel.setForeground(MUTED_TEXT);
        panel.add(langLabel);

        JComboBox<String> languageCombo = new JComboBox<>(new String[]{
                LanguageManager.getInstance().translate("language.pt"),
                LanguageManager.getInstance().translate("language.en"),
                LanguageManager.getInstance().translate("language.es")
        });
        languageCombo.setFont(BASE_FONT);
        languageCombo.setBackground(PANEL_BACKGROUND);
        languageCombo.setPreferredSize(new Dimension(120, 28));
        languageCombo.setSelectedIndex(LanguageManager.getInstance().getCurrentLanguage().ordinal());
        languageCombo.addActionListener(e -> {
            int selectedIndex = languageCombo.getSelectedIndex();
            LanguageManager.Language[] languages = LanguageManager.Language.values();
            LanguageManager.getInstance().setLanguage(languages[selectedIndex]);
        });

        panel.add(languageCombo);
        return panel;
    }

    private JComponent buildNavigation() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(225, 0));
        nav.setBackground(new Color(233, 236, 241));
        nav.setBorder(new EmptyBorder(18, 14, 18, 14));

        menuLabel = new JLabel(LanguageManager.getInstance().translate("menu.modules"));
        menuLabel.setForeground(MUTED_TEXT);
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menuLabel);
        nav.add(Box.createVerticalStrut(10));

        addNavButton(nav, "dashboard",    "menu.dashboard",       "menu.dashboard.subtitle");
        addNavButton(nav, "transfers",    "menu.transfers",       "menu.transfers.subtitle");
        addNavButton(nav, "passageiros",  "menu.passengers",      "menu.passengers.subtitle");
        addNavButton(nav, "motoristas",   "menu.drivers",         "menu.drivers.subtitle");
        addNavButton(nav, "veiculos",     "menu.vehicles",        "menu.vehicles.subtitle");
        addNavButton(nav, "ordens",       "menu.orders",          "menu.orders.subtitle");
        // ✅ Novo botão Montar Rota
        addNavButton(nav, "montarRota",   "menu.montarRota",      "menu.montarRota.subtitle");
        addNavButton(nav, "pontosColeta", "menu.pontosColeta",    "menu.pontosColeta.subtitle");

        if (authService.isAdmin()) {
            nav.add(Box.createVerticalStrut(14));
            adminLabel = new JLabel(LanguageManager.getInstance().translate("menu.admin"));
            adminLabel.setForeground(new Color(200, 50, 50));
            adminLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            nav.add(adminLabel);
            nav.add(Box.createVerticalStrut(10));
            addNavButton(nav, "admin", "menu.users", "menu.users.subtitle");
        }

        nav.add(Box.createVerticalGlue());
        nav.add(new JSeparator());
        nav.add(Box.createVerticalStrut(12));

        versionLabel = new JLabel(LanguageManager.getInstance().translate("version"));
        versionLabel.setForeground(MUTED_TEXT);
        versionLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(versionLabel);

        return nav;
    }

    private void addNavButton(JPanel nav, String key, String labelKey, String subtitleKey) {
        String label = LanguageManager.getInstance().translate(labelKey);

        navLabels.put(key, labelKey);
        navSubtitles.put(key, subtitleKey);

        JButton button = new JButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));
        button.setBackground(PANEL_BACKGROUND);
        button.setForeground(TEXT_COLOR);
        button.setFont(BASE_FONT);

        button.addActionListener(event -> selectPage(key, labelKey, subtitleKey));
        navButtons.put(key, button);
        nav.add(button);
        nav.add(Box.createVerticalStrut(8));
    }

    private JComponent buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(APP_BACKGROUND);
        main.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        pageTitle.setFont(TITLE_FONT);
        pageTitle.setForeground(TEXT_COLOR);
        pageSubtitle.setFont(BASE_FONT);
        pageSubtitle.setForeground(MUTED_TEXT);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(pageTitle);
        titleStack.add(Box.createVerticalStrut(4));
        titleStack.add(pageSubtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        heading.add(titleStack, BorderLayout.WEST);
        heading.add(actions,    BorderLayout.EAST);

        cardPanel.setBackground(APP_BACKGROUND);
        cardPanel.add(buildDashboardPage(),    "dashboard");
        cardPanel.add(buildPassengersPage(),   "passageiros");
        cardPanel.add(buildDriversPage(),      "motoristas");
        cardPanel.add(buildVehiclesPage(),     "veiculos");
        cardPanel.add(buildOrdersPage(),       "ordens");
        cardPanel.add(buildTransferPage(),     "transfers");
        cardPanel.add(buildMontarRotaPage(),   "montarRota");   // ✅ novo card
        cardPanel.add(buildPontosColetaPage(), "pontosColeta");
        if (authService.isAdmin()) {
            cardPanel.add(buildAdminPage(), "admin");
        }

        main.add(heading,   BorderLayout.NORTH);
        main.add(cardPanel, BorderLayout.CENTER);
        return main;
    }

    // PÁGINAS
    private JComponent buildDashboardPage()    { return new DashboardPanel(); }
    private JComponent buildPassengersPage()   { return new PassageirosPanel(); }
    private JComponent buildDriversPage()      { return new MotoristasPanel(); }
    private JComponent buildVehiclesPage()     { return new VeiculosPanel(); }
    private JComponent buildOrdersPage()       { return new OrdensPanel(); }
    private JComponent buildPontosColetaPage() { return new PontosColetaPanel(); }
    private JComponent buildTransferPage()     { return new TransfersPanel(); }
    private JComponent buildMontarRotaPage()   { return new MontarRotaPanel(); } // ✅ novo

    private JComponent buildAdminPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        DefaultTableModel model = createTableModel(
                new String[]{"Usuário", "Nome", "Tipo", "Criado em"},
                new Object[][]{{"admin", "Administrador", "ADMIN", "2024-01-15"}}
        );

        JComponent table = tablePanel("Usuários do Sistema", model);
        JButton refreshButton = outlineButton("Atualizar");
        refreshButton.addActionListener(e -> showMessage("Usuários: 1 admin (você)"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(outlineButton("Deletar usuário"));
        actions.add(primaryButton("+ Novo usuário"));

        page.add(actions, BorderLayout.NORTH);
        page.add(table,   BorderLayout.CENTER);
        return page;
    }

    // HELPERS
    private JComponent splitPage(JComponent left, JComponent right) {
        JPanel page = new JPanel(new BorderLayout(14, 0));
        page.setOpaque(false);
        left.setPreferredSize(new Dimension(345, 0));
        page.add(left,  BorderLayout.WEST);
        page.add(right, BorderLayout.CENTER);
        return page;
    }

    private JPanel formPanel(String title) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BACKGROUND);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));
        JLabel label = new JLabel(title);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 14, 0);
        form.add(label, c);
        return form;
    }

    private JPanel panel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));
        panel.setLayout(new BorderLayout(0, 12));
        JLabel label = new JLabel(title);
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JComponent tablePanel(String title, DefaultTableModel model) {
        JPanel panel = panel(title);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 232, 236));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(BASE_FONT);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JComponent tablePanel(String title, String[] columns, Object[][] rows) {
        return tablePanel(title, createTableModel(columns, rows));
    }

    private DefaultTableModel createTableModel(String[] columns, Object[][] rows) {
        return new DefaultTableModel(rows, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void addField(JPanel form, String label, JComponent input, int row) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = row * 2 + 1; lc.weightx = 1;
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.insets = new Insets(row == 0 ? 0 : 10, 0, 4, 0);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(MUTED_TEXT);
        labelComponent.setFont(BASE_FONT);
        form.add(labelComponent, lc);
        GridBagConstraints ic = new GridBagConstraints();
        ic.gridx = 0; ic.gridy = row * 2 + 2; ic.weightx = 1;
        ic.fill = GridBagConstraints.HORIZONTAL;
        form.add(input, ic);
    }

    private void addActions(JPanel form, JButton primary, JButton secondary) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(primary);
        actions.add(secondary);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 99; c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.weighty = 1; c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(18, 0, 0, 0);
        form.add(actions, c);
    }

    private JTextField textField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BASE_FONT);
        field.setPreferredSize(new Dimension(0, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return field;
    }

    private JComboBox<String> combo(String... values) {
        JComboBox<String> comboBox = new JComboBox<>(values);
        comboBox.setFont(BASE_FONT);
        comboBox.setPreferredSize(new Dimension(0, 34));
        comboBox.setBackground(PANEL_BACKGROUND);
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return comboBox;
    }

    private JButton primaryButton(String label) {
        JButton button = new JButton(label);
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        return button;
    }

    private JButton outlineButton(String label) {
        JButton button = new JButton(label);
        button.setBackground(PANEL_BACKGROUND);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 14, 8, 14)
        ));
        button.setFont(BASE_FONT);
        return button;
    }

    private void selectPage(String key, String titleKey, String subtitleKey) {
        String title    = LanguageManager.getInstance().translate(titleKey)
                .replaceAll("[🚗👥🧑‍✈️🚙📋⚙️📊📍]", "").trim();
        String subtitle = LanguageManager.getInstance().translate(subtitleKey);

        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        cardLayout.show(cardPanel, key);

        navButtons.forEach((navKey, button) -> {
            boolean active = navKey.equals(key);
            button.setBackground(active ? ACTIVE_NAV : PANEL_BACKGROUND);
            button.setForeground(active ? PRIMARY_BLUE : TEXT_COLOR);
        });
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        updateUIText();
    }

    private void updateUIText() {
        productLabel.setText(LanguageManager.getInstance().translate("app.title"));
        searchField.setText(" " + LanguageManager.getInstance().translate("app.search.placeholder"));
        logoutButton.setText(LanguageManager.getInstance().translate("button.logout"));
        menuLabel.setText(LanguageManager.getInstance().translate("menu.modules"));
        if (adminLabel != null)
            adminLabel.setText(LanguageManager.getInstance().translate("menu.admin"));
        versionLabel.setText(LanguageManager.getInstance().translate("version"));

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            String labelKey = navLabels.get(entry.getKey());
            if (labelKey != null)
                entry.getValue().setText(LanguageManager.getInstance().translate(labelKey));
        }

        for (String key : navButtons.keySet()) {
            if (navButtons.get(key).getBackground().equals(ACTIVE_NAV)) {
                String titleKey    = navLabels.get(key);
                String subtitleKey = navSubtitles.get(key);
                if (titleKey != null && subtitleKey != null) {
                    pageTitle.setText(LanguageManager.getInstance().translate(titleKey)
                            .replaceAll("[🚗👥🧑‍✈️🚙📋⚙️📊📍]", "").trim());
                    pageSubtitle.setText(LanguageManager.getInstance().translate(subtitleKey));
                }
                break;
            }
        }
    }

    private void performLogout() {
        LanguageManager lm = LanguageManager.getInstance();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                lm.translate("dialog.confirm.logout"),
                lm.translate("dialog.confirm.title"),
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginScreen newLoginScreen = new LoginScreen(authService);
                newLoginScreen.setLoginCallback(username -> {
                    ProtipoMainDashboard newDashboard = new ProtipoMainDashboard(authService);
                    newDashboard.setVisible(true);
                });
                newLoginScreen.setVisible(true);
            });
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "SOS VIALE", JOptionPane.INFORMATION_MESSAGE);
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.font",    BASE_FONT);
            UIManager.put("Label.font",     BASE_FONT);
            UIManager.put("TextField.font", BASE_FONT);
            UIManager.put("ComboBox.font",  BASE_FONT);
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.select",  new Color(210, 214, 220));
        } catch (Exception ignored) {}
    }
}