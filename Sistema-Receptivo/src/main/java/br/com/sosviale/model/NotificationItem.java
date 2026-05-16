package br.com.sosviale.model;

import java.time.LocalDateTime;
import java.util.UUID;

// item so em memoria pro sino da ui (nem t0do mundo persiste notificação)

public class NotificationItem {

    private final String id;
    private final int transferId;
    private final String message;
    private final LocalDateTime createdAt;

    public NotificationItem(int transferId, String message) {
        this.id = UUID.randomUUID().toString();
        this.transferId = transferId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public int getTransferId() {
        return transferId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
