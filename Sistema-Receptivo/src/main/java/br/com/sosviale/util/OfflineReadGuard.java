package br.com.sosviale.util;

import br.com.sosviale.App;
import br.com.sosviale.auth.SessionManager;

/**
 * Evita abrir JPA / PostgreSQL nas telas quando o sistema está sem banco (offline)
 * ou em sessão somente local {@link SessionManager#isModoOffline()}.
 */
public final class OfflineReadGuard {

    private OfflineReadGuard() {}

    public static boolean shouldSkipDatabaseReads() {
        if (!App.isDatabaseDisponivel()) {
            return true;
        }
        if (!SessionManager.getInstance().isAutenticado()) {
            return false;
        }
        return SessionManager.getInstance().isModoOffline();
    }
}
