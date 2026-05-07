package br.com.sosviale.view;

import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;
import javax.swing.border.EmptyBorder;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color TEXT_COLOR = new Color(38, 43, 51);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);

    public DashboardPanel() {
        setLayout(new BorderLayout(14, 14));
        setOpaque(false);

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
        metrics.add(metric("Passageiros cadastrados", String.valueOf(totalPassageiros), "no sistema"));
        metrics.add(metric("Motoristas cadastrados", String.valueOf(totalMotoristas), "no sistema"));
        metrics.add(metric("Veículos cadastrados", String.valueOf(totalVeiculos), "na frota"));
        metrics.add(metric("Transfers sem OS", String.valueOf(transfersSemOS), "aguardando vinculação"));

        add(metrics, BorderLayout.NORTH);
        add(workflowPanel(), BorderLayout.CENTER);
    }

    private JComponent metric(String label, String value, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(BASE_FONT);
        labelComponent.setForeground(MUTED_TEXT);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("SansSerif", Font.BOLD, 30));
        valueComponent.setForeground(TEXT_COLOR);

        JLabel hintComponent = new JLabel(hint);
        hintComponent.setFont(BASE_FONT);
        hintComponent.setForeground(MUTED_TEXT);

        panel.add(labelComponent);
        panel.add(Box.createVerticalStrut(8));
        panel.add(valueComponent);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hintComponent);
        return panel;
    }

    private JComponent workflowPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("Fluxo Operacional");
        title.setFont(SECTION_FONT);
        title.setForeground(TEXT_COLOR);
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(5, 1, 0, 8));
        content.setOpaque(false);
        content.add(step("1", "Cadastrar passageiro e documentos"));
        content.add(step("2", "Agendar transfer com origem/destino"));
        content.add(step("3", "Vincular motorista e veículo em OS"));
        content.add(step("4", "Acompanhar status durante rota"));
        content.add(step("5", "Concluir OS e emitir PDF"));
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JComponent step(String marker, String text) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel bullet = new JLabel(marker, SwingConstants.CENTER);
        bullet.setOpaque(true);
        bullet.setBackground(new Color(236, 239, 244));
        bullet.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        bullet.setPreferredSize(new Dimension(34, 28));
        bullet.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel content = new JLabel(text);
        content.setFont(BASE_FONT);
        content.setForeground(TEXT_COLOR);

        row.add(bullet, BorderLayout.WEST);
        row.add(content, BorderLayout.CENTER);
        return row;
    }
}