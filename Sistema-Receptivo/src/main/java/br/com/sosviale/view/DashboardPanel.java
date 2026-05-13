package br.com.sosviale.view;

import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;
import javax.swing.border.EmptyBorder;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel
        implements LanguageManager.LanguageChangeListener {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    private JLabel titleLabel;
    private JLabel step1Label, step2Label, step3Label, step4Label, step5Label;
    private JLabel metricPassengersLabel, metricDriversLabel, metricVehiclesLabel, metricTransfersLabel;
    private JLabel hintPassengersLabel, hintDriversLabel, hintVehiclesLabel, hintTransfersLabel;

    public DashboardPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);

        // Registrar listener
        LanguageManager.getInstance().addLanguageChangeListener(this);

        long totalPassageiros = 0, totalMotoristas = 0, totalVeiculos = 0, transfersSemOS = 0;
        try {
            totalPassageiros = new PassageiroRepository().contar();
            totalMotoristas = new MotoristaRepository().contar();
            totalVeiculos = new VeiculoRepository().contar();
            transfersSemOS = new TransferRepository().contarSemOrdemServico();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(createMetricPanel(
                "metricPassengersLabel", "metricPassengersLabel", "hintPassengersLabel",
                String.valueOf(totalPassageiros)
        ));
        metrics.add(createMetricPanel(
                "metricDriversLabel", "metricDriversLabel", "hintDriversLabel",
                String.valueOf(totalMotoristas)
        ));
        metrics.add(createMetricPanel(
                "metricVehiclesLabel", "metricVehiclesLabel", "hintVehiclesLabel",
                String.valueOf(totalVeiculos)
        ));
        metrics.add(createMetricPanel(
                "metricTransfersLabel", "metricTransfersLabel", "hintTransfersLabel",
                String.valueOf(transfersSemOS)
        ));

        add(metrics, BorderLayout.NORTH);
        add(createWorkflowPanel(), BorderLayout.CENTER);

        updateTexts();
    }

    private JComponent createMetricPanel(String labelKey, String labelVar, String hintVar, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel labelComponent = new JLabel();
        labelComponent.setFont(BASE_FONT);
        labelComponent.setForeground(MUTED_TEXT);

        // Armazenar referência
        if ("metricPassengersLabel".equals(labelVar)) metricPassengersLabel = labelComponent;
        else if ("metricDriversLabel".equals(labelVar)) metricDriversLabel = labelComponent;
        else if ("metricVehiclesLabel".equals(labelVar)) metricVehiclesLabel = labelComponent;
        else if ("metricTransfersLabel".equals(labelVar)) metricTransfersLabel = labelComponent;

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueComponent.setForeground(TEXT_COLOR);

        JLabel hintComponent = new JLabel();
        hintComponent.setFont(BASE_FONT);
        hintComponent.setForeground(MUTED_TEXT);

        // Armazenar referência
        if ("hintPassengersLabel".equals(hintVar)) hintPassengersLabel = hintComponent;
        else if ("hintDriversLabel".equals(hintVar)) hintDriversLabel = hintComponent;
        else if ("hintVehiclesLabel".equals(hintVar)) hintVehiclesLabel = hintComponent;
        else if ("hintTransfersLabel".equals(hintVar)) hintTransfersLabel = hintComponent;

        panel.add(labelComponent);
        panel.add(Box.createVerticalStrut(8));
        panel.add(valueComponent);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hintComponent);
        return panel;
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

    private JComponent createStep(String marker, JLabel textLabel) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel bullet = new JLabel(marker, SwingConstants.CENTER);
        bullet.setOpaque(true);
        bullet.setBackground(new Color(236, 239, 244));
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

        // Atualizar labels das métricas
        metricPassengersLabel.setText(lm.translate("dashboard.metric.passengers"));
        metricDriversLabel.setText(lm.translate("dashboard.metric.drivers"));
        metricVehiclesLabel.setText(lm.translate("dashboard.metric.vehicles"));
        metricTransfersLabel.setText(lm.translate("dashboard.metric.transfers"));

        // Atualizar hints das métricas
        hintPassengersLabel.setText(lm.translate("dashboard.metric.passengers.hint"));
        hintDriversLabel.setText(lm.translate("dashboard.metric.drivers.hint"));
        hintVehiclesLabel.setText(lm.translate("dashboard.metric.vehicles.hint"));
        hintTransfersLabel.setText(lm.translate("dashboard.metric.transfers.hint"));

        // Atualizar workflow
        titleLabel.setText(lm.translate("dashboard.workflow.title"));
        step1Label.setText(lm.translate("dashboard.workflow.step1"));
        step2Label.setText(lm.translate("dashboard.workflow.step2"));
        step3Label.setText(lm.translate("dashboard.workflow.step3"));
        step4Label.setText(lm.translate("dashboard.workflow.step4"));
        step5Label.setText(lm.translate("dashboard.workflow.step5"));
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        updateTexts();
    }
}