package br.com.sosviale.view;

import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel implements LanguageManager.LanguageChangeListener {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color ACCENT_BG = new Color(236, 239, 244);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font WELCOME_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private JLabel welcomeLabel;
    private JLabel dateLabel;
    private JLabel titleLabel;
    private JLabel step1Label, step2Label, step3Label, step4Label, step5Label;
    private JLabel metricPassengersLabel, metricDriversLabel, metricVehiclesLabel, metricTransfersLabel;
    private JLabel metricOrdersLabel, metricTodayLabel, metricLinkedLabel, metricExecutionLabel;
    private JLabel hintPassengersLabel, hintDriversLabel, hintVehiclesLabel, hintTransfersLabel;
    private JLabel hintOrdersLabel, hintTodayLabel, hintLinkedLabel, hintExecutionLabel;
    private JLabel upcomingTitleLabel;
    private JLabel upcomingEmptyLabel;
    private DefaultTableModel upcomingModel;
    private final JLabel[] metricValueLabels = new JLabel[8];

    public DashboardPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);
        LanguageManager.getInstance().addLanguageChangeListener(this);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildMetricsGrid(), BorderLayout.CENTER);
        add(buildBottomSection(), BorderLayout.SOUTH);
        refreshData();
        updateTexts();
    }

    public void refreshData() {
        long totalPassageiros = 0, totalMotoristas = 0, totalVeiculos = 0, transfersSemOS = 0;
        long transfersHoje = 0, transfersComOs = 0, emExecucao = 0, totalOs = 0;
        List<Transfer> proximos = List.of();

        try {
            totalPassageiros = new PassageiroRepository().contar();
            totalMotoristas = new MotoristaRepository().contar();
            totalVeiculos = new VeiculoRepository().contar();
            transfersSemOS = new TransferRepository().contarSemOrdemServico();

            List<Transfer> todos = new TransferService().listarTodos();
            LocalDate hoje = LocalDate.now();
            LocalDateTime agora = LocalDateTime.now();

            transfersHoje = todos.stream()
                    .filter(t -> hoje.equals(t.getDataTransfer()))
                    .count();
            transfersComOs = todos.stream()
                    .filter(t -> t.getOrdemServico() != null)
                    .count();
            emExecucao = todos.stream()
                    .filter(t -> t.getStatus() == StatusTransfer.EM_EXECUCAO)
                    .count();
            totalOs = todos.stream()
                    .filter(t -> t.getOrdemServico() != null)
                    .map(t -> t.getOrdemServico().getId())
                    .distinct()
                    .count();

            proximos = todos.stream()
                    .filter(t -> t.getDataTransfer() != null && t.getHoraTransfer() != null)
                    .filter(t -> {
                        LocalDateTime dt = LocalDateTime.of(t.getDataTransfer(), t.getHoraTransfer());
                        return !dt.isBefore(agora);
                    })
                    .sorted(Comparator.comparing(t ->
                            LocalDateTime.of(t.getDataTransfer(), t.getHoraTransfer())))
                    .limit(8)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setMetricValue(0, totalPassageiros);
        setMetricValue(1, totalMotoristas);
        setMetricValue(2, totalVeiculos);
        setMetricValue(3, transfersSemOS);
        setMetricValue(4, totalOs);
        setMetricValue(5, transfersHoje);
        setMetricValue(6, transfersComOs);
        setMetricValue(7, emExecucao);

        loadUpcoming(proximos);
        dateLabel.setText(LocalDate.now().format(DATE_FMT));
    }

    private void setMetricValue(int index, long value) {
        if (metricValueLabels[index] != null) {
            metricValueLabels[index].setText(String.valueOf(value));
        }
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        welcomeLabel = new JLabel();
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setForeground(PRIMARY_BLUE);

        dateLabel = new JLabel();
        dateLabel.setFont(BASE_FONT);
        dateLabel.setForeground(MUTED_TEXT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(welcomeLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(dateLabel);

        JButton refreshBtn = new JButton("↻");
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        refreshBtn.setToolTipText("Atualizar");
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 10, 4, 10)
        ));
        refreshBtn.addActionListener(e -> refreshData());

        header.add(left, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent buildMetricsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setOpaque(false);

        grid.add(createMetricPanel("metricPassengersLabel", "hintPassengersLabel", 0));
        grid.add(createMetricPanel("metricDriversLabel", "hintDriversLabel", 1));
        grid.add(createMetricPanel("metricVehiclesLabel", "hintVehiclesLabel", 2));
        grid.add(createMetricPanel("metricTransfersLabel", "hintTransfersLabel", 3));
        grid.add(createMetricPanel("metricOrdersLabel", "hintOrdersLabel", 4));
        grid.add(createMetricPanel("metricTodayLabel", "hintTodayLabel", 5));
        grid.add(createMetricPanel("metricLinkedLabel", "hintLinkedLabel", 6));
        grid.add(createMetricPanel("metricExecutionLabel", "hintExecutionLabel", 7));

        return grid;
    }

    private JComponent buildBottomSection() {
        JPanel bottom = new JPanel(new GridLayout(1, 2, 14, 0));
        bottom.setOpaque(false);
        bottom.setPreferredSize(new Dimension(0, 280));
        bottom.add(createWorkflowPanel());
        bottom.add(createUpcomingPanel());
        return bottom;
    }

    private JComponent createMetricPanel(String labelVar, String hintVar, int valueIndex) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel labelComponent = new JLabel();
        labelComponent.setFont(BASE_FONT);
        labelComponent.setForeground(MUTED_TEXT);
        assignLabelRef(labelVar, labelComponent);

        JLabel valueComponent = new JLabel("0");
        valueComponent.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueComponent.setForeground(TEXT_COLOR);
        metricValueLabels[valueIndex] = valueComponent;

        JLabel hintComponent = new JLabel();
        hintComponent.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hintComponent.setForeground(MUTED_TEXT);
        assignHintRef(hintVar, hintComponent);

        panel.add(labelComponent);
        panel.add(Box.createVerticalStrut(6));
        panel.add(valueComponent);
        panel.add(Box.createVerticalStrut(2));
        panel.add(hintComponent);
        return panel;
    }

    private void assignLabelRef(String labelVar, JLabel label) {
        switch (labelVar) {
            case "metricPassengersLabel" -> metricPassengersLabel = label;
            case "metricDriversLabel" -> metricDriversLabel = label;
            case "metricVehiclesLabel" -> metricVehiclesLabel = label;
            case "metricTransfersLabel" -> metricTransfersLabel = label;
            case "metricOrdersLabel" -> metricOrdersLabel = label;
            case "metricTodayLabel" -> metricTodayLabel = label;
            case "metricLinkedLabel" -> metricLinkedLabel = label;
            case "metricExecutionLabel" -> metricExecutionLabel = label;
            default -> { }
        }
    }

    private void assignHintRef(String hintVar, JLabel hint) {
        switch (hintVar) {
            case "hintPassengersLabel" -> hintPassengersLabel = hint;
            case "hintDriversLabel" -> hintDriversLabel = hint;
            case "hintVehiclesLabel" -> hintVehiclesLabel = hint;
            case "hintTransfersLabel" -> hintTransfersLabel = hint;
            case "hintOrdersLabel" -> hintOrdersLabel = hint;
            case "hintTodayLabel" -> hintTodayLabel = hint;
            case "hintLinkedLabel" -> hintLinkedLabel = hint;
            case "hintExecutionLabel" -> hintExecutionLabel = hint;
            default -> { }
        }
    }

    private JComponent createWorkflowPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        titleLabel = new JLabel();
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(5, 1, 0, 8));
        content.setOpaque(false);
        step1Label = new JLabel();
        step2Label = new JLabel();
        step3Label = new JLabel();
        step4Label = new JLabel();
        step5Label = new JLabel();

        content.add(createStep("1", step1Label));
        content.add(createStep("2", step2Label));
        content.add(createStep("3", step3Label));
        content.add(createStep("4", step4Label));
        content.add(createStep("5", step5Label));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createUpcomingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        upcomingTitleLabel = new JLabel();
        upcomingTitleLabel.setFont(SECTION_FONT);
        upcomingTitleLabel.setForeground(TEXT_COLOR);
        panel.add(upcomingTitleLabel, BorderLayout.NORTH);

        upcomingModel = new DefaultTableModel(new String[]{"", "", "", ""}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(upcomingModel);
        table.setRowHeight(26);
        table.setFont(BASE_FONT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 232, 236));

        upcomingEmptyLabel = new JLabel();
        upcomingEmptyLabel.setFont(BASE_FONT);
        upcomingEmptyLabel.setForeground(MUTED_TEXT);
        upcomingEmptyLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(upcomingEmptyLabel, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void loadUpcoming(List<Transfer> proximos) {
        upcomingModel.setRowCount(0);
        LanguageManager lm = LanguageManager.getInstance();
        for (Transfer t : proximos) {
            String rota = (t.getOrigem() != null ? t.getOrigem() : "—") + " → "
                    + (t.getDestino() != null ? t.getDestino() : "—");
            String dt = t.getDataTransfer().format(DATE_FMT) + " " + t.getHoraTransfer().format(TIME_FMT);
            upcomingModel.addRow(new Object[]{
                    t.getId(),
                    rota,
                    dt,
                    lm.translateStatus(t.getStatus())
            });
        }
        upcomingEmptyLabel.setText(proximos.isEmpty()
                ? lm.translate("dashboard.upcoming.empty")
                : " ");
    }

    private JComponent createStep(String marker, JLabel textLabel) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel bullet = new JLabel(marker, SwingConstants.CENTER);
        bullet.setOpaque(true);
        bullet.setBackground(ACCENT_BG);
        bullet.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        bullet.setPreferredSize(new Dimension(34, 28));
        bullet.setFont(new Font("SansSerif", Font.BOLD, 12));

        textLabel.setFont(BASE_FONT);
        textLabel.setForeground(TEXT_COLOR);

        row.add(bullet, BorderLayout.WEST);
        row.add(textLabel, BorderLayout.CENTER);
        return row;
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        welcomeLabel.setText(lm.translate("dashboard.welcome"));
        dateLabel.setText(lm.translate("dashboard.today") + ": " + LocalDate.now().format(DATE_FMT));

        metricPassengersLabel.setText(lm.translate("dashboard.metric.passengers"));
        metricDriversLabel.setText(lm.translate("dashboard.metric.drivers"));
        metricVehiclesLabel.setText(lm.translate("dashboard.metric.vehicles"));
        metricTransfersLabel.setText(lm.translate("dashboard.metric.transfers"));
        metricOrdersLabel.setText(lm.translate("dashboard.metric.orders"));
        metricTodayLabel.setText(lm.translate("dashboard.metric.today"));
        metricLinkedLabel.setText(lm.translate("dashboard.metric.linked"));
        metricExecutionLabel.setText(lm.translate("dashboard.metric.execution"));

        hintPassengersLabel.setText(lm.translate("dashboard.metric.passengers.hint"));
        hintDriversLabel.setText(lm.translate("dashboard.metric.drivers.hint"));
        hintVehiclesLabel.setText(lm.translate("dashboard.metric.vehicles.hint"));
        hintTransfersLabel.setText(lm.translate("dashboard.metric.transfers.hint"));
        hintOrdersLabel.setText(lm.translate("dashboard.metric.orders.hint"));
        hintTodayLabel.setText(lm.translate("dashboard.metric.today.hint"));
        hintLinkedLabel.setText(lm.translate("dashboard.metric.linked.hint"));
        hintExecutionLabel.setText(lm.translate("dashboard.metric.execution.hint"));

        titleLabel.setText(lm.translate("dashboard.workflow.title"));
        step1Label.setText(lm.translate("dashboard.workflow.step1"));
        step2Label.setText(lm.translate("dashboard.workflow.step2"));
        step3Label.setText(lm.translate("dashboard.workflow.step3"));
        step4Label.setText(lm.translate("dashboard.workflow.step4"));
        step5Label.setText(lm.translate("dashboard.workflow.step5"));

        upcomingTitleLabel.setText(lm.translate("dashboard.upcoming.title"));
        if (upcomingModel != null) {
            upcomingModel.setColumnIdentifiers(new String[]{
                    lm.translate("dashboard.upcoming.col.id"),
                    lm.translate("dashboard.upcoming.col.route"),
                    lm.translate("dashboard.upcoming.col.datetime"),
                    lm.translate("dashboard.upcoming.col.status")
            });
        }
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        updateTexts();
        refreshData();
    }
}
