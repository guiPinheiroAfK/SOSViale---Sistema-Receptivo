package br.com.sosviale.view;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.model.User;
import br.com.sosviale.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MainDashboard - Interface principal do SOS VIALE
 * Agora com sistema de cadeados 🔒 e controle de permissão por perfil.
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
    private final UserService userService = new UserService(); // Injeção do serviço de segurança

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

        // Página inicial padrão
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
        product.setForeground(PRIMARY_BLUE);

        JTextField search = new JTextField(" Buscar transfer, passageiro ou OS...");
        search.setPreferredSize(new Dimension(360, 34));
        search.setForeground(MUTED_TEXT);
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(0, 8, 0, 8)
        ));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        User logado = authService.getCurrentUser();
        userLabel.setText("👤 " + (logado != null ? logado.getUsuario() + " (" + logado.getPerfil() + ")" : "Usuário"));
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
        nav.setPreferredSize(new Dimension(240, 0));
        nav.setBackground(new Color(233, 236, 241));
        nav.setBorder(new EmptyBorder(18, 14, 18, 14));

        JLabel menuLabel = new JLabel("MÓDULOS");
        menuLabel.setForeground(MUTED_TEXT);
        menuLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(menuLabel);
        nav.add(Box.createVerticalStrut(10));

        // Botões de Navegação (A lógica de permissão agora está interna no addNavButton)
        addNavButton(nav, "dashboard", "📊 Painel Inicial", "Resumo operacional do dia");
        addNavButton(nav, "transfers", "🚗 Transfers", "Cadastro e acompanhamento");
        addNavButton(nav, "passageiros", "👥 Passageiros", "Cadastro de passageiros");
        addNavButton(nav, "motoristas", "🧑‍✈️ Motoristas", "Gestão de motoristas");
        addNavButton(nav, "veiculos", "🚙 Veículos", "Controle da frota");
        addNavButton(nav, "ordens", "📋 Ordens de Serviço", "Montagem de OS");

        nav.add(Box.createVerticalStrut(14));
        JLabel adminLabel = new JLabel("ADMIN");
        adminLabel.setForeground(new Color(200, 50, 50));
        adminLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(adminLabel);
        nav.add(Box.createVerticalStrut(10));
        addNavButton(nav, "admin", "⚙️ Usuários", "Gestão de acessos");

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
        br.com.sosviale.model.User logado = authService.getCurrentUser();
        String labelExibicao = label;
        boolean temAcesso = true;

        // Verifica se tem acesso para definir o estilo visual
        try {
            userService.verificarPermissao(logado, key);
        } catch (br.com.sosviale.auth.ValidationException e) {
            labelExibicao = "🔒 " + label;
            temAcesso = false; // Marca que o cara tá bloqueado
        }

        JButton button = new JButton(labelExibicao);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));

        // --- MUDANÇA AQUI: Estilo visual discreto ---
        button.setBackground(PANEL_BACKGROUND);
        if (temAcesso) {
            button.setForeground(TEXT_COLOR); // Cor normal (escuro)
        } else {
            button.setForeground(MUTED_TEXT); // Cor "apagada" (cinza claro)
        }

        button.setFont(BASE_FONT);

        button.addActionListener(event -> {
            try {
                userService.verificarPermissao(logado, key);
                selectPage(key, label.replaceAll("[^\\p{L}\\s]", "").trim(), subtitle);
            } catch (br.com.sosviale.auth.ValidationException ex) {
                // Mensagem personalizada: "Somente gerente e atendente podem mudar isso"
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Restrição de Acesso",
                        JOptionPane.WARNING_MESSAGE);
            }
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
        cardPanel.add(new DashboardPanel(), "dashboard");
        cardPanel.add(new TransfersPanel(), "transfers");
        cardPanel.add(new PassageirosPanel(), "passageiros");
        cardPanel.add(new MotoristasPanel(), "motoristas");
        cardPanel.add(new VeiculosPanel(), "veiculos");
        cardPanel.add(new OrdensPanel(), "ordens");
        cardPanel.add(buildAdminPage(), "admin");

        main.add(heading, BorderLayout.NORTH);
        main.add(cardPanel, BorderLayout.CENTER);
        return main;
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

    private JComponent buildAdminPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);
        // ... Lógica da tabela de usuários (simplificada para o exemplo)
        page.add(new JLabel("Painel de Gestão de Usuários (Apenas Admin)", SwingConstants.CENTER));
        return page;
    }

    private void performLogout() {
        if (JOptionPane.showConfirmDialog(this, "Sair do sistema?", "Confirmação", JOptionPane.YES_NO_OPTION) == 0) {
            authService.logout();
            dispose();
            // Chame sua tela de login aqui
        }
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
    }
}