package br.com.sosviale.service;

import br.com.sosviale.model.NotificationItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Armazena notificações apenas em memória durante a sessão da aplicação.

public class NotificationCenter {

    private static NotificationCenter instance;

    private final List<NotificationItem> notifications = new CopyOnWriteArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private NotificationCenter() {}

    public static synchronized NotificationCenter getInstance() {
        if (instance == null) {
            instance = new NotificationCenter();
        }
        return instance;
    }

    public void add(NotificationItem item) {
        if (item == null) return;
        boolean exists = notifications.stream()
                .anyMatch(n -> n.getTransferId() == item.getTransferId());
        if (exists) return;
        notifications.add(0, item);
        fireChanged();
    }

    public void remove(String notificationId) {
        if (notificationId == null) return;
        notifications.removeIf(n -> n.getId().equals(notificationId));
        fireChanged();
    }

    public void clearAll() {
        notifications.clear();
        fireChanged();
    }

    public List<NotificationItem> getAll() {
        return new ArrayList<>(notifications);
    }

    public int count() {
        return notifications.size();
    }

    public void addListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void fireChanged() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
