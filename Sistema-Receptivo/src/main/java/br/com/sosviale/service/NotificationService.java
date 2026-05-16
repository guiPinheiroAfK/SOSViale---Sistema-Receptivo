package br.com.sosviale.service;

import br.com.sosviale.i18n.LanguageManager;
import br.com.sosviale.model.NotificationItem;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.offline.ConnectivityService;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// monitora horários de transfer e gera alertas 30 minutos antes do pick-up.

public class NotificationService {

    private static final long CHECK_INTERVAL_MS = 60_000L;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final TransferService transferService;
    private final NotificationCenter center = NotificationCenter.getInstance();
    private final Set<Integer> alertedTransferIds = ConcurrentHashMap.newKeySet();
    private Timer timer;

    public NotificationService(TransferService transferService) {
        this.transferService = transferService;
    }

    public void startMonitoring() {
        if (timer != null && timer.isRunning()) {
            return;
        }
        timer = new Timer((int) CHECK_INTERVAL_MS, e -> checkSchedules());
        timer.setInitialDelay(0);
        timer.start();
    }

    public void stopMonitoring() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void checkSchedules() {
        if (!ConnectivityService.isDatabaseOnline()) {
            return;
        }
        List<Transfer> transfers = transferService.listarTodos();
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = LocalDate.now();

        for (Transfer t : transfers) {
            if (t.getId() == null || t.getDataTransfer() == null || t.getHoraTransfer() == null) {
                continue;
            }
            if (alertedTransferIds.contains(t.getId())) {
                continue;
            }

            LocalDateTime horario = LocalDateTime.of(t.getDataTransfer(), t.getHoraTransfer());
            if (horario.isBefore(agora)) {
                continue;
            }
            if (t.getDataTransfer().isBefore(hoje)) {
                continue;
            }

            long minutosRestantes = ChronoUnit.MINUTES.between(agora, horario);
            if (minutosRestantes >= 29 && minutosRestantes <= 31) {
                alertedTransferIds.add(t.getId());
                dispararNotificacao(t, minutosRestantes);
            }
        }
    }

    private void dispararNotificacao(Transfer t, long minutos) {
        LanguageManager lm = LanguageManager.getInstance();
        String mensagem = lm.translate("notifications.transfer.alert", Map.of(
                "id", String.valueOf(t.getId()),
                "origem", t.getOrigem() != null ? t.getOrigem() : "—",
                "destino", t.getDestino() != null ? t.getDestino() : "—",
                "hora", t.getHoraTransfer().format(TIME_FMT),
                "minutos", String.valueOf(minutos)
        ));

        NotificationItem item = new NotificationItem(t.getId(), mensagem);
        SwingUtilities.invokeLater(() -> center.add(item));
    }
}
