package br.com.sosviale.service;

import br.com.sosviale.model.Transfer;
import javax.swing.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NotificationService {

    private final TransferService transferService;

    public NotificationService(TransferService transferService) {
        this.transferService = transferService;
    }

    public void startMonitoring() {
        // Verifica a cada 1 minuto (60000ms)
        Timer timer = new Timer(60000, e -> checkSchedules());
        timer.start();
        checkSchedules(); // Primeira execução imediata
    }

    private void checkSchedules() {
        List<Transfer> transfers = transferService.listarTodos();
        LocalDateTime agora = LocalDateTime.now();

        for (Transfer t : transfers) {
            if (t.getDataTransfer() != null && t.getHoraTransfer() != null && !t.isNotificado()) {

                // Combina LocalDate e LocalTime em um LocalDateTime
                LocalDateTime horarioDoTransfer = LocalDateTime.of(t.getDataTransfer(), t.getHoraTransfer());

                long minutosRestantes = ChronoUnit.MINUTES.between(agora, horarioDoTransfer);

                // Dispara o alerta se faltar entre 1 e 30 minutos
                if (minutosRestantes > 0 && minutosRestantes <= 30) {
                    t.setNotificado(true); // Marca como notificado para não repetir
                    dispararAlerta(t, minutosRestantes);
                }
            }
        }
    }

    private void dispararAlerta(Transfer t, long minutos) {
        SwingUtilities.invokeLater(() -> {
            String mensagem = String.format(
                    "ALERTA DE PICK-UP\n\nOrigem: %s\nDestino: %s\nHorário: %s\nFaltam: %d minutos",
                    t.getOrigem(), t.getDestino(), t.getHoraTransfer(), minutos
            );

            JOptionPane.showMessageDialog(null, mensagem, "Notificação SOS Viale", JOptionPane.WARNING_MESSAGE);
        });
    }
}