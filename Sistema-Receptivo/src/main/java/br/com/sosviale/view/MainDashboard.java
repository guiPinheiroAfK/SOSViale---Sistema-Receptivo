package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.auth.SessionManager;
import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MainDashboard extends JFrame implements LanguageManager.LanguageChangeListener {

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

    private static final Set<String> NAV_GERENTE = Set.of(
            "dashboard", "passageiros", "pontosColeta", "transfers", "ordens",
            "motoristas", "veiculos"
    );
    private static final Set<String> NAV_MOTORISTA = Set.of("servicos");
    private static final Set<String> NAV_ADMIN = Set.of("admin");

    private static final String MSG_SEM_PERMISSAO =
            "Você não tem permissão para acessar.";

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

    private ServicosPanel   servicosPanel;
    private UsuariosPanel   usuariosPanel;

    public MainDashboard(AuthenticationService authService, TransferService transferService) {
        this.authService = authService;
        configureLookAndFeel();
        setTitle("SOS VIALE - Sistema Receptivo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1250, 750));
        setContentPane(buildShell());
        setLocationRelativeTo(null);
        LanguageManager.getInstance().addLanguageChangeListener(this);

        //NotificationService notificationService = new NotificationService(transferService);
        //notificationService.startMonitoring();

        abrirPaginaInicial();
    }

    private void abrirPaginaInicial() {
        if (podeAcessar("dashboard")) {
            selectPage("dashboard", navLabels.get("dashboard"), navSubtitles.get("dashboard"));
        } else if (podeAcessar("servicos")) {
            selectPage("servicos", navLabels.get("servicos"), navSubtitles.get("servicos"));
        } else {
            navButtons.keySet().stream()
                    .filter(this::podeAcessar)
                    .findFirst()
                    .ifPresent(key -> selectPage(key, navLabels.get(key), navSubtitles.get(key)));
        }
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
        var user = authService.getCurrentUser();
        String nome = user != null ? user.getNome() : SessionManager.getInstance().getNomeAtual();
        String perfilTxt = user != null && user.getPerfil() != null ? user.getPerfil().name() : "—";
        userLabel.setText("👤 " + nome + " (" + perfilTxt + ")");
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
        nav.setPreferredSize(new Dimension(245, 0));
        nav.setBackground(new Color(233, 236, 241));
        nav.setBorder(new EmptyBorder(18, 14, 18, 14));

        // ── GERENTE ──────────────────────────────────────
        nav.add(sectionLabel("GERENTE"));
        nav.add(Box.createVerticalStrut(10));

        addNavButton(nav, "dashboard",    "📊 Painel Inicial",    "menu.dashboard.subtitle");
        addNavButton(nav, "passageiros",  "👥 Passageiros",      "menu.passengers.subtitle");
        addNavButton(nav, "pontosColeta", "📍 Pontos de Coleta", "menu.pontosColeta.subtitle");
        addNavButton(nav, "transfers",    "📋 Transfers",        "menu.transfers.subtitle");
        addNavButton(nav, "ordens",       "📦 Ordens de Serviço", "menu.orders.subtitle");

        /*addNavButton(nav, "montarRota",   "📝 Atribuir OS a Transfer", "menu.montarRota.subtitle");
        */
        addNavButton(nav, "motoristas",   "🧑‍✈️ Motoristas",      "menu.drivers.subtitle");
        addNavButton(nav, "veiculos",     "🚐 Veículos",        "menu.vehicles.subtitle");

        // ── MOTORISTA ─────────────────────────────────────
        nav.add(Box.createVerticalStrut(14));
        nav.add(sectionLabel("MOTORISTA"));
        nav.add(Box.createVerticalStrut(10));

        addNavButton(nav, "servicos",     "🛠️ Serviços",         "menu.servicos.subtitle");

        // ── ADMIN ─────────────────────────────────────────
        nav.add(Box.createVerticalStrut(14));
        adminLabel = sectionLabel("ADMIN");
        nav.add(adminLabel);
        nav.add(Box.createVerticalStrut(10));
        addNavButton(nav, "admin", "⚙️ Usuários", "menu.users.subtitle");

        nav.add(Box.createVerticalGlue());
        atualizarEstiloBotoesNav();
        nav.add(new JSeparator());
        nav.add(Box.createVerticalStrut(12));

        versionLabel = new JLabel(LanguageManager.getInstance().translate("version"));
        versionLabel.setForeground(MUTED_TEXT);
        versionLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(versionLabel);

        return nav;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(SECTION_RED);
        label.setFont(NAV_SECTION_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addNavButton(JPanel nav, String key, String labelText, String subtitleKey) {
        navLabels.put(key, labelText);
        navSubtitles.put(key, subtitleKey);

        JButton button = new JButton(labelText);
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
        button.addActionListener(e -> {
            if (!podeAcessar(key)) {
                JOptionPane.showMessageDialog(this,
                        MSG_SEM_PERMISSAO,
                        "Acesso negado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectPage(key, labelText, subtitleKey);
        });

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
        cardPanel.add(new OrdemServicoUnifiedPanel(),       "ordens");

        cardPanel.add(new AtribuirOS_TransferPanel(), "OrdensVerdadeiras");

        cardPanel.add(new MotoristasPanel(),   "motoristas");
        cardPanel.add(new VeiculosPanel(),     "veiculos");
        servicosPanel = new ServicosPanel();
        cardPanel.add(servicosPanel, "servicos");

        usuariosPanel = new UsuariosPanel();
        cardPanel.add(usuariosPanel, "admin");

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

    /**
     * Motorista: só Serviços. Gerente: Gerente + Motorista. Admin: tudo.
     */
    private boolean podeAcessar(String key) {
        if (SessionManager.getInstance().isAdmin()) return true;

        Perfil perfil = SessionManager.getInstance().getPerfilAtual();
        if (perfil == Perfil.ADMIN) return true;

        if (NAV_ADMIN.contains(key)) return false;

        if (perfil == Perfil.MOTORISTA) {
            return NAV_MOTORISTA.contains(key);
        }
        if (perfil == Perfil.GERENTE) {
            return NAV_GERENTE.contains(key) || NAV_MOTORISTA.contains(key);
        }
        return false;
    }

    private void atualizarEstiloBotoesNav() {
        navButtons.forEach((key, button) -> {
            boolean liberado = podeAcessar(key);
            String rotulo = navLabels.get(key);
            button.setText(liberado ? rotulo : "🔒 " + rotulo);
            button.setForeground(liberado ? TEXT_COLOR : MUTED_TEXT);
            button.setToolTipText(liberado ? null : MSG_SEM_PERMISSAO);
        });
    }

    private void selectPage(String key, String titleText, String subtitleKey) {
        if (!podeAcessar(key)) {
            JOptionPane.showMessageDialog(this,
                    MSG_SEM_PERMISSAO,
                    "Acesso negado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpa os emojis do título para o header (inclui cadeado)
        String cleanTitle = titleText.replaceAll("[🛠️📊👥📍📋📦📝🧑‍✈️🚐⚙️🔒]", "").trim();
        String subtitle = LanguageManager.getInstance().translate(subtitleKey);

        pageTitle.setText(cleanTitle);
        pageSubtitle.setText(subtitle);
        cardLayout.show(cardPanel, key);

        if ("servicos".equals(key) && servicosPanel != null) {
            servicosPanel.atualizar();
        }
        if ("admin".equals(key) && usuariosPanel != null) {
            usuariosPanel.atualizar();
        }

        atualizarEstiloBotoesNav();
        JButton ativo = navButtons.get(key);
        if (ativo != null) {
            ativo.setBackground(ACTIVE_NAV);
            ativo.setForeground(PRIMARY_BLUE);
        }
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        productLabel.setText(LanguageManager.getInstance().translate("app.title"));
        searchField.setText(" " + LanguageManager.getInstance().translate("app.search.placeholder"));
        logoutButton.setText(LanguageManager.getInstance().translate("button.logout"));
        versionLabel.setText(LanguageManager.getInstance().translate("version"));
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
                TransferService transferService = new TransferService();
                ls.setLoginCallback(u -> new MainDashboard(authService, transferService).setVisible(true));
                ls.setVisible(true);
            });
        }
    }

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