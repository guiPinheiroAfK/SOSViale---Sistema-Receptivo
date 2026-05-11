package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * MainDashboard - Interface principal do SOS VIALE
 * Integrada com autenticação JWT e design moderno
 */
public class ProtipoMainDashboard extends JFrame {

    private static final Color APP_BACKGROUND = new Color(244, 245, 247);
    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color ACTIVE_NAV = new Color(218, 231, 245);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private final AuthenticationService authService;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();
    private final JLabel userLabel = new JLabel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public ProtipoMainDashboard(AuthenticationService authService) {
        this.authService = authService;
        configureLookAndFeel();

        setTitle("SOS VIALE - Sistema Receptivo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setContentPane(buildShell());
        setLocationRelativeTo(null);

        selectPage("dashboard", "Painel Inicial", "Resumo operacional do dia");
    }

    private JComponent buildShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(APP_BACKGROUND);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildNavigation(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);
        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(PANEL_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel product = new JLabel("SOS VIALE | Sistema Receptivo");
        product.setFont(new Font("SansSerif", Font.BOLD, 18));
        product.setForeground(TEXT_COLOR);

        JTextField search = new JTextField(" Buscar transfer, passageiro ou OS...");
        search.setPreferredSize(new Dimension(360, 34));
        search.setForeground(MUTED_TEXT);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        userLabel.setText("👤 " + authService.getCurrentUser());
        userLabel.setFont(BASE_FONT);
        userLabel.setForeground(MUTED_TEXT);
        right.add(userLabel);

        JButton logoutButton = new JButton("Sair");
        logoutButton.setFont(BASE_FONT);
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        logoutButton.addActionListener(e -> performLogout());
        right.add(logoutButton);

        header.add(product, BorderLayout.WEST);
        header.add(search, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JComponent buildNavigation() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(225, 0));
        nav.setBackground(new Color(233, 236, 241));
        nav.setBorder(new EmptyBorder(18, 14, 18, 14));

        JLabel menuLabel = new JLabel("MÓDULOS");
        menuLabel.setForeground(MUTED_TEXT);
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menuLabel);
        nav.add(Box.createVerticalStrut(10));

        addNavButton(nav, "dashboard", "📊 Painel Inicial", "Resumo operacional do dia");
        addNavButton(nav, "transfers", "🚗 Transfers", "Cadastro e acompanhamento");
        addNavButton(nav, "passageiros", "👥 Passageiros", "Cadastro de passageiros");
        addNavButton(nav, "motoristas", "🧑‍✈️ Motoristas", "Gestão de motoristas");
        addNavButton(nav, "veiculos", "🚙 Veículos", "Controle da frota");
        addNavButton(nav, "ordens", "📋 Ordens de Serviço", "Montagem de OS");

        if (authService.isAdmin()) {
            nav.add(Box.createVerticalStrut(14));
            JLabel adminLabel = new JLabel("ADMIN");
            adminLabel.setForeground(new Color(200, 50, 50));
            adminLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            nav.add(adminLabel);
            nav.add(Box.createVerticalStrut(10));
            addNavButton(nav, "admin", "⚙️ Usuários", "Gestão de usuários do sistema");
        }

        nav.add(Box.createVerticalGlue());
        nav.add(new JSeparator());
        nav.add(Box.createVerticalStrut(12));

        JLabel version = new JLabel("v2.0 Refatorado");
        version.setForeground(MUTED_TEXT);
        version.setFont(new Font("SansSerif", Font.ITALIC, 10));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(version);

        return nav;
    }

    private void addNavButton(JPanel nav, String key, String label, String subtitle) {
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
        button.addActionListener(event -> selectPage(key, label.replaceAll("[🚗👥🧑‍✈️🚙📋⚙️📊]", "").trim(), subtitle));
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
        heading.add(actions, BorderLayout.EAST);

        // Adiciona painéis ao cardPanel
        cardPanel.setBackground(APP_BACKGROUND);
        cardPanel.add(buildDashboardPage(), "dashboard");
        cardPanel.add(buildPassengersPage(), "passageiros");
        cardPanel.add(buildDriversPage(), "motoristas");
        cardPanel.add(buildVehiclesPage(), "veiculos");
        cardPanel.add(buildOrdersPage(), "ordens");
        if (authService.isAdmin()) {
            cardPanel.add(buildAdminPage(), "admin");
        }

        main.add(heading, BorderLayout.NORTH);
        main.add(cardPanel, BorderLayout.CENTER);
        return main;
    }

    // ===== PÁGINAS =====

    private JComponent buildDashboardPage() {
        return new DashboardPanel();
    }

    private JComponent buildPassengersPage() {
        return new PassageirosPanel();
    }

    private JComponent buildDriversPage() {
        return new MotoristasPanel();
    }

    private JComponent buildVehiclesPage() {
        return new VeiculosPanel();
    }

    private JComponent buildOrdersPage() {
        return new OrdensPanel();
    }

    private JComponent buildAdminPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        DefaultTableModel model = createTableModel(
                new String[]{"Usuário", "Nome", "Tipo", "Criado em"},
                new Object[][]{
                        {"admin", "Administrador", "ADMIN", "2024-01-15"}
                }
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
        page.add(table, BorderLayout.CENTER);

        return page;
    }

    // ===== HELPER COMPONENTS =====



    private JComponent splitPage(JComponent left, JComponent right) {
        JPanel page = new JPanel(new BorderLayout(14, 0));
        page.setOpaque(false);
        left.setPreferredSize(new Dimension(345, 0));
        page.add(left, BorderLayout.WEST);
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

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 14, 0);
        form.add(label, constraints);
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
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JComponent tablePanel(String title, String[] columns, Object[][] rows) {
        return tablePanel(title, createTableModel(columns, rows));
    }

    private DefaultTableModel createTableModel(String[] columns, Object[][] rows) {
        return new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void addField(JPanel form, String label, JComponent input, int row) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row * 2 + 1;
        labelConstraints.weightx = 1;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.insets = new Insets(row == 0 ? 0 : 10, 0, 4, 0);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(MUTED_TEXT);
        labelComponent.setFont(BASE_FONT);
        form.add(labelComponent, labelConstraints);

        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 0;
        inputConstraints.gridy = row * 2 + 2;
        inputConstraints.weightx = 1;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(input, inputConstraints);
    }

    private void addActions(JPanel form, JButton primary, JButton secondary) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(primary);
        actions.add(secondary);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 99;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.SOUTHWEST;
        constraints.insets = new Insets(18, 0, 0, 0);
        form.add(actions, constraints);
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

    private void selectPage(String key, String title, String subtitle) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        cardLayout.show(cardPanel, key);

        navButtons.forEach((navKey, button) -> {
            boolean active = navKey.equals(key);
            button.setBackground(active ? ACTIVE_NAV : PANEL_BACKGROUND);
            button.setForeground(active ? PRIMARY_BLUE : TEXT_COLOR);
        });
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja sair?",
                "Confirmação",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginScreen(authService).setVisible(true);
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "SOS VIALE", JOptionPane.INFORMATION_MESSAGE);
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.font", BASE_FONT);
            UIManager.put("Label.font", BASE_FONT);
            UIManager.put("TextField.font", BASE_FONT);
            UIManager.put("ComboBox.font", BASE_FONT);
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.select", new Color(210, 214, 220));
        } catch (Exception ignored) {
        }
    }
}
