package br.com.sosviale.view;

import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.NotificationItem;
import br.com.sosviale.service.NotificationCenter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

// icone de notificações com painel popup (descartável, sem banco).

public class NotificationBellPanel extends JPanel
        implements LanguageManager.LanguageChangeListener {

    private static final Color PRIMARY_BLUE = new Color(50, 91, 140);
    private static final Color BORDER_COLOR = new Color(210, 214, 220);
    private static final Color MUTED_TEXT = new Color(98, 108, 122);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private final NotificationCenter center = NotificationCenter.getInstance();
    private final JButton bellButton = new JButton();
    private final JLabel badgeLabel = new JLabel();
    private JPopupMenu popup;
    private JPanel listPanel;
    private JLabel popupTitle;
    private JLabel emptyLabel;
    private JButton clearAllButton;

    private final Runnable refreshListener = this::refreshBadge;

    public NotificationBellPanel() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        bellButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
        bellButton.setFocusPainted(false);
        bellButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 10, 4, 10)
        ));
        bellButton.setBackground(Color.WHITE);
        bellButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellButton.addActionListener(e -> togglePopup());

        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(new Color(200, 50, 50));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        badgeLabel.setBorder(new EmptyBorder(1, 5, 1, 5));
        badgeLabel.setVisible(false);

        JPanel wrap = new JPanel(null);
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(52, 36));
        bellButton.setBounds(0, 2, 48, 32);
        badgeLabel.setBounds(30, 0, 22, 16);
        wrap.add(bellButton);
        wrap.add(badgeLabel);

        add(wrap);
        buildPopup();
        center.addListener(refreshListener);
        LanguageManager.getInstance().addLanguageChangeListener(this);
        refreshBadge();
    }

    private void buildPopup() {
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(10, 12, 10, 12));
        content.setPreferredSize(new Dimension(380, 280));

        popupTitle = new JLabel();
        popupTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        content.add(popupTitle, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        emptyLabel = new JLabel();
        emptyLabel.setFont(BASE_FONT);
        emptyLabel.setForeground(MUTED_TEXT);
        emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        JButton clearBtn = new JButton();
        clearBtn.setFont(BASE_FONT);
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            center.clearAll();
            rebuildList();
        });
        footer.add(clearBtn);
        this.clearAllButton = clearBtn;
        content.add(footer, BorderLayout.SOUTH);

        popup.add(content);
        updateTexts();
    }

    private void togglePopup() {
        if (popup.isVisible()) {
            popup.setVisible(false);
            return;
        }
        rebuildList();
        popup.show(bellButton, bellButton.getWidth() - 380, bellButton.getHeight() + 4);
    }

    private void rebuildList() {
        listPanel.removeAll();
        List<NotificationItem> items = center.getAll();
        if (items.isEmpty()) {
            listPanel.add(emptyLabel);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            LanguageManager lm = LanguageManager.getInstance();
            for (NotificationItem item : items) {
                listPanel.add(buildRow(item, fmt, lm));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildRow(NotificationItem item, DateTimeFormatter fmt, LanguageManager lm) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(248, 249, 251));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 8, 8, 8)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel msg = new JLabel("<html>" + item.getMessage().replace("\n", "<br>") + "</html>");
        msg.setFont(BASE_FONT);
        row.add(msg, BorderLayout.CENTER);

        JPanel east = new JPanel(new BorderLayout());
        east.setOpaque(false);
        JLabel time = new JLabel(item.getCreatedAt().format(fmt));
        time.setFont(new Font("SansSerif", Font.PLAIN, 10));
        time.setForeground(MUTED_TEXT);
        east.add(time, BorderLayout.NORTH);

        JButton dismiss = new JButton("×");
        dismiss.setFont(new Font("SansSerif", Font.BOLD, 14));
        dismiss.setForeground(MUTED_TEXT);
        dismiss.setBorderPainted(false);
        dismiss.setContentAreaFilled(false);
        dismiss.setFocusPainted(false);
        dismiss.setToolTipText(lm.translate("notifications.dismiss"));
        dismiss.addActionListener(e -> {
            center.remove(item.getId());
            rebuildList();
        });
        east.add(dismiss, BorderLayout.SOUTH);
        row.add(east, BorderLayout.EAST);

        return row;
    }

    private void refreshBadge() {
        int count = center.count();
        badgeLabel.setText(count > 9 ? "9+" : String.valueOf(count));
        badgeLabel.setVisible(count > 0);
        if (popup != null && popup.isVisible()) {
            rebuildList();
        }
    }

    private void updateTexts() {
        LanguageManager lm = LanguageManager.getInstance();
        bellButton.setText(lm.translate("notifications.bell"));
        bellButton.setToolTipText(lm.translate("notifications.tooltip"));
        popupTitle.setText(lm.translate("notifications.title"));
        emptyLabel.setText(lm.translate("notifications.empty"));
        if (clearAllButton != null) {
            clearAllButton.setText(lm.translate("notifications.clear.all"));
        }
    }

    @Override
    public void onLanguageChanged(LanguageManager.Language newLanguage) {
        updateTexts();
        if (popup.isVisible()) {
            rebuildList();
        }
    }

    public void dispose() {
        center.removeListener(refreshListener);
        LanguageManager.getInstance().removeLanguageChangeListener(this);
    }
}
