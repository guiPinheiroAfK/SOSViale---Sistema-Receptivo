package br.com.sosviale.offline;

import br.com.sosviale.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// ping leve no postgres com cache de alguns segundos pra nao martelar rede

public class ConnectivityService {

    private static final long CACHE_MS = 5_000L;
    private static final AtomicBoolean LAST_ONLINE = new AtomicBoolean(true);
    private static final AtomicLong LAST_CHECK = new AtomicLong(0);

    private ConnectivityService() {}

    public static boolean isDatabaseOnline() {
        long now = System.currentTimeMillis();
        if (now - LAST_CHECK.get() < CACHE_MS) {
            return LAST_ONLINE.get();
        }
        boolean online = probeDatabase();
        LAST_ONLINE.set(online);
        LAST_CHECK.set(now);
        return online;
    }

    // proxima chamada ignora cache (ex.: depois de flyway)

    public static void invalidateCache() {
        LAST_CHECK.set(0);
    }

    private static boolean probeDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(
                    DatabaseConfig.JDBC_URL,
                    DatabaseConfig.JDBC_USER,
                    DatabaseConfig.JDBC_PASSWORD)) {
                return conn.isValid(2);
            }
        } catch (Exception e) {
            return false;
        }
    }
}
