package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.i18n.LanguageManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProtipoMainDashboard extends JFrame implements LanguageManager.LanguageChangeListener {

    private static final Color APP_BACKGROUND   = new Color(244, 245, 247);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR     = new Color(210, 214, 220);
    private static final Color TEXT_COLOR       = new Color(38, 43, 51);
    private static final Color MUTED_TEXT       = new Color(98, 108, 122);
    private static final Color ACTIVE_NAV       = new Color(218, 231, 245);
    private static final Color PRIMARY_BLUE     = new Color(50, 91, 140);
    private static final Color SECTION_RED      = new Color(200, 50, 50);
    private static final Font  BASE_FONT        = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  TITLE_FONT       = new Font("SansSerif", Font.BOLD, 20);
    private static final Font  SECTION_FONT     = new Font("SansSerif", Font.BOLD, 16);
    private static final Font  NAV_SECTION_FONT = new Font("SansSerif", Font.BOLD, 11);

    private final AuthenticationService authService;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);
    private final JLabel     pageTitle    = new JLabel();
    private final JLabel     pageSubtitle = new JLabel();
    private final JLabel     userLabel    = new JLabel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private JLabel productLabel;
    private JTextField searchField;
    private JButton    logoutButton;
    private JLabel     adminLabel;
    private JLabel     versionLabel;
    private final Map<String, String> navLabels    = new LinkedHashMap<>();
    private final Map<String, String> navSubtitles = new LinkedHashMap<>();

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
        right.add(createLanguageSelector());

        logoutButton = new JButton(LanguageManager.getInstance().translate("button.logout"));
        logoutButton.setFont(BASE_FONT);
        logoutButton.setBackground(SECTION_RED);
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
        JComboBox<String> combo = new JComboBox<>(new String[]{
                LanguageManager.getInstance().translate("language.pt"),
                LanguageManager.getInstance().translate("language.en"),
                LanguageManager.getInstance().translate("language.es")
        });
        combo.setFont(BASE_FONT);
        combo.setBackground(PANEL_BACKGROUND);
        combo.setPreferredSize(new Dimension(120, 28));
        combo.setSelectedIndex(LanguageManager.getInstance().getCurrentLanguage().ordinal());
        combo.addActionListener(e -> {
            LanguageManager.Language[] langs = LanguageManager.Language.values();
            LanguageManager.getInstance().setLanguage(langs[combo.getSelectedIndex()]);
        });
        panel.add(combo);
        return panel;
    }

    private JComponent buildNavigation() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(225, 0));
        nav.setBackground(new Color(233, 236, 241));
        nav.setBorder(new EmptyBorder(18, 14, 18, 14));

        // ── GERENTE ──────────────────────────────────────
        nav.add(sectionLabel("GERENTE"));
        nav.add(Box.createVerticalStrut(10));

        addNavButton(nav, "dashboard",    "menu.dashboard",    "menu.dashboard.subtitle");
        addNavButton(nav, "passageiros",  "menu.passengers",   "menu.passengers.subtitle");
        addNavButton(nav, "pontosColeta", "menu.pontosColeta", "menu.pontosColeta.subtitle");
        addNavButton(nav, "transfers",    "menu.transfers",    "menu.transfers.subtitle");
        addNavButton(nav, "ordens",       "menu.orders",       "menu.orders.subtitle");
        addNavButton(nav, "montarRota",   "menu.montarRota",   "menu.montarRota.subtitle");
        addNavButton(nav, "motoristas",   "menu.drivers",      "menu.drivers.subtitle");
        addNavButton(nav, "veiculos",     "menu.vehicles",     "menu.vehicles.subtitle");

        // ── MOTORISTA ─────────────────────────────────────
        nav.add(Box.createVerticalStrut(14));
        nav.add(sectionLabel("MOTORISTA"));
        nav.add(Box.createVerticalStrut(10));

        addNavButton(nav, "servicos", "menu.servicos", "menu.servicos.subtitle");

        // ── ADMIN ─────────────────────────────────────────
        if (authService.isAdmin()) {
            nav.add(Box.createVerticalStrut(14));
            adminLabel = sectionLabel("ADMIN");
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

    // Label de seção vermelho igual ao ADMIN
    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(SECTION_RED);
        label.setFont(NAV_SECTION_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addNavButton(JPanel nav, String key, String labelKey, String subtitleKey) {
        navLabels.put(key, labelKey);
        navSubtitles.put(key, subtitleKey);

        String label = LanguageManager.getInstance().translate(labelKey);
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
        button.addActionListener(e -> selectPage(key, labelKey, subtitleKey));

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
        heading.add(titleStack, BorderLayout.WEST);

        cardPanel.setBackground(APP_BACKGROUND);
        cardPanel.add(new DashboardPanel(),    "dashboard");
        cardPanel.add(new PassageirosPanel(),  "passageiros");
        cardPanel.add(new PontosColetaPanel(), "pontosColeta");
        cardPanel.add(new TransfersPanel(),    "transfers");
        cardPanel.add(new OrdensPanel(),       "ordens");
        cardPanel.add(new MontarRotaPanel(),   "montarRota");
        cardPanel.add(new MotoristasPanel(),   "motoristas");
        cardPanel.add(new VeiculosPanel(),     "veiculos");
        cardPanel.add(new ServicosPanel(),     "servicos");   // ✅ novo

        if (authService.isAdmin()) {
            cardPanel.add(buildAdminPage(), "admin");
        }

        main.add(heading,   BorderLayout.NORTH);
        main.add(cardPanel, BorderLayout.CENTER);
        return main;
    }

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
        productLabel.setText(LanguageManager.getInstance().translate("app.title"));
        searchField.setText(" " + LanguageManager.getInstance().translate("app.search.placeholder"));
        logoutButton.setText(LanguageManager.getInstance().translate("button.logout"));
        versionLabel.setText(LanguageManager.getInstance().translate("version"));
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            String lk = navLabels.get(entry.getKey());
            if (lk != null) entry.getValue().setText(LanguageManager.getInstance().translate(lk));
        }
        for (String key : navButtons.keySet()) {
            if (navButtons.get(key).getBackground().equals(ACTIVE_NAV)) {
                String tk = navLabels.get(key);
                String sk = navSubtitles.get(key);
                if (tk != null && sk != null) {
                    pageTitle.setText(LanguageManager.getInstance().translate(tk)
                            .replaceAll("[🚗👥🧑‍✈️🚙📋⚙️📊📍]", "").trim());
                    pageSubtitle.setText(LanguageManager.getInstance().translate(sk));
                }
                break;
            }
        }
    }

    private void performLogout() {
        LanguageManager lm = LanguageManager.getInstance();
        int confirm = JOptionPane.showConfirmDialog(this,
                lm.translate("dialog.confirm.logout"),
                lm.translate("dialog.confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginScreen ls = new LoginScreen(authService);
                ls.setLoginCallback(u -> new ProtipoMainDashboard(authService).setVisible(true));
                ls.setVisible(true);
            });
        }
    }

    // ── Helpers ──────────────────────────────────────────────
    private JPanel panel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(PANEL_BACKGROUND);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)));
        JLabel l = new JLabel(title);
        l.setFont(SECTION_FONT); l.setForeground(TEXT_COLOR);
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    private JComponent tablePanel(String title, DefaultTableModel model) {
        JPanel p = panel(title);
        JTable t = new JTable(model);
        t.setFillsViewportHeight(true); t.setRowHeight(28);
        t.setShowGrid(true); t.setGridColor(new Color(230, 232, 236));
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setFont(BASE_FONT);
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    private DefaultTableModel createTableModel(String[] cols, Object[][] rows) {
        return new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JButton primaryButton(String label) {
        JButton b = new JButton(label);
        b.setBackground(PRIMARY_BLUE); b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE), new EmptyBorder(8, 14, 8, 14)));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        return b;
    }

    private JButton outlineButton(String label) {
        JButton b = new JButton(label);
        b.setBackground(PANEL_BACKGROUND); b.setForeground(TEXT_COLOR);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(8, 14, 8, 14)));
        b.setFont(BASE_FONT);
        return b;
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "SOS VIALE", JOptionPane.INFORMATION_MESSAGE);
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.font",       BASE_FONT);
            UIManager.put("Label.font",        BASE_FONT);
            UIManager.put("TextField.font",    BASE_FONT);
            UIManager.put("ComboBox.font",     BASE_FONT);
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.select",     new Color(210, 214, 220));
        } catch (Exception ignored) {}
    }
}